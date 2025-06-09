package info.bitrich.xchangestream.btcmarkets;

import java.io.IOException;
import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.knowm.xchange.btcmarkets.service.BTCMarketsDigestV3;
import org.knowm.xchange.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketHeartbeatMessage;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketSubscriptionMessage;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.rxjava3.core.Observable;
import si.mazi.rescu.SynchronizedValueFactory;

public class BTCMarketsStreamingService extends JsonNettyStreamingService {

	// TODO change to enumerator
	static final String CHANNEL_ORDERBOOK = "orderbook";
	static final String CHANNEL_HEARTBEAT = "heartbeat";
	static final String CHANNEL_TICKER = "tick";
	static final String CHANNEL_TRADE = "trade";
	static final String CHANNEL_ERROR = "error";
	static final String CHANNEL_FUNDCHANGE = "fundChange";
	static final String CHANNEL_ORDERCHANGE = "orderChange";

	private static final Logger LOG = LoggerFactory.getLogger(BTCMarketsStreamingService.class);

	/**
	 * public channels are channels that are available publicly and do not need
	 * authentication
	 */
	private final List<String> publicChannels = Lists.newArrayList(CHANNEL_ORDERBOOK, CHANNEL_HEARTBEAT, CHANNEL_TICKER,
			CHANNEL_TRADE, CHANNEL_ERROR);
	/**
	 * private channels are channels that requires and authenticated/signed message
	 */
	private final List<String> privateChannels = Lists.newArrayList(CHANNEL_FUNDCHANGE, CHANNEL_ORDERCHANGE);

	private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

	private final ConcurrentHashMap<String, Set<String>> subscribedMarketIds = new ConcurrentHashMap<String, Set<String>>();
	private Observable<JsonNode> heartbeat;
	private String heartbeatChannel;
	private final SynchronizedValueFactory<Long> nonceFactory;
	private String apiKey;
	private String apiSecret;

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setApiSecret(String apiSecret) {
		this.apiSecret = apiSecret;
	}

	public BTCMarketsStreamingService(String apiUrl, SynchronizedValueFactory<Long> nonceFactory) {
		super(apiUrl);
		this.nonceFactory = nonceFactory;
		mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
	}

	/*
	 * Implementation renamed from BTCMarketsWebSocketSubscribeMessage to
	 * BTCMarketsWebSocketSubscriptionMessage to look after more than just the
	 * OrderBook subscription. This new implementation also incorporates the use of
	 * adding subscriptions to an existing one instead of having to resubscribing
	 * with all channels and all {@code marketIds} every time the data services
	 * calls the subscribe methods.
	 */
	private BTCMarketsWebSocketSubscriptionMessage buildSubscribeMessage(String channelName, Set<String> marketIds) {

		// Create the first subscription message
		if (!hasActiveSubscriptions()) {
			return BTCMarketsWebSocketSubscriptionMessage.getFirstSubscriptionMessage(
					marketIds == null ? null : Lists.newArrayList(marketIds),
					Lists.newArrayList(channelName, CHANNEL_HEARTBEAT), null, null, null);
		} else {
			return BTCMarketsWebSocketSubscriptionMessage.getAddSubscriptionMessage(Lists.newArrayList(marketIds),
					Lists.newArrayList(channelName), null, null, null);
		}
	}

	private BTCMarketsWebSocketSubscriptionMessage buildRemoveSubscriptionMessage(String channelName,
			Set<String> marketIds) {

		return BTCMarketsWebSocketSubscriptionMessage.getRemoveSubcriptionMessage(
				marketIds == null ? new ArrayList<String>() : Lists.newArrayList(marketIds),
				Lists.newArrayList(channelName), null, null, null);
	}

	@Override
	protected String getChannelNameFromMessage(JsonNode message) {
		LOG.trace("entering: getChannelNameFromMessage");
		final String messageType = message.get("messageType").asText();

		if (messageType.startsWith(CHANNEL_HEARTBEAT))
			return heartbeatChannel;
		if (message.get("marketId") != null)
			return messageType + ":" + message.get("marketId").asText();
		else
			return messageType;
	}

	@Override
	public String getSubscribeMessage(String channelName, Object... args) throws IOException {
		LOG.trace("entering: getSubscribeMessage");

		Set<String> newMarketIds = new HashSet<String>();
		if (publicChannels.contains(channelName) || privateChannels.contains(channelName)) {

			newMarketIds = upddateSubscriptedMarketId(channelName, args);
			LOG.debug("getSubscribeMessage: what is in subscribedMarketIds {} - {} / new marketIds {}", channelName,
					subscribedMarketIds.get(channelName), newMarketIds);
			if (args != null && newMarketIds.isEmpty()) {
				// We are already subscribed, so do nothing
				LOG.debug(channelName + ": No new marketIds, so subscibe message will throw exception");
				throw new IOException("The markets provided are already subscribed:" + args.toString());
			}
		}
		BTCMarketsWebSocketSubscriptionMessage subscriptionMsg;
		if (publicChannels.contains(channelName)) {
			subscriptionMsg = buildSubscribeMessage(channelName, newMarketIds);

		} else if (privateChannels.contains(channelName)) {

			subscriptionMsg = buildAutendticatedSubscribeMessage(channelName, newMarketIds);

		} else {

			throw new IllegalArgumentException("Channel " + channelName + " not currently supported.");
		}
		if (subscriptionMsg.channels.contains(CHANNEL_HEARTBEAT))
			heartbeatChannel = getSubscriptionUniqueId(channelName, args);
		return objectMapper.writeValueAsString(subscriptionMsg);

	}

	private BTCMarketsWebSocketSubscriptionMessage buildAutendticatedSubscribeMessage(String channelName,
			Set<String> marketIds) {

		Assert.notNull(this.apiKey, "API Key not set. Required for subscription to " + channelName);
		Assert.notNull(this.apiSecret, "API Secret not set. Required for subscription to " + channelName);

		// Create the first subscription message
		if (!hasActiveSubscriptions()) {

			BTCMarketsDigestV3 signer = new BTCMarketsDigestV3(this.apiSecret);
			Date now = new Date();

			return BTCMarketsWebSocketSubscriptionMessage.getFirstSubscriptionMessage(
					marketIds == null ? null : Lists.newArrayList(marketIds),
					Lists.newArrayList(channelName, CHANNEL_HEARTBEAT), now.getTime(), this.apiKey,
					signer.sign("/users/self/subscribe" + "\n" + now.getTime()));
		} else {

			BTCMarketsDigestV3 signer = new BTCMarketsDigestV3(this.apiSecret);
			Date now = new Date();

			return BTCMarketsWebSocketSubscriptionMessage.getAddSubscriptionMessage(Lists.newArrayList(marketIds),
					Lists.newArrayList(channelName),
//	    	  null, 	  Lists.newArrayList(channelName),
					now.getTime(), this.apiKey, signer.sign("/users/self/subscribe" + "\n" + now.getTime()));
		}
	}

	private Set<String> upddateSubscriptedMarketId(String channelName, Object... args) {
		Set<String> newMarketIds = new HashSet<String>();
		Set<String> updateMarketIds = subscribedMarketIds.get(channelName);
//  	LOG.debug("Now subscribing to {}:{}", channelName, args);
		if (args != null) {
			for (Object marketId : args) {
				if (updateMarketIds != null) {
					if (!updateMarketIds.contains(marketId))
						newMarketIds.add(marketId.toString());
				} else
					newMarketIds.add(marketId.toString());
			}
		}
		// Add the marketIds to the Channel

		if (updateMarketIds != null) {
			updateMarketIds.addAll(newMarketIds);
			subscribedMarketIds.put(channelName, updateMarketIds);
		} else {
			subscribedMarketIds.put(channelName, newMarketIds);
		}

		return newMarketIds;
	}

	@Override
	public String getSubscriptionUniqueId(String channelName, Object... args) {

		String uniqueId = args == null ? channelName : channelName + ":" + args[0].toString();

		LOG.debug("Returning unique id {}", uniqueId);
		return uniqueId;
	}

	@Override
	public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {

		Assert.notNull(channelName, "channelName can not be null");
		// onDispose passes the channelId that is a colon separated id of
		// channelName:marketid.
		List<String> channelId = Arrays.asList(channelName.split(":"));
		String useChannelName = channelName;
		Set<String> useMarketId = new HashSet<String>();

		if (channelId.size() > 1) {
			// Most likely sent by onDispose
			useChannelName = channelId.get(0);
			useMarketId.add(channelId.get(1));
		} else if (args != null)
			Sets.newHashSet(args).stream().forEach(item -> useMarketId.add((String) item));
//			Set.of(args).stream().forEach(item -> useMarketId.add((String) item));
		
		if (publicChannels.contains(useChannelName) | privateChannels.contains(useChannelName)) {
			LOG.debug("getUnsubscribeMessage: what is in subscribedMarketIds {}:{}", useChannelName,
					subscribedMarketIds.get(useChannelName));
			if (useMarketId.isEmpty())
				subscribedMarketIds.remove(useChannelName);

			else {
//    	  final String finalChannelName = useChannelName;
//    	  Set<String> newMarketIds = new HashSet<String>();
				Set<String> currentMarketIds = subscribedMarketIds.get(useChannelName);

				for (Iterator<String> iterator = useMarketId.iterator(); iterator.hasNext();) {
					String id = (String) iterator.next();
					LOG.debug("useChannelName: {} useMarketId.item: {} : removed? {}", useChannelName, id,
							Boolean.toString(currentMarketIds.remove(id)));

				}
				if (currentMarketIds.isEmpty())
					subscribedMarketIds.remove(currentMarketIds);
				else
					subscribedMarketIds.put(useChannelName, currentMarketIds);
//    	  useMarketId.forEach( item -> subscribedMarketIds.remove(finalChannelName, item) );
			}

			return objectMapper.writeValueAsString(buildRemoveSubscriptionMessage(useChannelName, useMarketId));
		} else {
			return null;
		}
	}

	private Boolean hasActiveSubscriptions() {
		/*
		 * TODO - hasActiveSubscription: this effectively looks at an internally managed
		 * list of channels and their associated subscriptions this whole concept should
		 * be enhanced to handle the heartbeat event and update the channels accordingly
		 * (heartbeat is produced every time a channel is subscribed). One potential
		 * implementation is to always first subscribe to heartbeat and blockwait for
		 * this to return before adding any other subscriptions.
		 */
		return !channels.isEmpty();
	}

	@Override
	public Observable<JsonNode> subscribeChannel(String channelName, Object... args) {

		LOG.trace("entering: subscribeChannel");

		Assert.notNull(channelName, "channelName cannot be null.");

		if (!(publicChannels.contains(channelName) || privateChannels.contains(channelName)))
			throw new IllegalArgumentException("Channel " + channelName + " not yet supported.");

		boolean hasActiveSubription = hasActiveSubscriptions();
		// process heartbeat events
		if (!hasActiveSubription) {

			heartbeat = getHeartbeatSubscription();
			heartbeat.filter(node -> node.findValue("messageType").asText().equals(CHANNEL_HEARTBEAT))
					.map(node -> mapper.treeToValue(node, BTCMarketsWebSocketHeartbeatMessage.class))
					.forEach(hearbeat -> LOG.info("heartbeat -  {}", hearbeat));

		}
		return super.subscribeChannel(channelName, args);

	}

	/** @return {@code} Observable<JsonNode> that will receive heartbeat events} */
	public Observable<JsonNode> getHeartbeatSubscription() throws NonReadableChannelException {

		if (heartbeat == null) {
			heartbeat = super.subscribeChannel(CHANNEL_HEARTBEAT, "");
			if (heartbeat == null)
				throw new NonReadableChannelException();
		}

		heartbeat.filter(node -> node.findValue("messageType").asText().equals(CHANNEL_HEARTBEAT))
				.map(node -> mapper.treeToValue(node, BTCMarketsWebSocketHeartbeatMessage.class))
				.forEach(hearbeat -> LOG.info("heartbeat -  {}", hearbeat));

		return heartbeat;
	}
}

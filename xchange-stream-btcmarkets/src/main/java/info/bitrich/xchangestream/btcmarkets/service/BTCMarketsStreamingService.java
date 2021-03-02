package info.bitrich.xchangestream.btcmarkets.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketHeartbeatMessage;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketSubscriptionMessage;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import java.io.IOException;
import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BTCMarketsStreamingService extends JsonNettyStreamingService {

  //	TODO change to enumerator
  static final String CHANNEL_ORDERBOOK = "orderbook";
  static final String CHANNEL_HEARTBEAT = "heartbeat";
  static final String CHANNEL_TICKER = "tick";
  static final String CHANNEL_TRADE = "trade";

  private static final Logger LOG = LoggerFactory.getLogger(BTCMarketsStreamingService.class);
  /** public channels are channels that are available publicly and do not need authentication */
  private final List<String> publicChannels =
      Lists.newArrayList(CHANNEL_ORDERBOOK, CHANNEL_HEARTBEAT, CHANNEL_TICKER, CHANNEL_TRADE);
  /** private channels are channels that requires and authenticated/signed message */
  private final List<String> privateChannels = Lists.newArrayList();

  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private final ConcurrentHashMap<String, Set<String>> subscribedMarketIds =
      new ConcurrentHashMap<String, Set<String>>();
  private Observable<JsonNode> heartbeat;
  private String heartbeatChannel;

  public BTCMarketsStreamingService(String apiUrl) {
    super(apiUrl);
    mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
  }

  /*
   * Implementation renamed from BTCMarketsWebSocketSubscribeMessage to BTCMarketsWebSocketSubscriptionMessage to look after
   * more than just the OrderBook subscription. This new implementation also incorporates the use of adding subscriptions to an existing one
   * instead of having to resubscribing with all channels and all {@code marketIds} every time the data services calls the subscribe methods.
   */
  private BTCMarketsWebSocketSubscriptionMessage buildSubscribeMessage(
      String channelName, Set<String> marketIds) {

    // Create the first subscription message
    if (!hasActiveSubscriptions()) {
      return BTCMarketsWebSocketSubscriptionMessage.getFirstSubscriptionMessage(
          marketIds == null ? null : Lists.newArrayList(marketIds),
          Lists.newArrayList(channelName, CHANNEL_HEARTBEAT),
          null,
          null,
          null);
    } else {
      return BTCMarketsWebSocketSubscriptionMessage.getAddSubscriptionMessage(
          Lists.newArrayList(marketIds), Lists.newArrayList(channelName), null, null, null);
    }
  }

  private BTCMarketsWebSocketSubscriptionMessage buildRemoveSubscriptionMessage(
      String channelName, Set<String> marketIds) {

    return BTCMarketsWebSocketSubscriptionMessage.getRemoveSubcriptionMessage(
        marketIds == null ? new ArrayList<String>() : Lists.newArrayList(marketIds),
        Lists.newArrayList(channelName),
        null,
        null,
        null);
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {
    LOG.trace("entering: getChannelNameFromMessage");
    final String messageType = message.get("messageType").asText();

    if (messageType.startsWith(CHANNEL_HEARTBEAT)) return heartbeatChannel;
    if (message.get("marketId") != null)
      return messageType + ":" + message.get("marketId").asText();
    else return messageType;
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    LOG.trace("entering: getSubscribeMessage");
    if (publicChannels.contains(channelName)) {

      LOG.debug("Now subscribing to {}:{}", channelName, args);
      Set<String> newMarketIds = new HashSet<String>();
      if (args != null) {
        for (Object marketId : args) {
          newMarketIds.add(marketId.toString());
        }
        // Add the marketIds to the Channel
        Set<String> updateMarketIds = subscribedMarketIds.get(channelName);
        if (updateMarketIds != null) {
          updateMarketIds.addAll(newMarketIds);
          subscribedMarketIds.put(channelName, updateMarketIds);
        } else {
          subscribedMarketIds.put(channelName, newMarketIds);
        }
      }
      LOG.debug(
          "getSubscribeMessage: what is in subscribedMarketIds {} - {} / new marketIds {}",
          channelName,
          subscribedMarketIds.get(channelName),
          newMarketIds);

      BTCMarketsWebSocketSubscriptionMessage subscriptionMsg =
          buildSubscribeMessage(channelName, newMarketIds);
      if (subscriptionMsg.channels.contains(CHANNEL_HEARTBEAT))
        heartbeatChannel = getSubscriptionUniqueId(channelName, args);
      return objectMapper.writeValueAsString(subscriptionMsg);

    } else {
      throw new IllegalArgumentException(
          "Can't create subscribe messsage for channel " + channelName);
    }
  }

  @Override
  public String getSubscriptionUniqueId(String channelName, Object... args) {

    String uniqueId = args == null ? channelName : channelName + ":" + args[0].toString();

    LOG.debug("Returning unique id {}", uniqueId);
    return uniqueId;
  }

  @Override
  public String getUnsubscribeMessage(String channelName) throws IOException {

    if (publicChannels.contains(channelName) | publicChannels.contains(channelName.split(":")[0])) {
      LOG.debug(
          "getUnsubscribeMessage: what is in subscribedMarketIds {}:{}",
          channelName,
          subscribedMarketIds.get(channelName));
      return objectMapper.writeValueAsString(
          buildRemoveSubscriptionMessage(channelName, subscribedMarketIds.remove(channelName)));
    } else {
      return null;
    }
  }

  private Boolean hasActiveSubscriptions() {
    /*
     * TODO - hasActiveSubscription: this effectively looks at an internally managed list of channels and
     * their associated subscriptions this whole concept should be enhanced to handle the heartbeat event and update the channels
     * accordingly (heartbeat is produced every time a channel is subscribed). One potential implementation is to always first
     * subscribe to heartbeat and blockwait for this to return before adding any other subscriptions.
     */
    return !channels.isEmpty();
  }

  @Override
  public Observable<JsonNode> subscribeChannel(String channelName, Object... args) {

    boolean hasActiveSubription = hasActiveSubscriptions();
    Observable<JsonNode> subscription = super.subscribeChannel(channelName, args);
    // process hearbeat events
    if (!hasActiveSubription) {
      heartbeat = subscription;
      heartbeat
          .filter(node -> node.findValue("messageType").asText().equals(CHANNEL_HEARTBEAT))
          .map(node -> mapper.treeToValue(node, BTCMarketsWebSocketHeartbeatMessage.class))
          .forEach(hearbeat -> LOG.info("heartbeat -  {}", hearbeat));
    }

    return subscription;
  }

  /** @return {@code} Observable<JsonNode> that will receive heartbeat events} */
  public Observable<JsonNode> getHeartbeatSubscription() throws NonReadableChannelException {
    if (heartbeat == null) throw new NonReadableChannelException();
    return heartbeat;
  }
}

package info.bitrich.xchangestream.btcmarkets.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketSubscriptionMessage;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import java.io.IOException;
import java.util.ArrayList;
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
  private final ConcurrentHashMap<String, Set<String>> subscribedMarketIds =
      new ConcurrentHashMap<String, Set<String>>();

  public BTCMarketsStreamingService(String apiUrl) {
    super(apiUrl);
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
      return BTCMarketsWebSocketSubscriptionMessage.getFirstSubcritionMessage(
          Lists.newArrayList(marketIds),
          Lists.newArrayList(channelName, CHANNEL_HEARTBEAT),
          null,
          null,
          null);
    } else {
      return BTCMarketsWebSocketSubscriptionMessage.getAddSubcritionMessage(
          Lists.newArrayList(marketIds), Lists.newArrayList(channelName), null, null, null);
    }

    //    return new BTCMarketsWebSocketSubscriptionMessage(
    //        new ArrayList<>(subscribedMarketIds),
    //        Lists.newArrayList(channelName, CHANNEL_HEARTBEAT),
    //        null,
    //        null,
    //        null);
  }

  private BTCMarketsWebSocketSubscriptionMessage buildRemoveSubscriptionMessage(
      String channelName, Set<String> marketIds) {

    return BTCMarketsWebSocketSubscriptionMessage.getRemoveSubcritionMessage(
        marketIds == null ? new ArrayList<String>() : Lists.newArrayList(marketIds),
        Lists.newArrayList(channelName),
        null,
        null,
        null);
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {
    final String messageType = message.get("messageType").asText();
    if (messageType.startsWith(CHANNEL_ORDERBOOK)
        | messageType.startsWith(CHANNEL_TICKER)
        | messageType.startsWith(CHANNEL_TRADE)) {
      return messageType + ":" + message.get("marketId").asText();
    }
    return messageType;
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {

    if (CHANNEL_ORDERBOOK.equals(channelName)
        | CHANNEL_TICKER.equals(channelName)
        | CHANNEL_TRADE.equals(channelName)) {
      // TODO - removing this because it only ever uses the first Instrument provided. Re-look
      // at the reasoning here. For now we are changing all this to enable different
      // channel/instrument subscriptions
      //      subscribedMarketIds.add(args[0].toString());
      //      LOG.debug("Now subscribed to orderbooks {}", subscribedMarketIds);
      LOG.debug("Now subscribing to {}:{}", channelName, args);
      Set<String> newMarketIds = Sets.newConcurrentHashSet();
      if (args != null) {
        for (Object marketId : args) {
          newMarketIds.add(marketId.toString());
        }
        // Add the marketIds to the Channel
        //        if (!subscribedMarketIds.containsKey(channelName))
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
      return objectMapper.writeValueAsString(buildSubscribeMessage(channelName, newMarketIds));
    } else {
      throw new IllegalArgumentException(
          "Can't create subscribe messsage for channel " + channelName);
    }
  }

  @Override
  public String getSubscriptionUniqueId(String channelName, Object... args) {
    LOG.debug("Returning unique id {}", channelName + ":" + args[0].toString());
    return channelName + ":" + args[0].toString();
  }

  @Override
  public String getUnsubscribeMessage(String channelName) throws IOException {
    if (channelName.startsWith(CHANNEL_ORDERBOOK)
        | channelName.startsWith(CHANNEL_TICKER)
        | channelName.startsWith(CHANNEL_TRADE)) {
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
}

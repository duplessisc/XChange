package info.bitrich.xchangestream.btcmarkets;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketOrderbookMessage;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketTickerMessage;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketTradeMessage;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BTCMarketsStreamingAdapters {

  private static final Logger LOG = LoggerFactory.getLogger(BTCMarketsStreamingAdapters.class);

  public static String adaptCurrencyPairToMarketId(CurrencyPair currencyPair) {
    return currencyPair.base.toString() + "-" + currencyPair.counter.toString();
  }

  public static CurrencyPair adaptMarketIdToCurrencyPair(String marketId) {
    String[] parts = marketId.split("-");
    return new CurrencyPair(parts[0], parts[1]);
  }

  public static OrderBook adaptOrderbookMessageToOrderbook(
      BTCMarketsWebSocketOrderbookMessage message) throws InvalidFormatException {
    CurrencyPair currencyPair = adaptMarketIdToCurrencyPair(message.marketId);
    BiFunction<List<BigDecimal>, Order.OrderType, LimitOrder> toLimitOrder =
        (strings, ot) ->
            new LimitOrder.Builder(ot, currencyPair)
                .originalAmount(strings.get(1))
                .limitPrice(strings.get(0))
                .build();

    return new OrderBook(
        DateUtils.fromISODateString(message.timestamp),
        message.asks.stream()
            .map((o) -> toLimitOrder.apply(o, Order.OrderType.ASK))
            .collect(Collectors.toList()),
        message.bids.stream()
            .map((o) -> toLimitOrder.apply(o, Order.OrderType.BID))
            .collect(Collectors.toList()));
  }

  public static Ticker adaptTickerMessageToTicker(BTCMarketsWebSocketTickerMessage message)
      throws InvalidFormatException {
    CurrencyPair currencyPair = adaptMarketIdToCurrencyPair(message.getMarketId());

    return new Ticker.Builder()
        .instrument(currencyPair)
        .last(message.getLastPrice())
        .bid(message.getBestBid())
        .ask(message.getBestAsk())
        .timestamp(DateUtils.fromISODateString(message.getTimestamp()))
        .volume(message.getVolume24h())
        .build();
  }

  public static Trade adaptTradeMessageToTrade(BTCMarketsWebSocketTradeMessage message)
      throws InvalidFormatException {
    CurrencyPair currencyPair = adaptMarketIdToCurrencyPair(message.getMarketId());
    return new Trade.Builder()
        .instrument(currencyPair)
        .id(message.getTradeId())
        .price(message.getPrice())
        .timestamp(DateUtils.fromISODateString(message.getTimestamp()))
        .type(getOrderType(message.getSide()))
        .build();
  }

  private static OrderType getOrderType(String side) {
    OrderType orderType = null;

    if (side.compareToIgnoreCase("ask") == 0) orderType = OrderType.ASK;
    else if (side.compareToIgnoreCase("bid") == 0) orderType = OrderType.BID;
    else LOG.error("Trade side {} not recognised, set to null", side);

    return orderType;
  }

  public static OrderBook adaptOrderUpdateMessageToOrderBook(
      BTCMarketsWebSocketOrderbookMessage message) {

    throw new NotYetImplementedForExchangeException();
  }
}

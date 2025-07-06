package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.rxjava3.core.Observable;

import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserOrderEvent;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserTradeEvent;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CryptoComStreamingTradeService implements StreamingTradeService {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComStreamingTradeService.class);
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

    private final CryptoComAuthenticatedStreamingService streamingService;

    public CryptoComStreamingTradeService(CryptoComAuthenticatedStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    public Observable<Order> getOrderChanges(Instrument instrument, Object... args) {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
        // Channel name can be specific (e.g., user.order.BTC_USDT) or generic (user.order)
        // For now, let's make it specific to the instrument.
        // If null is passed for instrument, we could subscribe to the generic "user.order".
        final String channelName = (instrument == null) ? "user.order" : String.format("user.order.%s", instrumentName);

        return streamingService
            .subscribeChannel(channelName)
            .flatMap(jsonNode -> {
                // According to docs, data is an array of order objects
                JsonNode dataArray = jsonNode.at("/result/data");
                if (dataArray != null && dataArray.isArray()) {
                    java.util.List<Order> orders = new java.util.ArrayList<>();
                    for (JsonNode orderNode : dataArray) {
                        try {
                            CryptoComUserOrderEvent orderEvent = objectMapper.treeToValue(orderNode, CryptoComUserOrderEvent.class);
                            Order order = CryptoComAdapters.adaptUserOrder(orderEvent);
                            // Ensure the order is for the requested instrument if a specific instrument was requested
                            if (instrument == null || order.getInstrument().equals(instrument)) {
                                orders.add(order);
                            }
                        } catch (IOException e) {
                            LOG.error("Error parsing user order event: {}", orderNode, e);
                        }
                    }
                    return Observable.fromIterable(orders);
                } else {
                    LOG.warn("User order data is not an array or is missing in message: {}", jsonNode);
                }
                return Observable.empty();
            })
            .filter(order -> order != null);
    }

    @Override
    public Observable<Order> getOrderChanges(CurrencyPair currencyPair, Object... args) {
        return getOrderChanges((Instrument) currencyPair, args);
    }


    @Override
    public Observable<UserTrade> getUserTrades(Instrument instrument, Object... args) {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
        final String channelName = (instrument == null) ? "user.trade" : String.format("user.trade.%s", instrumentName);

        return streamingService
            .subscribeChannel(channelName)
            .flatMap(jsonNode -> {
                // Data is an array of trade objects
                JsonNode dataArray = jsonNode.at("/result/data");
                if (dataArray != null && dataArray.isArray()) {
                    java.util.List<UserTrade> userTrades = new java.util.ArrayList<>();
                    for (JsonNode tradeNode : dataArray) {
                        try {
                            CryptoComUserTradeEvent tradeEvent = objectMapper.treeToValue(tradeNode, CryptoComUserTradeEvent.class);
                            UserTrade userTrade = CryptoComAdapters.adaptUserTrade(tradeEvent);
                             // Ensure the trade is for the requested instrument if a specific instrument was requested
                            if (instrument == null || userTrade.getInstrument().equals(instrument)) {
                                userTrades.add(userTrade);
                            }
                        } catch (IOException e) {
                            LOG.error("Error parsing user trade event: {}", tradeNode, e);
                        }
                    }
                    return Observable.fromIterable(userTrades);
                } else {
                    LOG.warn("User trade data is not an array or is missing in message: {}", jsonNode);
                }
                return Observable.empty();
            })
            .filter(userTrade -> userTrade != null);
    }

    @Override
    public Observable<UserTrade> getUserTrades(CurrencyPair currencyPair, Object... args) {
        return getUserTrades((Instrument) currencyPair, args);
    }

    // Additional methods from StreamingTradeService that might need implementation or can throw NotYetImplemented:
    // default Observable<Order> getOrderChanges() { ... }
    // default Observable<UserTrade> getUserTrades() { ... }
    // default Observable<Trade> getFillEvents() { ... }
    // default Completable placeLimitOrder(LimitOrder limitOrder) { ... }
    // default Completable placeMarketOrder(MarketOrder marketOrder) { ... }
    // default Completable placeStopOrder(StopOrder stopOrder) { ... }
    // default Completable cancelOrder(CancelOrderParams orderParams) { ... }
    // default Completable cancelOrder(String orderId) { ... }
    // default boolean isPlaceLimitOrderImplemented() { ... }
    // default boolean isPlaceMarketOrderImplemented() { ... }
    // default boolean isPlaceStopOrderImplemented() { ... }
    // default boolean isCancelOrderImplemented() { ... }

}

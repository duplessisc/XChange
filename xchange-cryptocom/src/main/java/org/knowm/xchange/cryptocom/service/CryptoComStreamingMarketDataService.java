package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.rxjava3.core.Observable;
import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComBookEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickerEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTradeEvent;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CryptoComStreamingMarketDataService implements StreamingMarketDataService {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComStreamingMarketDataService.class);
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

    private final CryptoComStreamingService streamingService;

    public CryptoComStreamingMarketDataService(CryptoComStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    private OrderBook handleOrderBookMessage(JsonNode message) {
        LOG.debug("Handling order book message: {}", message);
        try {
            // The actual data is nested within result.data[0] for snapshot
            // For updates, the structure might be different (result.channel == "book.update")
            String channel = message.at("/result/channel").asText("");
            JsonNode dataNode;

            if ("book".equals(channel) && message.at("/result/data").isArray()) { // Initial snapshot
                dataNode = message.at("/result/data/0");
            } else if ("book.update".equals(channel) && message.at("/result/data").isArray()) { // Delta update
                // For delta updates, we need to fetch the full snapshot first or manage updates.
                // For simplicity in this initial step, we'll only fully parse initial snapshots.
                // Proper delta handling requires maintaining the book state.
                LOG.info("Received order book delta update, full parsing not yet implemented for deltas. Message: {}", message);
                // For now, try to parse it as if it's a snapshot, might fail or be incomplete.
                 dataNode = message.at("/result/data/0/update"); // Path for update data
                 if (dataNode.isMissingNode()) {
                     LOG.warn("Delta update data not found at expected path, trying snapshot path for message: {}", message);
                     dataNode = message.at("/result/data/0"); // Fallback for unexpected structure
                 }

            } else {
                LOG.warn("Unrecognized order book message structure: {}", message);
                return null; // Or throw exception
            }

            if (dataNode == null || dataNode.isMissingNode()) {
                LOG.warn("Order book data node is null or missing in message: {}", message);
                return null;
            }


            CryptoComBookEvent bookEvent = objectMapper.treeToValue(dataNode, CryptoComBookEvent.class);
            // Instrument might be in the outer result node
            String instrumentName = message.at("/result/instrument_name").asText();
            if (instrumentName.isEmpty() && dataNode.has("instrument_name")) { // check dataNode if not in result
                 instrumentName = dataNode.get("instrument_name").asText();
            }

            Instrument instrument = CryptoComAdapters.adaptInstrument(instrumentName);
            return CryptoComAdapters.adaptOrderBook(bookEvent, instrument);
        } catch (IOException e) {
            LOG.error("Error parsing order book message: {}", message, e);
            return null;
        }
    }


    @Override
    public Observable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
        // Default depth, Crypto.com supports 10, 50. Let's use 10 as a default.
        // More sophisticated depth handling can be added via args.
        int depth = (args != null && args.length > 0 && args[0] instanceof Integer) ? (Integer) args[0] : 10;
        String channelName = String.format("book.%s.%d", instrumentName, depth);

        // Optional: SNAPSHOT_AND_UPDATE for deltas, requires book_update_frequency
        // String bookSubscriptionType = "SNAPSHOT"; // or "SNAPSHOT_AND_UPDATE"
        // int bookUpdateFrequency = 100; // e.g. 100ms for SNAPSHOT_AND_UPDATE

        return streamingService
            .subscribeChannel(channelName /*, bookSubscriptionType, bookUpdateFrequency (if using deltas) */)
            .map(this::handleOrderBookMessage)
            .filter(orderBook -> orderBook != null && orderBook.getInstrument().equals(instrument));
    }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        return getOrderBook((Instrument) currencyPair, args);
    }


    private Ticker handleTickerMessage(JsonNode message) {
        LOG.debug("Handling ticker message: {}", message);
        try {
            // Data is nested within result.data[0]
            JsonNode dataNode = message.at("/result/data/0");
            if (dataNode.isMissingNode()) {
                LOG.warn("Ticker data node is null or missing in message: {}", message);
                return null;
            }
            CryptoComTickerEvent tickerEvent = objectMapper.treeToValue(dataNode, CryptoComTickerEvent.class);
            // Instrument is part of the ticker data itself ('i' field)
            Instrument instrument = CryptoComAdapters.adaptInstrument(tickerEvent.getInstrumentName());
            return CryptoComAdapters.adaptTicker(tickerEvent, instrument);
        } catch (IOException e) {
            LOG.error("Error parsing ticker message: {}", message, e);
            return null;
        }
    }

    @Override
    public Observable<Ticker> getTicker(Instrument instrument, Object... args) {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
        String channelName = String.format("ticker.%s", instrumentName);

        return streamingService
            .subscribeChannel(channelName)
            .map(this::handleTickerMessage)
            .filter(ticker -> ticker != null && ticker.getInstrument().equals(instrument));
    }

    @Override
    public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
        return getTicker((Instrument) currencyPair, args);
    }

    private Trade handleTradeMessage(JsonNode message) {
        LOG.debug("Handling trade message: {}", message);
        try {
            // Data is an array, process each trade in the array
            // result.data seems to be an array of trades
            JsonNode dataArray = message.at("/result/data");
            if (dataArray.isMissingNode() || !dataArray.isArray()) {
                LOG.warn("Trade data array is null, missing, or not an array in message: {}", message);
                return null; // Or handle differently
            }

            // This will only process the first trade in the array if we map directly.
            // If multiple trades can come in one message, we should use flatMap.
            // For now, assuming one trade object per message or processing the first one.
            if (dataArray.isEmpty()) return null;

            JsonNode dataNode = dataArray.get(0); // Process first trade
            CryptoComTradeEvent tradeEvent = objectMapper.treeToValue(dataNode, CryptoComTradeEvent.class);
            Instrument instrument = CryptoComAdapters.adaptInstrument(tradeEvent.getInstrumentName());
            return CryptoComAdapters.adaptTrade(tradeEvent, instrument);

        } catch (IOException e) {
            LOG.error("Error parsing trade message: {}", message, e);
            return null;
        }
    }

    @Override
    public Observable<Trade> getTrades(Instrument instrument, Object... args) {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
        String channelName = String.format("trade.%s", instrumentName);

        return streamingService
            .subscribeChannel(channelName)
            // If a single message can contain multiple trades in the data array,
            // we need to flatMap and process each item in the array.
            .flatMap(message -> {
                JsonNode dataArray = message.at("/result/data");
                if (dataArray.isArray()) {
                    java.util.List<Trade> trades = new java.util.ArrayList<>();
                    for (JsonNode tradeNode : dataArray) {
                        try {
                            CryptoComTradeEvent event = objectMapper.treeToValue(tradeNode, CryptoComTradeEvent.class);
                            Instrument tradeInstrument = CryptoComAdapters.adaptInstrument(event.getInstrumentName());
                             if (tradeInstrument.equals(instrument)) { // ensure it's for the requested instrument
                                trades.add(CryptoComAdapters.adaptTrade(event, tradeInstrument));
                            }
                        } catch (IOException e) {
                            LOG.error("Error parsing individual trade event: {}", tradeNode, e);
                        }
                    }
                    return Observable.fromIterable(trades);
                } else {
                     // Try parsing as a single trade if not an array (though docs imply array)
                    Trade singleTrade = handleTradeMessage(message);
                    if (singleTrade != null && singleTrade.getInstrument().equals(instrument)) {
                        return Observable.just(singleTrade);
                    }
                }
                return Observable.empty();
            })
            .filter(trade -> trade != null); // Redundant if flatMap handles null by Observable.empty()
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        return getTrades((Instrument) currencyPair, args);
    }
}

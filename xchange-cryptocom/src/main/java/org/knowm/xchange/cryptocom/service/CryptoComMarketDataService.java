package org.knowm.xchange.cryptocom.service;

import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComInstrumentsResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComOrderBookResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComPublicTradesResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickerEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickersResponse;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.marketdata.MarketDataService;
// import org.knowm.xchange.service.marketdata.params.Params; // Not used yet

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CryptoComMarketDataService extends CryptoComMarketDataServiceRaw implements MarketDataService {

    public CryptoComMarketDataService(CryptoComExchange exchange) {
        super(exchange);
    }

    @Override
    public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(currencyPair);
        // The API for specific ticker seems to be via public/get-tickers?instrument_name=INST_NAME
        CryptoComResponse<CryptoComTickersResponse> response = getCryptoComTickers(instrumentName);

        if (response == null || response.getResult() == null || response.getResult().getTickers() == null || response.getResult().getTickers().isEmpty()) {
            throw new IOException("Failed to get ticker for " + instrumentName + ", response or data is null/empty. Response: " + response);
        }

        // getTickers with specific instrument name should return one ticker in the list
        CryptoComTickerEvent tickerEvent = response.getResult().getTickers().get(0);
        return CryptoComAdapters.adaptTicker(tickerEvent, currencyPair);
    }

    @Override
    public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(currencyPair);
        Integer depth = null;
        if (args != null && args.length > 0 && args[0] instanceof Integer) {
            depth = (Integer) args[0];
        } // API default depth is 150 if not specified or if an invalid value is passed.
          // Valid depths are 10, 50, 150 for REST API.

        CryptoComResponse<CryptoComOrderBookResponse> response = getCryptoComOrderBook(instrumentName, depth);

        if (response == null || response.getResult() == null || response.getResult().getOrderBook() == null) {
            throw new IOException("Failed to get order book for " + instrumentName + ", response or data is null. Response: " + response);
        }
        // The DTO CryptoComOrderBookResponse has a getOrderBook() method that extracts the actual book from data[0]
        return CryptoComAdapters.adaptOrderBook(response.getResult().getOrderBook(), currencyPair);
    }

    @Override
    public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
        String instrumentName = CryptoComAdapters.adaptXchangeInstrument(currencyPair);
        // Args parsing for count, start_ts, end_ts for public/get-trades
        Integer count = null; // Default: 25, Max: 150
        Long startTs = null;
        Long endTs = null;

        // Example: (this could be more structured with a Params object)
        if (args != null) {
            if (args.length > 0 && args[0] instanceof Integer) {
                count = (Integer) args[0];
            }
            if (args.length > 1 && args[1] instanceof Long) {
                 startTs = (Long) args[1];
            }
             if (args.length > 2 && args[2] instanceof Long) {
                 endTs = (Long) args[2];
            }
        }

        CryptoComResponse<CryptoComPublicTradesResponse> response = getCryptoComTrades(instrumentName, count, startTs, endTs);

        if (response == null || response.getResult() == null || response.getResult().getTrades() == null) {
            throw new IOException("Failed to get trades for " + instrumentName + ", response or data is null. Response: " + response);
        }
        List<org.knowm.xchange.dto.marketdata.Trade> trades = response.getResult().getTrades().stream()
                .map(event -> CryptoComAdapters.adaptTrade(event, currencyPair))
                .collect(Collectors.toList());
        // The API doesn't specify sort order for public trades, but they are generally time-based.
        // Trade ID 'd' could be used for sorting if needed, but timestamp is more standard for XChange.
        return new Trades(trades, Trades.TradeSortType.SortByTimestamp);
    }

    /**
     * Fetches the list of all instruments (symbols) available on the exchange.
     * This is used by BaseExchange.remoteInit() to populate ExchangeMetaData.
     * @return List of instruments
     * @throws IOException if an error occurs
     */
    public List<Instrument> getInstruments() throws IOException {
        CryptoComResponse<CryptoComInstrumentsResponse> response = getCryptoComInstruments();
        if (response == null || response.getResult() == null || response.getResult().getInstruments() == null) {
            throw new IOException("Failed to get instruments, response or data is null. Response: " + response);
        }
        return response.getResult().getInstruments().stream()
                .filter(instrumentDto -> instrumentDto.isTradable()) // Optionally filter for tradable instruments
                .map(instrumentDto -> CryptoComAdapters.adaptInstrument(instrumentDto.getSymbol()))
                .collect(Collectors.toList());
    }
}

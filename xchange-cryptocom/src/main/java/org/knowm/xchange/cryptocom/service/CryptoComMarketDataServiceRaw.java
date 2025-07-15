package org.knowm.xchange.cryptocom.service;

import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComCandlestickResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComInstrumentsResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComOrderBookResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComPublicTradesResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickersResponse;
import si.mazi.rescu.SynchronizedValueFactory;


import java.io.IOException;

public class CryptoComMarketDataServiceRaw extends CryptoComService {

    protected final CryptoComAPI cryptoComAPI;
    protected final SynchronizedValueFactory<Long> nonceFactory;


    protected CryptoComMarketDataServiceRaw(CryptoComExchange exchange) {
        super(exchange);
        this.cryptoComAPI = exchange.getPublicApi();
        this.nonceFactory = exchange.getNonceFactory();
    }

    /**
     * Corresponds to public/get-instruments
     */
    public CryptoComResponse<CryptoComInstrumentsResponse> getCryptoComInstruments() throws IOException {
        return cryptoComAPI.getInstruments();
    }

    /**
     * Corresponds to public/get-book
     * @param instrumentName e.g., BTC_USDT
     * @param depth 10, 50, 150 (Crypto.com V1 API docs might specify exact valid values)
     */
    public CryptoComResponse<CryptoComOrderBookResponse> getCryptoComOrderBook(String instrumentName, Integer depth) throws IOException {
        return cryptoComAPI.getBook(instrumentName, depth);
    }

    /**
     * Corresponds to public/get-candlestick
     * @param instrumentName e.g., BTC_USDT
     * @param timeframe 1m, 5m, 15m, 30m, 1h, 2h, 4h, 12h, 1D, 7D, 14D, 1M
     * @param count Number of candlesticks (optional)
     * @param startTs Start timestamp ms (optional)
     * @param endTs End timestamp ms (optional)
     */
    public CryptoComResponse<CryptoComCandlestickResponse> getCryptoComCandlesticks(
            String instrumentName, String timeframe, Integer count, Long startTs, Long endTs) throws IOException {
        return cryptoComAPI.getCandlestick(instrumentName, timeframe, count, startTs, endTs);
    }

    /**
     * Corresponds to public/get-trades
     * @param instrumentName e.g., BTC_USDT (optional, null for all recent trades across instruments if API supports)
     * @param count Number of trades (optional)
     * @param startTs Start timestamp ms (optional)
     * @param endTs End timestamp ms (optional)
     */
    public CryptoComResponse<CryptoComPublicTradesResponse> getCryptoComTrades(
            String instrumentName, Integer count, Long startTs, Long endTs) throws IOException {
        return cryptoComAPI.getTrades(instrumentName, count, startTs, endTs);
    }

    /**
     * Corresponds to public/get-tickers
     * @param instrumentName e.g., BTC_USDT (optional, null for all tickers)
     */
    public CryptoComResponse<CryptoComTickersResponse> getCryptoComTickers(String instrumentName) throws IOException {
        // The JAX-RS interface CryptoComAPI.getTickers expects an optional instrumentName.
        // If null is passed, it should fetch all tickers.
        return cryptoComAPI.getTickers(instrumentName);
    }
}

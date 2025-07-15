package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComCandlestickResponse {

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("interval") // Requested interval like "M5", "H1"
    private String interval;

    @JsonProperty("data")
    private List<CryptoComCandlestick> candlesticks;

    // Getters
    public String getInstrumentName() {
        return instrumentName;
    }

    public String getInterval() {
        return interval;
    }

    public List<CryptoComCandlestick> getCandlesticks() {
        return candlesticks;
    }

    // Setters
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public void setCandlesticks(List<CryptoComCandlestick> candlesticks) {
        this.candlesticks = candlesticks;
    }

    @Override
    public String toString() {
        return "CryptoComCandlestickResponse{" +
               "instrumentName='" + instrumentName + '\'' +
               ", interval='" + interval + '\'' +
               ", candlesticks_count=" + (candlesticks != null ? candlesticks.size() : "null") +
               '}';
    }
}

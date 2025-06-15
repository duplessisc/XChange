package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComTickerEvent {

    @JsonProperty("h") // Price of the 24h highest trade
    private BigDecimal high;

    @JsonProperty("l") // Price of the 24h lowest trade
    private BigDecimal low;

    @JsonProperty("a") // The price of the latest trade
    private BigDecimal lastTradePrice;

    @JsonProperty("i") // Instrument name
    private String instrumentName;

    @JsonProperty("v") // The total 24h traded volume
    private BigDecimal volume;

    @JsonProperty("vv") // The total 24h traded volume value (in USD)
    private BigDecimal volumeValue;

    @JsonProperty("oi") // Open interest
    private BigDecimal openInterest;

    @JsonProperty("c") // 24-hour price change
    private BigDecimal change;

    @JsonProperty("b") // The current best bid price
    private BigDecimal bestBidPrice;

    @JsonProperty("bs") // The current best bid size
    private BigDecimal bestBidSize;

    @JsonProperty("k") // The current best ask price
    private BigDecimal bestAskPrice;

    @JsonProperty("ks") // The current best ask size
    private BigDecimal bestAskSize;

    @JsonProperty("t") // The published timestamp in ms
    private long timestamp;

    // Getters
    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getLastTradePrice() {
        return lastTradePrice;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getVolumeValue() {
        return volumeValue;
    }

    public BigDecimal getOpenInterest() {
        return openInterest;
    }

    public BigDecimal getChange() {
        return change;
    }

    public BigDecimal getBestBidPrice() {
        return bestBidPrice;
    }

    public BigDecimal getBestBidSize() {
        return bestBidSize;
    }

    public BigDecimal getBestAskPrice() {
        return bestAskPrice;
    }

    public BigDecimal getBestAskSize() {
        return bestAskSize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters can be added if needed
    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public void setLastTradePrice(BigDecimal lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public void setVolumeValue(BigDecimal volumeValue) {
        this.volumeValue = volumeValue;
    }

    public void setOpenInterest(BigDecimal openInterest) {
        this.openInterest = openInterest;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public void setBestBidPrice(BigDecimal bestBidPrice) {
        this.bestBidPrice = bestBidPrice;
    }

    public void setBestBidSize(BigDecimal bestBidSize) {
        this.bestBidSize = bestBidSize;
    }

    public void setBestAskPrice(BigDecimal bestAskPrice) {
        this.bestAskPrice = bestAskPrice;
    }

    public void setBestAskSize(BigDecimal bestAskSize) {
        this.bestAskSize = bestAskSize;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CryptoComTickerEvent{" +
               "high=" + high +
               ", low=" + low +
               ", lastTradePrice=" + lastTradePrice +
               ", instrumentName='" + instrumentName + '\'' +
               ", volume=" + volume +
               ", volumeValue=" + volumeValue +
               ", openInterest=" + openInterest +
               ", change=" + change +
               ", bestBidPrice=" + bestBidPrice +
               ", bestBidSize=" + bestBidSize +
               ", bestAskPrice=" + bestAskPrice +
               ", bestAskSize=" + bestAskSize +
               ", timestamp=" + timestamp +
               '}';
    }
}

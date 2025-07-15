package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComCandlestick {

    @JsonProperty("t") // Start time of candlestick (Unix timestamp ms)
    private long timestamp;

    @JsonProperty("o") // Open price
    private BigDecimal open;

    @JsonProperty("h") // High price
    private BigDecimal high;

    @JsonProperty("l") // Low price
    private BigDecimal low;

    @JsonProperty("c") // Close price
    private BigDecimal close;

    @JsonProperty("v") // Volume
    private BigDecimal volume;

    // Getters
    public long getTimestamp() { return timestamp; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getClose() { return close; }
    public BigDecimal getVolume() { return volume; }

    // Setters
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setOpen(BigDecimal open) { this.open = open; }
    public void setHigh(BigDecimal high) { this.high = high; }
    public void setLow(BigDecimal low) { this.low = low; }
    public void setClose(BigDecimal close) { this.close = close; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    @Override
    public String toString() {
        return "CryptoComCandlestick{" +
               "timestamp=" + timestamp +
               ", open=" + open +
               ", high=" + high +
               ", low=" + low +
               ", close=" + close +
               ", volume=" + volume +
               '}';
    }
}

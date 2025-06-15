package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComTradeEvent {

    @JsonProperty("d") // Trade ID (long)
    private long tradeId;

    @JsonProperty("t") // Trade timestamp (milliseconds)
    private long timestamp;

    @JsonProperty("tn") // Trade timestamp (nanoseconds) - as string in docs, but long is fine if it fits
    private String timestampNano;


    @JsonProperty("p") // Price
    private BigDecimal price;

    @JsonProperty("q") // Quantity
    private BigDecimal quantity;

    @JsonProperty("s") // Side (BUY/SELL)
    private String side;

    @JsonProperty("i") // Instrument name
    private String instrumentName;

    @JsonProperty("m") // Trade match ID (string in docs)
    private String matchId;


    // Getters
    public long getTradeId() {
        return tradeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTimestampNano() {
        return timestampNano;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public String getSide() {
        return side;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public String getMatchId() {
        return matchId;
    }

    // Setters
    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestampNano(String timestampNano) {
        this.timestampNano = timestampNano;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    @Override
    public String toString() {
        return "CryptoComTradeEvent{" +
               "tradeId=" + tradeId +
               ", timestamp=" + timestamp +
               ", timestampNano='" + timestampNano + '\'' +
               ", price=" + price +
               ", quantity=" + quantity +
               ", side='" + side + '\'' +
               ", instrumentName='" + instrumentName + '\'' +
               ", matchId='" + matchId + '\'' +
               '}';
    }
}

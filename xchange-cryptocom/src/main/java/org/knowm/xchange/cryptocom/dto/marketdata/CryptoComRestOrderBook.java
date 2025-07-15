package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Reusing CryptoComOrderBookEntry from the streaming DTOs as its structure is identical
// import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComOrderBookEntry;

import java.util.List;

/**
 * Represents the actual order book data part, typically found in result.data[0]
 * for public/get-book REST endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComRestOrderBook {

    @JsonProperty("asks")
    private List<CryptoComOrderBookEntry> asks;

    @JsonProperty("bids")
    private List<CryptoComOrderBookEntry> bids;

    @JsonProperty("t")
    private long timestamp; // Snapshot timestamp

    // Getters
    public List<CryptoComOrderBookEntry> getAsks() {
        return asks;
    }

    public List<CryptoComOrderBookEntry> getBids() {
        return bids;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setAsks(List<CryptoComOrderBookEntry> asks) {
        this.asks = asks;
    }

    public void setBids(List<CryptoComOrderBookEntry> bids) {
        this.bids = bids;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CryptoComRestOrderBook{" +
               "asks_count=" + (asks != null ? asks.size() : "null") +
               ", bids_count=" + (bids != null ? bids.size() : "null") +
               ", timestamp=" + timestamp +
               '}';
    }
}

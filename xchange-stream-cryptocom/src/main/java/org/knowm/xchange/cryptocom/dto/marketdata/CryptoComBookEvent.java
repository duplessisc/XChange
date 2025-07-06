package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComBookEvent {

    @JsonProperty("instrument_name")
    private String instrumentName; // May not always be present in the nested data part, might be in wrapper

    @JsonProperty("depth")
    private int depth; // Present in the wrapper, not in the data part usually

    @JsonProperty("data") // This might be how it's structured if the whole message is one event
    private List<BookData> data; // If "data" is an array with one object containing asks/bids

    // Direct fields if "data" is not an array but the object itself
    @JsonProperty("asks")
    private List<CryptoComOrderBookEntry> asks;

    @JsonProperty("bids")
    private List<CryptoComOrderBookEntry> bids;

    @JsonProperty("t")
    private long timestamp; // Message publish time

    @JsonProperty("tt")
    private long lastUpdateTimestamp; // Last book update time

    @JsonProperty("u")
    private long updateSequence;

    @JsonProperty("pu") // For delta updates: previous update sequence
    private Long previousUpdateSequence;


    // Getters
    public String getInstrumentName() {
        return instrumentName;
    }

    public int getDepth() {
        return depth;
    }

    public List<BookData> getData() { // If data is an array
        return data;
    }

    // Getters for direct fields, used if data is not an array
    public List<CryptoComOrderBookEntry> getAsks() {
        if (asks != null) return asks;
        if (data != null && !data.isEmpty() && data.get(0) != null) return data.get(0).getAsks();
        return null;
    }

    public List<CryptoComOrderBookEntry> getBids() {
        if (bids != null) return bids;
        if (data != null && !data.isEmpty() && data.get(0) != null) return data.get(0).getBids();
        return null;
    }

    public long getTimestamp() {
        if (timestamp != 0) return timestamp;
        if (data != null && !data.isEmpty() && data.get(0) != null) return data.get(0).getTimestamp();
        return 0;
    }

    public long getLastUpdateTimestamp() {
        if (lastUpdateTimestamp != 0) return lastUpdateTimestamp;
        if (data != null && !data.isEmpty() && data.get(0) != null) return data.get(0).getLastUpdateTimestamp();
        return 0;
    }

    public long getUpdateSequence() {
        if (updateSequence != 0) return updateSequence;
        if (data != null && !data.isEmpty() && data.get(0) != null) return data.get(0).getUpdateSequence();
        return 0;
    }

    public Long getPreviousUpdateSequence() {
        if (previousUpdateSequence != null) return previousUpdateSequence;
        if (data != null && !data.isEmpty() && data.get(0) != null) return data.get(0).getPreviousUpdateSequence();
        return null;
    }

    // Setters can be added if needed, or use constructor
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setData(List<BookData> data) {
        this.data = data;
    }

    public void setAsks(List<CryptoComOrderBookEntry> asks) {
        this.asks = asks;
    }

    public void setBids(List<CryptoComOrderBookEntry> bids) {
        this.bids = bids;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public void setUpdateSequence(long updateSequence) {
        this.updateSequence = updateSequence;
    }

    public void setPreviousUpdateSequence(Long previousUpdateSequence) {
        this.previousUpdateSequence = previousUpdateSequence;
    }

    /**
     * Inner class to represent the structure within the "data" array
     * { "asks": [...], "bids": [...], "t": ..., "tt": ..., "u": ... }
     * This is for initial snapshot messages where `result.data` is an array.
     * For delta updates (`book.update`), the structure might be `result.data[0].update.asks/bids`.
     * The `CryptoComStreamingMarketDataService` handles this path difference.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BookData {
        @JsonProperty("asks")
        private List<CryptoComOrderBookEntry> asks;

        @JsonProperty("bids")
        private List<CryptoComOrderBookEntry> bids;

        @JsonProperty("t")
        private long timestamp;

        @JsonProperty("tt")
        private long lastUpdateTimestamp;

        @JsonProperty("u")
        private long updateSequence;

        @JsonProperty("pu")
        private Long previousUpdateSequence;


        // Getters
        public List<CryptoComOrderBookEntry> getAsks() { return asks; }
        public List<CryptoComOrderBookEntry> getBids() { return bids; }
        public long getTimestamp() { return timestamp; }
        public long getLastUpdateTimestamp() { return lastUpdateTimestamp; }
        public long getUpdateSequence() { return updateSequence; }
        public Long getPreviousUpdateSequence() { return previousUpdateSequence; }

        // Setters
        public void setAsks(List<CryptoComOrderBookEntry> asks) { this.asks = asks; }
        public void setBids(List<CryptoComOrderBookEntry> bids) { this.bids = bids; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public void setLastUpdateTimestamp(long lastUpdateTimestamp) { this.lastUpdateTimestamp = lastUpdateTimestamp; }
        public void setUpdateSequence(long updateSequence) { this.updateSequence = updateSequence; }
        public void setPreviousUpdateSequence(Long previousUpdateSequence) { this.previousUpdateSequence = previousUpdateSequence; }


        @Override
        public String toString() {
            return "BookData{" +
                   "asks_count=" + (asks != null ? asks.size() : "null") +
                   ", bids_count=" + (bids != null ? bids.size() : "null") +
                   ", timestamp=" + timestamp +
                   ", lastUpdateTimestamp=" + lastUpdateTimestamp +
                   ", updateSequence=" + updateSequence +
                   ", previousUpdateSequence=" + previousUpdateSequence +
                   '}';
        }
    }

    @Override
    public String toString() {
        return "CryptoComBookEvent{" +
               "instrumentName='" + instrumentName + '\'' +
               ", depth=" + depth +
               ", asks_count=" + (getAsks() != null ? getAsks().size() : "null") +
               ", bids_count=" + (getBids() != null ? getBids().size() : "null") +
               ", timestamp=" + getTimestamp() +
               ", lastUpdateTimestamp=" + getLastUpdateTimestamp() +
               ", updateSequence=" + getUpdateSequence() +
               ", previousUpdateSequence=" + getPreviousUpdateSequence() +
               '}';
    }
}

package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the "result" field for a public/get-book API call.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComOrderBookResponse {

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("depth")
    private int depth;

    @JsonProperty("data")
    private List<CryptoComRestOrderBook> bookDataList; // API shows "data" as an array with one element

    // Getters
    public String getInstrumentName() {
        return instrumentName;
    }

    public int getDepth() {
        return depth;
    }

    /**
     * Gets the actual order book data. The list is expected to contain one element.
     * Returns null if the list is empty or null.
     */
    public CryptoComRestOrderBook getOrderBook() {
        if (bookDataList != null && !bookDataList.isEmpty()) {
            return bookDataList.get(0);
        }
        return null;
    }

    public List<CryptoComRestOrderBook> getBookDataList() {
        return bookDataList;
    }


    // Setters
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setBookDataList(List<CryptoComRestOrderBook> bookDataList) {
        this.bookDataList = bookDataList;
    }

    @Override
    public String toString() {
        return "CryptoComOrderBookResponse{" +
               "instrumentName='" + instrumentName + '\'' +
               ", depth=" + depth +
               ", orderBook=" + getOrderBook() +
               '}';
    }
}

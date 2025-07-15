package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Reusing CryptoComTickerEvent from streaming DTOs
// import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickerEvent;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComTickersResponse {

    @JsonProperty("data")
    private List<CryptoComTickerEvent> tickers;

    // Getter
    public List<CryptoComTickerEvent> getTickers() {
        return tickers;
    }

    // Setter
    public void setTickers(List<CryptoComTickerEvent> tickers) {
        this.tickers = tickers;
    }

    @Override
    public String toString() {
        return "CryptoComTickersResponse{" +
               "tickers_count=" + (tickers != null ? tickers.size() : "null") +
               '}';
    }
}

package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Reusing CryptoComTradeEvent from streaming DTOs
// import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTradeEvent;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComPublicTradesResponse {

    // instrument_name might also be part of the result wrapper if not included in each trade event,
    // but the example shows it within each trade event ("i" field).
    // For now, assuming the list of trades is the primary content of "data".

    @JsonProperty("data")
    private List<CryptoComTradeEvent> trades;

    // Getter
    public List<CryptoComTradeEvent> getTrades() {
        return trades;
    }

    // Setter
    public void setTrades(List<CryptoComTradeEvent> trades) {
        this.trades = trades;
    }

    @Override
    public String toString() {
        return "CryptoComPublicTradesResponse{" +
               "trades_count=" + (trades != null ? trades.size() : "null") +
               '}';
    }
}

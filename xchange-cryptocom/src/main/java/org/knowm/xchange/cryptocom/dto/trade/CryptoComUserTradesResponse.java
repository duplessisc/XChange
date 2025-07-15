package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Reusing CryptoComUserTradeEvent from streaming DTOs
// import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserTradeEvent;

import java.util.List;

/**
 * Represents the "result" field of a private/get-trades (user trades) API call.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComUserTradesResponse {

    @JsonProperty("data")
    private List<CryptoComUserTradeEvent> userTrades;

    // Getter
    public List<CryptoComUserTradeEvent> getUserTrades() {
        return userTrades;
    }

    // Setter
    public void setUserTrades(List<CryptoComUserTradeEvent> userTrades) {
        this.userTrades = userTrades;
    }

    @Override
    public String toString() {
        return "CryptoComUserTradesResponse{" +
               "userTrades_count=" + (userTrades != null ? userTrades.size() : "null") +
               '}';
    }
}

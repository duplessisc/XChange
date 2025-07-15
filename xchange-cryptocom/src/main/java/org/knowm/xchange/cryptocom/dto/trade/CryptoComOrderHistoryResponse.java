package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Reusing CryptoComUserOrderEvent from streaming DTOs
// import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserOrderEvent;

import java.util.List;

/**
 * Represents the "result" field of a private/get-order-history API call.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComOrderHistoryResponse {

    @JsonProperty("data")
    private List<CryptoComUserOrderEvent> orders; // List of historical orders

    // Getter
    public List<CryptoComUserOrderEvent> getOrders() {
        return orders;
    }

    // Setter
    public void setOrders(List<CryptoComUserOrderEvent> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return "CryptoComOrderHistoryResponse{" +
               "orders_count=" + (orders != null ? orders.size() : "null") +
               '}';
    }
}

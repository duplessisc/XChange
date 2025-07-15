package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the "result" field for private/create-order and private/cancel-order.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComSubmitOrderResponse {

    @JsonProperty("client_oid")
    private String clientOid;

    @JsonProperty("order_id")
    private String orderId;

    // Getters
    public String getClientOid() {
        return clientOid;
    }

    public String getOrderId() {
        return orderId;
    }

    // Setters
    public void setClientOid(String clientOid) {
        this.clientOid = clientOid;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "CryptoComSubmitOrderResponse{" +
               "clientOid='" + clientOid + '\'' +
               ", orderId='" + orderId + '\'' +
               '}';
    }
}

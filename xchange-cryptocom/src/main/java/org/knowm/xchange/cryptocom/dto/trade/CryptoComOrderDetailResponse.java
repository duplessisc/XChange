package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped; // Potentially, or just map the fields

// Reusing CryptoComUserOrderEvent from streaming DTOs
// import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserOrderEvent;


/**
 * Represents the "result" field of a private/get-order-detail API call.
 * The structure of the order detail itself is the same as CryptoComUserOrderEvent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComOrderDetailResponse {

    // The actual order data is directly the content of the "result" field,
    // not nested further under a "data" field like in list responses.
    // So, we can use @JsonUnwrapped if CryptoComUserOrderEvent contains all fields,
    // or map them directly if we want this class to be a distinct type.
    // For simplicity and direct mapping of the "result" object:

    // Option 1: Direct mapping (if result IS the order event)
    // This assumes the JSON for "result" directly maps to CryptoComUserOrderEvent fields.
    // Let's treat CryptoComUserOrderEvent as the content of "result".
    // The JAX-RS method would be CryptoComResponse<CryptoComUserOrderEvent> getOrderDetail(...)

    // Option 2: If "result" has a specific wrapper object that then contains the order,
    // then this DTO would map that wrapper.
    // Crypto.com example: "result": { order_detail_fields... }
    // So, this DTO will effectively be the same as CryptoComUserOrderEvent for its fields.
    // We can make this class extend CryptoComUserOrderEvent or use @JsonUnwrapped if we had a field.
    // For now, let's assume the JAX-RS method will deserialize the "result" part into CryptoComUserOrderEvent directly.
    // So, CryptoComResponse<CryptoComUserOrderEvent> would be the return type.

    // If a distinct wrapper type for "result" is strictly needed, even if it has the same fields:
    private String accountId;
    private String orderId;
    private String clientOid;
    // ... and all other fields from CryptoComUserOrderEvent ...
    // This would be redundant.

    // Let's assume the service layer will handle this:
    // The JAX-RS method `getOrderDetail` returns `CryptoComResponse<JsonNode>`.
    // The `*ServiceRaw` class then does:
    // `CryptoComResponse<JsonNode> response = api.getOrderDetail(...);`
    // `CryptoComUserOrderEvent orderEvent = objectMapper.treeToValue(response.getResult(), CryptoComUserOrderEvent.class);`
    // Therefore, a specific *Response.java for the *result* of get-order-detail is simply the CryptoComUserOrderEvent itself.

    // This file might not be strictly necessary if the JAX-RS method is defined as:
    // CryptoComResponse<CryptoComUserOrderEvent> getOrderDetail(...);
    // For consistency with other list-based responses having a *Response.java for the "result" object,
    // let's define it as a wrapper for a single order event, though it might seem redundant.
    // The API shows "result": { order_fields... }
    // So this class represents that single object.

    @com.fasterxml.jackson.annotation.JsonUnwrapped
    private CryptoComUserOrderEvent orderDetail;

    public CryptoComUserOrderEvent getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(CryptoComUserOrderEvent orderDetail) {
        this.orderDetail = orderDetail;
    }

    @Override
    public String toString() {
        return "CryptoComOrderDetailResponse{" + orderDetail + "}";
    }
}

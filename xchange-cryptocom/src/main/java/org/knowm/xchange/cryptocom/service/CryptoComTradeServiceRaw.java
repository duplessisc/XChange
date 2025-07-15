package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComOpenOrdersResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComOrderHistoryResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComSubmitOrderResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserOrderEvent; // For order detail
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserTradesResponse;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.instrument.Instrument;
import si.mazi.rescu.RestInvocationParams;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.math.BigDecimal;

public class CryptoComTradeServiceRaw extends CryptoComService {

    protected final CryptoComAuthenticatedAPI authenticatedApi;
    protected final CryptoComDigest signatureCreator;
    protected final SynchronizedValueFactory<Long> nonceFactory;
    protected final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();

    protected CryptoComTradeServiceRaw(CryptoComExchange exchange) {
        super(exchange);
        this.authenticatedApi = exchange.getAuthenticatedApi();
        this.signatureCreator = exchange.getSignatureCreator();
        this.nonceFactory = exchange.getNonceFactory();
        this.apiKey = exchange.getExchangeSpecification().getApiKey();

        if (this.authenticatedApi == null || this.signatureCreator == null || this.apiKey == null) {
            throw new ExchangeException("Authenticated API, signature creator, or API key is null for TradeService.");
        }
    }

    private CryptoComRequest createSignedRequest(String method, ObjectNode params) {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(nonceFactory.createValue());
        request.setMethod(method);
        request.setApiKey(apiKey);
        request.setNonce(nonceFactory.createValue());
        request.setParams(params != null ? params : JsonNodeFactory.instance.objectNode());

        try {
            String tempRequestBody = objectMapper.writeValueAsString(request);
            RestInvocationParams paramsForSigning = RestInvocationParams.create(null, null, tempRequestBody, null, null, null, null, null, null, null, null);
            String signature = signatureCreator.digestParams(paramsForSigning);
            request.setSig(signature);
        } catch (Exception e) {
            throw new ExchangeException("Failed to sign request for " + method, e);
        }
        return request;
    }

    public CryptoComResponse<CryptoComOpenOrdersResponse> getCryptoComOpenOrders(String instrumentName, Integer page, Integer pageSize) throws IOException {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        if (instrumentName != null) {
            params.put("instrument_name", instrumentName);
        }
        if (page != null) {
            params.put("page", page); // API might use page or other pagination
        }
        if (pageSize != null) {
            params.put("page_size", pageSize);
        }
        CryptoComRequest request = createSignedRequest("private/get-open-orders", params);
        return authenticatedApi.getOpenOrders(request);
    }

    public CryptoComResponse<CryptoComSubmitOrderResponse> createCryptoComOrder(ObjectNode orderParams) throws IOException {
        // Method "private/create-order" is set in orderParams by the caller
        CryptoComRequest request = createSignedRequest(orderParams.get("method").asText(), orderParams);
        return authenticatedApi.createOrder(request);
    }

    public CryptoComResponse<CryptoComSubmitOrderResponse> cancelCryptoComOrder(String orderId, String instrumentName) throws IOException {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        params.put("order_id", orderId);
        // instrument_name might be optional for cancel, check API docs. Assuming it might be needed.
        if (instrumentName != null && !instrumentName.isEmpty()) {
             params.put("instrument_name", instrumentName);
        }
        CryptoComRequest request = createSignedRequest("private/cancel-order", params);
        return authenticatedApi.cancelOrder(request);
    }

    public CryptoComResponse<CryptoComUserOrderEvent> getCryptoComOrderDetail(String orderId, String clientOid) throws IOException {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        if (orderId != null) {
            params.put("order_id", orderId);
        } else if (clientOid != null) {
            params.put("client_oid", clientOid);
        } else {
            throw new IllegalArgumentException("Either orderId or clientOid must be provided for getOrderDetail.");
        }
        CryptoComRequest request = createSignedRequest("private/get-order-detail", params);
        // Assuming the response.result for get-order-detail directly maps to CryptoComUserOrderEvent
        return objectMapper.convertValue(authenticatedApi.getOrderDetail(request),
            objectMapper.getTypeFactory().constructParametricType(CryptoComResponse.class, CryptoComUserOrderEvent.class));
    }

    public CryptoComResponse<CryptoComUserTradesResponse> getCryptoComUserTrades(
            String instrumentName, Long startTime, Long endTime, Integer page, Integer pageSize, String type) throws IOException {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        if (instrumentName != null) params.put("instrument_name", instrumentName);
        if (startTime != null) params.put("start_time", startTime); // API uses start_time, end_time
        if (endTime != null) params.put("end_time", endTime);
        if (page != null) params.put("page", page);
        if (pageSize != null) params.put("page_size", pageSize);
        // type might be for filtering specific trade types if API supports, e.g. "ALL", "BUY", "SELL"
        // For "private/get-trades", this usually means user's own trades.
        CryptoComRequest request = createSignedRequest("private/get-trades", params);
        return authenticatedApi.getUserTrades(request);
    }

    public CryptoComResponse<CryptoComOrderHistoryResponse> getCryptoComOrderHistory(
            String instrumentName, Long startTime, Long endTime, Integer page, Integer pageSize) throws IOException {
        ObjectNode params = JsonNodeFactory.instance.objectNode();
        if (instrumentName != null) params.put("instrument_name", instrumentName);
        if (startTime != null) params.put("start_time", startTime);
        if (endTime != null) params.put("end_time", endTime);
        if (page != null) params.put("page", page); // API uses start_ts, end_ts, page, page_size
        if (pageSize != null) params.put("page_size", pageSize);

        CryptoComRequest request = createSignedRequest("private/get-order-history", params);
        return authenticatedApi.getOrderHistory(request);
    }
}

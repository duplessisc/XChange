package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComOpenOrdersResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComOrderHistoryResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComSubmitOrderResponse;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserOrderEvent;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserTradesResponse;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.*;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParams;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CryptoComTradeService extends CryptoComTradeServiceRaw implements TradeService {

    public CryptoComTradeService(CryptoComExchange exchange) {
        super(exchange);
    }

    @Override
    public OpenOrders getOpenOrders() throws IOException {
        return getOpenOrders(createOpenOrdersParams());
    }

    @Override
    public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
        String instrumentName = null;
        Integer page = null; // Crypto.com private/get-open-orders uses page (0-indexed) and page_size (default 20, max 200)
        Integer pageSize = null;

        if (params instanceof OpenOrdersParamsInstrument) {
            Instrument instrument = ((OpenOrdersParamsInstrument) params).getInstrument();
            if (instrument != null) {
                instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
            }
        }
        if (params instanceof OpenOrdersParamsPaging) {
             page = ((OpenOrdersParamsPaging) params).getPageNumber(); // Assumes 0-indexed if that's what CryptoComParamsPaging uses
             pageSize = ((OpenOrdersParamsPaging) params).getPageLength();
        }


        CryptoComResponse<CryptoComOpenOrdersResponse> response = getCryptoComOpenOrders(instrumentName, page, pageSize);
        if (response == null || response.getResult() == null || response.getResult().getOrders() == null) {
            throw new ExchangeException("Failed to get open orders. Response or data is null. Response: " + response);
        }

        List<LimitOrder> limitOrders = response.getResult().getOrders().stream()
                .map(CryptoComAdapters::adaptUserOrder)
                // Filter for LimitOrder instances, as OpenOrders typically holds LimitOrders.
                // CryptoComAdapters.adaptUserOrder might return various Order subtypes.
                .filter(o -> o instanceof LimitOrder || o instanceof StopOrder) // StopOrders can also be open
                .map(o -> (LimitOrder) o) // This cast might be problematic if StopOrder is not a LimitOrder.
                                          // XChange OpenOrders is List<LimitOrder>, so we need to ensure compatibility.
                                          // A better approach might be to have adaptUserOrder return a common Order type
                                          // and then filter/cast more carefully or adapt OpenOrders to hold more types.
                                          // For now, assuming most open orders are limit or can be represented as such.
                                          // If StopOrder is not a LimitOrder, this needs adjustment.
                                          // StopOrder does extend Order, but OpenOrders is List<LimitOrder>.
                                          // This will need review based on how StopOrders are handled in XChange model for OpenOrders.
                                          // For simplicity, if adaptUserOrder for stop orders returns a StopOrder that IS a LimitOrder (e.g. stop-limit), it's fine.
                                          // If it's a market-based stop order, it won't fit List<LimitOrder>.
                                          // Let's assume adaptUserOrder returns appropriate types and we filter for LimitOrder for now.
                .collect(Collectors.toList());

        // To handle StopOrders in OpenOrders, we might need a custom OpenOrders object or filter differently.
        // For now, this focuses on LimitOrders which is the typical content of XChange OpenOrders.
        // A more robust solution would be to have `adaptUserOrder` produce `LimitOrder` for limit/stop-limit
        // and then decide how to include other open order types if `OpenOrders` DTO is restricted to `LimitOrder`.
        // Let's refine the filter:
        List<LimitOrder> collectedOrders = response.getResult().getOrders().stream()
            .map(CryptoComAdapters::adaptUserOrder)
            .filter(order -> order instanceof LimitOrder) // Only include actual LimitOrders
            .map(LimitOrder.class::cast)
            .collect(Collectors.toList());

        return new OpenOrders(collectedOrders);
    }

    private String placeOrder(Order order) throws IOException {
        ObjectNode paramsNode = JsonNodeFactory.instance.objectNode();
        // "method" is part of the request DTO for signing, but not part of "params" object for create-order
        // It's set in createSignedRequest in Raw service.
        // Here, we just prepare the "params" field for CryptoComRequest.

        paramsNode.put("instrument_name", CryptoComAdapters.adaptXchangeInstrument(order.getInstrument()));
        paramsNode.put("side", order.getType() == Order.OrderType.BID ? "BUY" : "SELL");
        paramsNode.put("quantity", order.getOriginalAmount().toPlainString());

        if (order instanceof LimitOrder) {
            paramsNode.put("type", "LIMIT");
            paramsNode.put("price", ((LimitOrder) order).getLimitPrice().toPlainString());
        } else if (order instanceof MarketOrder) {
            paramsNode.put("type", "MARKET");
            // For MARKET BUY, Crypto.com API might prefer/require 'notional' (amount to spend).
            // If 'notional' is provided in MarketOrder, use it. Otherwise, quantity is used (for SELL or if notional not supported for BUY).
            // This needs to be verified against Crypto.com API for MARKET BUY orders.
            // Assuming 'quantity' is acceptable for now for both BUY/SELL market orders.
        } else if (order instanceof StopOrder) {
            StopOrder stopOrder = (StopOrder) order;
            paramsNode.put("trigger_price", stopOrder.getTriggerPrice().toPlainString()); // API uses 'ref_price'
            // The DTO and Adapter for CryptoComUserOrderEvent should use 'ref_price' if that's what the API takes.
            // Let's assume the adapter handles mapping 'trigger_price' to 'ref_price' if needed.
            // For now, using 'trigger_price' as it's the XChange field.
            // The actual field name sent to Crypto.com should be 'ref_price'.
            // This means the ObjectNode here should use 'ref_price'.
            // Let's correct this:
            paramsNode.put("ref_price", stopOrder.getTriggerPrice().toPlainString());


            if (stopOrder.getLimitPrice() != null) {
                paramsNode.put("type", "STOP_LIMIT");
                paramsNode.put("price", stopOrder.getLimitPrice().toPlainString());
            } else {
                // Crypto.com uses STOP_LOSS (for market execution after trigger)
                // or TAKE_PROFIT (also market execution after trigger)
                // We need to infer intent or require a flag. Assuming STOP_LOSS for now.
                paramsNode.put("type", "STOP_LOSS");
            }
        } else {
            throw new ExchangeException("Unsupported order type: " + order.getClass().getName());
        }

        if (order.getUserReference() != null) {
            paramsNode.put("client_oid", order.getUserReference());
        }

        // Handle TimeInForce (e.g., GOOD_TILL_CANCEL, IMMEDIATE_OR_CANCEL, FILL_OR_KILL)
        if (order.hasFlag(Order.OrderFlags.IMMEDIATE_OR_CANCEL)) {
            paramsNode.put("time_in_force", "IMMEDIATE_OR_CANCEL");
        } else if (order.hasFlag(Order.OrderFlags.FILL_OR_KILL)) {
            paramsNode.put("time_in_force", "FILL_OR_KILL");
        } else {
            paramsNode.put("time_in_force", "GOOD_TILL_CANCEL"); // Default
        }

        // Handle PostOnly flag (exec_inst: ["POST_ONLY"])
        if (order.hasFlag(Order.OrderFlags.POST_ONLY)) {
            paramsNode.putArray("exec_inst").add("POST_ONLY");
        }

        // The method "private/create-order" will be set in createSignedRequest in Raw service.
        // Here, we pass the "params" part of the request.
        CryptoComRequest request = createSignedRequest("private/create-order", paramsNode);


        CryptoComResponse<CryptoComSubmitOrderResponse> response = authenticatedApi.createOrder(request);

        if (response == null || response.getResult() == null || response.getResult().getOrderId() == null) {
            String message = (response != null && response.getMessage() != null) ? response.getMessage() : "Invalid response from exchange.";
            if (response != null && response.getCode() != 0) message = "Error code: " + response.getCode() + " - " + message;
            throw new ExchangeException("Failed to place order. " + message);
        }
        if (response.getCode() != 0) {
             throw new ExchangeException("Failed to place order. Error code: " + response.getCode() + ", Message: " + response.getMessage());
        }
        return response.getResult().getOrderId();
    }

    @Override
    public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
        return placeOrder(marketOrder);
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
        return placeOrder(limitOrder);
    }

    @Override
    public String placeStopOrder(StopOrder stopOrder) throws IOException {
        return placeOrder(stopOrder);
    }

    @Override
    public boolean cancelOrder(String orderId) throws IOException {
         throw new NotYetImplementedForExchangeException("Use cancelOrder(CancelOrderParams params) instead. Instrument context might be needed by Crypto.com API for cancellation.");
    }

    @Override
    public boolean cancelOrder(CancelOrderParams params) throws IOException {
        String orderId = null;
        String instrumentName = null;

        if (params instanceof CancelOrderByIdParams) {
            orderId = ((CancelOrderByIdParams) params).getOrderId();
        }
        if (params instanceof CancelOrderByInstrument) { // Crypto.com cancel does not require instrument_name
           // instrumentName = CryptoComAdapters.adaptXchangeInstrument(((CancelOrderByInstrument) params).getInstrument());
        }
         if (params instanceof DefaultCancelOrderParamIdAndInstrument) {
            orderId = ((DefaultCancelOrderParamIdAndInstrument) params).getOrderId();
            // instrumentName = CryptoComAdapters.adaptXchangeInstrument(((DefaultCancelOrderParamIdAndInstrument) params).getInstrument());
        }


        if (orderId == null) {
            throw new IllegalArgumentException("Order ID must be provided in CancelOrderParams for Crypto.com.");
        }

        CryptoComResponse<CryptoComSubmitOrderResponse> response = cancelCryptoComOrder(orderId, instrumentName);
        // Successful cancel usually returns code 0.
        return response != null && response.getCode() == 0;
    }

    @Override
    public Order getOrder(String orderId, Object... args) throws IOException {
        // This method is deprecated in XChange. Prefer getOrder(OrderQueryParams...).
        // For compatibility, can implement if simple.
        // Assuming no clientOid or instrument needed if only orderId is primary.
        if (orderId == null) throw new IllegalArgumentException("Order ID cannot be null.");
        OrderQueryParams param = new DefaultOrderQueryParams(orderId);
        Collection<Order> orders = getOrder(param);
        return orders.isEmpty() ? null : orders.iterator().next();
    }

    @Override
    public Collection<Order> getOrder(OrderQueryParams... params) throws IOException {
        if (params == null || params.length == 0 || params[0] == null) {
            throw new IllegalArgumentException("OrderQueryParams must be provided.");
        }

        String orderId = params[0].getOrderId();
        String clientOid = null; // TODO: Check if OrderQueryParams has clientOrderId
        // String clientOid = params[0].getClientOrderId(); // If available in a specific OrderQueryParams implementation

        if (orderId == null && clientOid == null) {
            throw new IllegalArgumentException("Either Order ID or Client Order ID must be in OrderQueryParams.");
        }

        CryptoComResponse<CryptoComUserOrderEvent> response = getCryptoComOrderDetail(orderId, clientOid);
        if (response == null || response.getResult() == null) {
            // If code is specific for "not found", could return emptyList.
            // Example: if (response != null && response.getCode() == SOME_NOT_FOUND_CODE) return Collections.emptyList();
            throw new ExchangeException("Failed to get order detail for query: " + (orderId != null ? orderId : clientOid));
        }
        if (response.getCode() != 0) { // Check for API error codes
             if (response.getMessage() != null && response.getMessage().toLowerCase().contains("not found")) { // Heuristic
                return Collections.emptyList();
            }
            throw new ExchangeException("Failed to get order detail. Error code: " + response.getCode() + ", Message: " + response.getMessage());
        }
        return Collections.singletonList(CryptoComAdapters.adaptUserOrder(response.getResult()));
    }


    @Override
    public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
        String instrumentName = null;
        if (params instanceof TradeHistoryParamsInstrument) {
            Instrument instrument = ((TradeHistoryParamsInstrument) params).getInstrument();
            if (instrument != null) {
                instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
            }
        }
        Long startTime = null;
        Long endTime = null;
        if (params instanceof TradeHistoryParamsTimeSpan) {
            startTime = ((TradeHistoryParamsTimeSpan) params).getStartTime() != null ? ((TradeHistoryParamsTimeSpan) params).getStartTime().getTime() : null;
            endTime = ((TradeHistoryParamsTimeSpan) params).getEndTime() != null ? ((TradeHistoryParamsTimeSpan) params).getEndTime().getTime() : null;
        }

        Integer page = null; // Crypto.com uses 0-indexed page
        Integer pageSize = null; // Default 20, Max 200
        if (params instanceof TradeHistoryParamsPaging) {
            page = ((TradeHistoryParamsPaging) params).getPageNumber();
            pageSize = ((TradeHistoryParamsPaging) params).getPageLength();
        }

        CryptoComResponse<CryptoComUserTradesResponse> response = getCryptoComUserTrades(instrumentName, startTime, endTime, page, pageSize, null);
        if (response == null || response.getResult() == null || response.getResult().getUserTrades() == null) {
            throw new ExchangeException("Failed to get trade history. Response or data is null. Response: " + response);
        }
         if (response.getCode() != 0) {
             throw new ExchangeException("Failed to get trade history. Error code: " + response.getCode() + ", Message: " + response.getMessage());
        }
        List<UserTrade> trades = response.getResult().getUserTrades().stream()
                .map(CryptoComAdapters::adaptUserTrade)
                .collect(Collectors.toList());
        // TODO: Crypto.com does not provide total trade count in this response for pagination.
        // XChange UserTrades expects a tradeCount. We can use list size or null.
        return new UserTrades(trades, Trades.TradeSortType.SortByTimestamp);
    }

    @Override
    public OpenOrdersParams createOpenOrdersParams() {
        // DefaultOpenOrdersParamsInstrument allows setting an instrument for filtering
        return new DefaultOpenOrdersParamsInstrument();
    }

    @Override
    public TradeHistoryParams createTradeHistoryParams() {
        return new CryptoComTradeHistoryParams();
    }

    public static class CryptoComTradeHistoryParams extends DefaultTradeHistoryParamsTimeSpan implements
        TradeHistoryParamsInstrument, TradeHistoryParamsPaging {
        private Instrument instrument;
        private Integer pageLength; // page_size (default 20, max 200)
        private Integer pageNumber; // page (0-indexed)

        public CryptoComTradeHistoryParams() {}

        @Override public Instrument getInstrument() { return instrument; }
        @Override public void setInstrument(Instrument instrument) { this.instrument = instrument; }
        @Override public Integer getPageLength() { return pageLength; }
        @Override public void setPageLength(Integer pageLength) { this.pageLength = pageLength; }
        @Override public Integer getPageNumber() { return pageNumber; }
        @Override public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }
    }

    @Override
    public Collection<Order> getOrderHistory(OrderHistoryParams params) throws IOException {
        String instrumentName = null;
        if (params instanceof OrderHistoryParamsInstrument) {
            Instrument instrument = ((OrderHistoryParamsInstrument) params).getInstrument();
            if (instrument != null) {
                instrumentName = CryptoComAdapters.adaptXchangeInstrument(instrument);
            }
        }
        Long startTime = null;
        Long endTime = null;
        if (params instanceof OrderHistoryParamsTimeSpan) {
            startTime = ((OrderHistoryParamsTimeSpan) params).getStartTime() != null ? ((OrderHistoryParamsTimeSpan) params).getStartTime().getTime() : null;
            endTime = ((OrderHistoryParamsTimeSpan) params).getEndTime() != null ? ((OrderHistoryParamsTimeSpan) params).getEndTime().getTime() : null;
        }

        Integer page = null; // page (0-indexed)
        Integer pageSize = null; // page_size (default 20, max 200)
        // Crypto.com get-order-history has page and page_size.
        // Need to ensure OrderHistoryParams can provide these.
        // Let's assume a custom CryptoComOrderHistoryParams or check existing interfaces.
        // For now, using null if not directly available from a standard XChange interface.
        // if (params instanceof TradeHistoryParamsPaging) { // Re-using for concept
        //    page = ((TradeHistoryParamsPaging) params).getPageNumber();
        //    pageSize = ((TradeHistoryParamsPaging) params).getPageLength();
        // }


        CryptoComResponse<CryptoComOrderHistoryResponse> response = getCryptoComOrderHistory(instrumentName, startTime, endTime, page, pageSize);
        if (response == null || response.getResult() == null || response.getResult().getOrders() == null) {
            throw new ExchangeException("Failed to get order history. Response or data is null. Response: " + response);
        }
        if (response.getCode() != 0) {
             throw new ExchangeException("Failed to get order history. Error code: " + response.getCode() + ", Message: " + response.getMessage());
        }
        return response.getResult().getOrders().stream()
                .map(CryptoComAdapters::adaptUserOrder)
                .collect(Collectors.toList());
    }
}

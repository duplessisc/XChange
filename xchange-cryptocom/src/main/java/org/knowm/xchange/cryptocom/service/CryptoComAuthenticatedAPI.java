package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest; // Assuming this DTO is in the base dto package

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Interface for Crypto.com authenticated REST API endpoints.
 * Base URL for ResCU can be set in ExchangeSpecification (e.g., https://api.crypto.com)
 * Common path prefix for these methods is /exchange/v1
 * Authentication details (api_key, sig, nonce, method, id, params) are sent in the request body.
 */
@Path("/exchange/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CryptoComAuthenticatedAPI {

    // Account Balance and Position API
    @POST
    @Path("private/user-balance")
    JsonNode userBalance(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/user-balance-history")
    JsonNode userBalanceHistory(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-accounts")
    JsonNode getAccounts(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-positions")
    JsonNode getPositions(CryptoComRequest requestBody) throws IOException;

    // Trading API
    @POST
    @Path("private/create-order")
    JsonNode createOrder(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/amend-order")
    JsonNode amendOrder(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/cancel-order")
    JsonNode cancelOrder(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/cancel-all-orders")
    JsonNode cancelAllOrders(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/close-position")
    JsonNode closePosition(CryptoComRequest requestBody) throws IOException;


    @POST
    @Path("private/get-open-orders")
    JsonNode getOpenOrders(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-order-detail")
    JsonNode getOrderDetail(CryptoComRequest requestBody) throws IOException;

    // Order, Trade, Transaction History API
    @POST
    @Path("private/get-order-history")
    JsonNode getOrderHistory(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-trades") // User trades
    JsonNode getUserTrades(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-transactions")
    JsonNode getTransactions(CryptoComRequest requestBody) throws IOException;

    // Wallet API
    @POST
    @Path("private/create-withdrawal")
    JsonNode createWithdrawal(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-currency-networks")
    JsonNode getCurrencyNetworks(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-deposit-address")
    JsonNode getDepositAddress(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-deposit-history")
    JsonNode getDepositHistory(CryptoComRequest requestBody) throws IOException;

    @POST
    @Path("private/get-withdrawal-history")
    JsonNode getWithdrawalHistory(CryptoComRequest requestBody) throws IOException;

    // Note: The CryptoComRequest DTO (used as requestBody) should contain fields for
    // id, method, params, api_key, sig, nonce.
    // The 'method' field in the DTO will be redundant with the @Path annotation here
    // but is required for signature calculation. ResCU might need configuration
    // or a custom RequestWriter to ensure the body is formatted as Crypto.com expects
    // if the path parameter also dictates the method in the body.
    // Typically, the signing interceptor populates api_key, sig, nonce, and ensures method/id are correct.
}

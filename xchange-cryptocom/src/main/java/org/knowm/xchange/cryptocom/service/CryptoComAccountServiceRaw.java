package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.account.CryptoComAccountBalanceResponse;
import org.knowm.xchange.cryptocom.dto.account.CryptoComPositionResponse;
import org.knowm.xchange.exceptions.ExchangeException;
import si.mazi.rescu.RestInvocationParams;
import si.mazi.rescu.SynchronizedValueFactory;


import java.io.IOException;
import java.util.HashMap;


public class CryptoComAccountServiceRaw extends CryptoComService {

    protected final CryptoComAuthenticatedAPI authenticatedApi;
    protected final CryptoComDigest signatureCreator;
    protected final SynchronizedValueFactory<Long> nonceFactory;
    protected final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();


    protected CryptoComAccountServiceRaw(CryptoComExchange exchange) {
        super(exchange);
        this.authenticatedApi = exchange.getAuthenticatedApi();
        this.signatureCreator = exchange.getSignatureCreator();
        this.nonceFactory = exchange.getNonceFactory();
        this.apiKey = exchange.getExchangeSpecification().getApiKey();

        if (this.authenticatedApi == null || this.signatureCreator == null || this.apiKey == null) {
            throw new ExchangeException("Authenticated API, signature creator, or API key is null. " +
                                        "Ensure API key and secret are provided in ExchangeSpecification.");
        }
    }

    /**
     * Corresponds to private/user-balance
     */
    public CryptoComResponse<CryptoComAccountBalanceResponse> getCryptoComAccountBalance() throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(nonceFactory.createValue()); // Using nonce for ID as well, common practice
        request.setMethod("private/user-balance");
        request.setApiKey(apiKey);
        request.setNonce(nonceFactory.createValue());
        request.setParams(JsonNodeFactory.instance.objectNode()); // Empty params for user-balance

        // Sign the request
        // The CryptoComDigest expects RestInvocationParams, which includes the full request body string.
        // We need to serialize the request *without* the signature first, then sign that string.
        try {
            String tempRequestBody = objectMapper.writeValueAsString(request);

            // Construct RestInvocationParams manually for signing
            // Path is not strictly needed by CryptoComDigest if method is in DTO, but good to be aware
            RestInvocationParamsรีวิวparamsForSigning = RestInvocationParams.create(null, null, tempRequestBody, null, null, null, null, null, null, null, null);
            String signature = signatureCreator.digestParams(paramsForSigning);
            request.setSig(signature);
        } catch (Exception e) {
            throw new ExchangeException("Failed to sign request for user-balance", e);
        }

        return authenticatedApi.userBalance(request);
    }

    /**
     * Corresponds to private/get-positions
     * @param instrumentName (optional) e.g., BTCUSD-PERP. If null, fetches all positions.
     */
    public CryptoComResponse<CryptoComPositionResponse> getCryptoComPositions(String instrumentName) throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(nonceFactory.createValue());
        request.setMethod("private/get-positions");
        request.setApiKey(apiKey);
        request.setNonce(nonceFactory.createValue());

        ObjectNode params = JsonNodeFactory.instance.objectNode();
        if (instrumentName != null && !instrumentName.isEmpty()) {
            params.put("instrument_name", instrumentName);
        }
        request.setParams(params);

        try {
            String tempRequestBody = objectMapper.writeValueAsString(request);
            RestInvocationParamsรีวิวparamsForSigning = RestInvocationParams.create(null, null, tempRequestBody, null, null, null, null, null, null, null, null);
            String signature = signatureCreator.digestParams(paramsForSigning);
            request.setSig(signature);
        } catch (Exception e) {
            throw new ExchangeException("Failed to sign request for get-positions", e);
        }

        return authenticatedApi.getPositions(request);
    }

    // TODO: Add other raw methods for AccountService if needed, e.g.,
    // - getDepositAddress
    // - getWithdrawalHistory
    // - requestWithdrawal
    // - getFundingHistory (private/get-transactions)
    // Each will follow a similar pattern of creating CryptoComRequest, signing, and calling authenticatedApi.
}

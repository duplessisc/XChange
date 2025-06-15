package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.apache.commons.codec.binary.Hex;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class CryptoComAuthenticatedStreamingService extends CryptoComStreamingService {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComAuthenticatedStreamingService.class);
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

    private final String apiKey;
    private final String secretKey;

    public CryptoComAuthenticatedStreamingService(String apiUrl, String apiKey, String secretKey) {
        super(apiUrl, apiKey, secretKey); // Pass keys to parent for storage
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    private String calculateSignature(CryptoComRequest request) throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        String paramsString = "";
        if (request.getParams() != null) {
            // Crypto.com requires params to be sorted alphabetically by key and concatenated
            // The DTO structure needs to be considered. If params is a Map:
            if (request.getParams() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> paramsMap = (Map<String, Object>) request.getParams();
                paramsString = getParamsString(new TreeMap<>(paramsMap)); // TreeMap sorts keys
            } else if (request.getParams() instanceof JsonNode) {
                // If params is already a JsonNode (e.g. ObjectNode from parent)
                paramsString = getParamsStringFromObjectNode((ObjectNode) request.getParams());
            } else {
                // Fallback or error if params type is unexpected
                LOG.warn("Unsupported params type for signature calculation: {}", request.getParams().getClass().getName());
            }
        }

        String sigPayload = request.getMethod() +
                            request.getId() +
                            apiKey +
                            paramsString +
                            request.getNonce();

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_spec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_spec);

        return Hex.encodeHexString(sha256_HMAC.doFinal(sigPayload.getBytes(StandardCharsets.UTF_8)));
    }

    // Helper for Map params, adapted from Crypto.com's Java example
    private String getParamsString(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append(entry.getKey());
            if (entry.getValue() == null) {
                 // According to some exchange docs, nulls might be string "null" or skipped.
                 // Crypto.com example does not show nulls, assuming they are skipped or stringified if present.
                 // For safety, let's skip if null, or use "null" if that's what their example implies for other cases.
                 // The provided Crypto.com examples mostly deal with non-null simple values or nested objects.
            } else if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();
                sb.append(getParamsString(new TreeMap<>(nestedMap))); // Recursive call for nested sorted maps
            } else if (entry.getValue() instanceof JsonNode && ((JsonNode)entry.getValue()).isObject()){
                sb.append(getParamsStringFromObjectNode((ObjectNode) entry.getValue()));
            }
            else if (entry.getValue() instanceof JsonNode && ((JsonNode)entry.getValue()).isArray()){
                 // TODO: Handle array parameters if Crypto.com API uses them in signed requests
                 // Arrays need specific stringification rules (e.g. just concat values)
                 // Crypto.com example shows array values concatenated directly.
                 sb.append(entry.getValue().toString()); // simplified, might need refinement
            }
            else {
                sb.append(entry.getValue().toString());
            }
        }
        return sb.toString();
    }


    // Helper for ObjectNode params
    private String getParamsStringFromObjectNode(ObjectNode paramsNode) {
        if (paramsNode == null || paramsNode.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // TreeMap to sort keys alphabetically
        Map<String, Object> paramsMap = objectMapper.convertValue(paramsNode, Map.class);
        TreeMap<String, Object> sortedParamsMap = new TreeMap<>(paramsMap);

        for (Map.Entry<String, Object> entry : sortedParamsMap.entrySet()) {
            sb.append(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map) {
                 sb.append(getParamsString(new TreeMap<>((Map<String,Object>)value)));
            } else if (value instanceof JsonNode && ((JsonNode) value).isObject()) {
                 sb.append(getParamsStringFromObjectNode((ObjectNode) value));
            } else if (value instanceof JsonNode && ((JsonNode) value).isArray()) {
                // Crypto.com's array stringification seems to be just concatenating values.
                // This part needs careful validation against their examples if arrays are used in signed params.
                JsonNode arrayNode = (JsonNode) value;
                for (JsonNode elementNode : arrayNode) {
                    // This simplistic approach might not cover all edge cases for arrays of complex objects.
                    if (elementNode.isObject()) {
                        sb.append(getParamsStringFromObjectNode((ObjectNode) elementNode));
                    } else {
                        sb.append(elementNode.asText());
                    }
                }
            } else if (value != null) {
                sb.append(value.toString());
            }
            // Nulls are generally skipped or handled as empty strings in param strings if not explicitly defined.
        }
        return sb.toString();
    }


    private boolean isPrivateChannel(String channelName) {
        // Channels like "user.order", "user.balance" are private
        return channelName != null && channelName.startsWith("user.");
    }

    private CryptoComRequest signRequest(CryptoComRequest request) {
        if (apiKey == null || secretKey == null) {
            LOG.warn("API key or secret key is null. Cannot sign request for method: {}", request.getMethod());
            return request; // Or throw exception
        }
        request.setApiKey(apiKey);
        try {
            request.setSig(calculateSignature(request));
        } catch (NoSuchAlgorithmException | InvalidKeyException | JsonProcessingException e) {
            LOG.error("Failed to sign request: ", e);
            // Handle error appropriately, maybe throw a runtime exception
        }
        return request;
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(requestCounter.getAndIncrement()); // Use AtomicLong from parent or re-declare
        request.setMethod("subscribe");
        request.setNonce(System.currentTimeMillis());

        ObjectNode params = objectMapper.createObjectNode();
        // Assuming channelName is the full channel string like "book.BTC_USDT.10" or "user.order.ETH_CRO"
        params.putArray("channels").add(channelName);
        if (args != null && args.length > 0) {
            // Handle additional arguments for subscription if Crypto.com API requires them in params
            // For example, book_subscription_type, book_update_frequency for book.instrument_name.depth
            if (channelName.startsWith("book.") && args.length >= 2) {
                 params.put("book_subscription_type", args[0].toString());
                 params.put("book_update_frequency", Integer.parseInt(args[1].toString()));
            }
        }
        request.setParams(params);

        if (isPrivateChannel(channelName)) {
            signRequest(request);
        }
        return objectMapper.writeValueAsString(request);
    }

    @Override
    public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(requestCounter.getAndIncrement());
        request.setMethod("unsubscribe");
        request.setNonce(System.currentTimeMillis());

        ObjectNode params = objectMapper.createObjectNode();
        params.putArray("channels").add(channelName);
        request.setParams(params);

        // Unsubscribe might also need signing for private channels, check docs
        if (isPrivateChannel(channelName)) {
            signRequest(request);
        }
        return objectMapper.writeValueAsString(request);
    }

    @Override
    public String getAuthenticateMessage() throws IOException {
        if (apiKey == null || secretKey == null) {
            throw new IllegalStateException("API key and secret key required for authentication");
        }
        CryptoComRequest request = new CryptoComRequest();
        request.setId(requestCounter.getAndIncrement());
        request.setMethod("public/auth");
        request.setNonce(System.currentTimeMillis());
        // Params for public/auth is an empty object or null according to some examples
        // request.setParams(objectMapper.createObjectNode()); // Or null, check docs

        signRequest(request); // This will set apiKey and sig
        return objectMapper.writeValueAsString(request);
    }
}

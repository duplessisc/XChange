package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Hex;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest; // Reusing the DTO
import org.knowm.xchange.service.칡HttpRequestInvocation; // Correct ResCU import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.RestInvocationParams;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class CryptoComDigest implements ParamsDigest {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComDigest.class);
    private final String secretKey;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For parsing request body if needed

    public CryptoComDigest(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String digestParams(RestInvocationParams params) {
        // For Crypto.com, the signature is part of the JSON request body.
        // The ResCU framework typically uses ParamsDigest to generate a signature
        // that is then added as a header or query parameter.
        // Here, we are generating the signature that must be manually inserted
        // into the request body DTO *before* it's serialized by ResCU.
        // This digestParams method will be called by a custom interceptor or directly
        // in the raw service method to get the signature.

        String requestBodyString = params.getRequestBody();
        if (requestBodyString == null) {
            LOG.error("Request body is null, cannot generate Crypto.com signature.");
            // Or throw an exception, depending on how it's used.
            // For now, returning null or empty might cause issues downstream if not handled.
            throw new IllegalArgumentException("Request body is null for Crypto.com signature generation.");
        }

        try {
            // Parse the request body string into our CryptoComRequest DTO
            // or a JsonNode to extract necessary fields.
            JsonNode requestNode = objectMapper.readTree(requestBodyString);

            String method = requestNode.path("method").asText();
            long id = requestNode.path("id").asLong();
            String apiKey = requestNode.path("api_key").asText(); // api_key should be in the body
            long nonce = requestNode.path("nonce").asLong();
            JsonNode paramsNode = requestNode.path("params");

            String paramsString = "";
            if (paramsNode != null && !paramsNode.isNull() && paramsNode.isObject()) {
                paramsString = getParamsStringFromObjectNode((ObjectNode) paramsNode);
            }

            String sigPayload = method + id + apiKey + paramsString + nonce;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_spec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_spec);

            return Hex.encodeHexString(sha256_HMAC.doFinal(sigPayload.getBytes(StandardCharsets.UTF_8)));

        } catch (IOException e) {
            LOG.error("Failed to parse request body for signature generation: {}", e.getMessage());
            throw new RuntimeException("Failed to parse request body for signature", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            LOG.error("Failed to generate HMAC-SHA256 signature: {}", e.getMessage());
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Helper to convert the 'params' object (JsonNode) into the sorted, concatenated string.
     * This logic should be identical to the one used in CryptoComAuthenticatedStreamingService.
     * Adapted from CryptoComAuthenticatedStreamingService.
     */
    private String getParamsStringFromObjectNode(ObjectNode paramsNode) {
        if (paramsNode == null || paramsNode.isEmpty() || paramsNode.isNull()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // Use TreeMap to sort keys alphabetically, converting from JsonNode
        @SuppressWarnings("unchecked")
        Map<String, Object> paramsMap = objectMapper.convertValue(paramsNode, Map.class);
        TreeMap<String, Object> sortedParamsMap = new TreeMap<>(paramsMap);

        for (Map.Entry<String, Object> entry : sortedParamsMap.entrySet()) {
            sb.append(entry.getKey());
            Object value = entry.getValue();
            // Recursive stringification for nested objects/arrays
            sb.append(recursiveParamStringify(value));
        }
        return sb.toString();
    }

    private String recursiveParamStringify(Object value) {
        if (value == null) {
            return ""; // Or "null" if API expects that, Crypto.com examples usually omit nulls
        }
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            StringBuilder sb = new StringBuilder();
            TreeMap<String, Object> sortedMap = new TreeMap<>(map); // Sort nested map keys
            for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                sb.append(entry.getKey());
                sb.append(recursiveParamStringify(entry.getValue()));
            }
            return sb.toString();
        } else if (value instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> list = (java.util.List<Object>) value;
            StringBuilder sb = new StringBuilder();
            for (Object item : list) {
                sb.append(recursiveParamStringify(item));
            }
            return sb.toString();
        } else {
            return value.toString();
        }
    }
}

package org.knowm.xchange.cryptocom.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CryptoComRequest {

    @JsonProperty("id")
    private long id;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private JsonNode params; // Using JsonNode for flexibility, can be ObjectNode

    @JsonProperty("nonce")
    private long nonce;

    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("sig")
    private String sig;

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public JsonNode getParams() {
        return params;
    }

    public void setParams(JsonNode params) {
        this.params = params;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSig() {
        return sig;
    }

    public void setSig(String sig) {
        this.sig = sig;
    }

    @Override
    public String toString() {
        return "CryptoComRequest{" +
               "id=" + id +
               ", method='" + method + '\'' +
               ", params=" + params +
               ", nonce=" + nonce +
               (apiKey != null ? ", apiKey='" + apiKey + '\'' : "") +
               (sig != null ? ", sig='" + sig + '\'' : "") +
               '}';
    }
}

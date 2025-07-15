package org.knowm.xchange.cryptocom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComResponse<T> {

    @JsonProperty("id")
    private long id;

    @JsonProperty("method")
    private String method;

    @JsonProperty("code")
    private int code; // 0 for success

    @JsonProperty("message")
    private String message; // Optional, for server or error messages

    @JsonProperty("original")
    private String original; // Optional, original request for error cases

    @JsonProperty("result")
    private T result;

    // Getters
    public long getId() {
        return id;
    }

    public String getMethod() {
        return method;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getOriginal() {
        return original;
    }

    public T getResult() {
        return result;
    }

    // Setters (useful for testing or manual construction)
    public void setId(long id) {
        this.id = id;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "CryptoComResponse{" +
               "id=" + id +
               ", method='" + method + '\'' +
               ", code=" + code +
               (message != null ? ", message='" + message + '\'' : "") +
               (original != null ? ", original='" + original + '\'' : "") +
               ", result=" + result +
               '}';
    }
}

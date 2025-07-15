package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Reusing CryptoComPositionEvent from streaming DTOs
// import org.knowm.xchange.cryptocom.dto.account.CryptoComPositionEvent;

import java.util.List;

/**
 * Represents the "result" field of a private/get-positions API call.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComPositionResponse {

    @JsonProperty("data")
    private List<CryptoComPositionEvent> positions;

    // Getter
    public List<CryptoComPositionEvent> getPositions() {
        return positions;
    }

    // Setter
    public void setPositions(List<CryptoComPositionEvent> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return "CryptoComPositionResponse{" +
               "positions_count=" + (positions != null ? positions.size() : "null") +
               '}';
    }
}

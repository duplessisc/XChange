package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents the "result" field of a private/user-balance API call.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComAccountBalanceResponse {

    @JsonProperty("data")
    private List<CryptoComUserBalanceDetail> balanceDetails; // Typically a list with one entry

    // Getter
    public List<CryptoComUserBalanceDetail> getBalanceDetails() {
        return balanceDetails;
    }

    /**
     * Helper to get the primary balance detail object, as "data" usually contains a single element.
     * @return The first CryptoComUserBalanceDetail object, or null if the list is empty.
     */
    public CryptoComUserBalanceDetail getPrimaryBalanceDetail() {
        if (balanceDetails != null && !balanceDetails.isEmpty()) {
            return balanceDetails.get(0);
        }
        return null;
    }

    // Setter
    public void setBalanceDetails(List<CryptoComUserBalanceDetail> balanceDetails) {
        this.balanceDetails = balanceDetails;
    }

    @Override
    public String toString() {
        return "CryptoComAccountBalanceResponse{" +
               "balanceDetail=" + getPrimaryBalanceDetail() +
               '}';
    }
}

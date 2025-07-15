package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComInstrumentsResponse {

    @JsonProperty("data")
    private List<CryptoComInstrument> instruments;

    // Getter
    public List<CryptoComInstrument> getInstruments() {
        return instruments;
    }

    // Setter
    public void setInstruments(List<CryptoComInstrument> instruments) {
        this.instruments = instruments;
    }

    @Override
    public String toString() {
        return "CryptoComInstrumentsResponse{" +
               "instruments_count=" + (instruments != null ? instruments.size() : "null") +
               '}';
    }
}

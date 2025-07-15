package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComInstrument {

    @JsonProperty("symbol")
    private String symbol; // e.g., BTCUSD-PERP

    @JsonProperty("inst_type")
    private String instType; // e.g., PERPETUAL_SWAP, SPOT, FUTURE, OPTION

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("base_ccy")
    private String baseCcy;

    @JsonProperty("quote_ccy")
    private String quoteCcy;

    @JsonProperty("quote_decimals")
    private int quoteDecimals;

    @JsonProperty("quantity_decimals")
    private int quantityDecimals;

    @JsonProperty("price_tick_size")
    private BigDecimal priceTickSize;

    @JsonProperty("qty_tick_size")
    private BigDecimal qtyTickSize;

    @JsonProperty("max_leverage")
    private String maxLeverage; // Can be string like "50" or "50x"

    @JsonProperty("tradable")
    private boolean tradable;

    @JsonProperty("expiry_timestamp_ms")
    private Long expiryTimestampMs; // Nullable

    @JsonProperty("underlying_symbol")
    private String underlyingSymbol; // Nullable

    // Getters
    public String getSymbol() { return symbol; }
    public String getInstType() { return instType; }
    public String getDisplayName() { return displayName; }
    public String getBaseCcy() { return baseCcy; }
    public String getQuoteCcy() { return quoteCcy; }
    public int getQuoteDecimals() { return quoteDecimals; }
    public int getQuantityDecimals() { return quantityDecimals; }
    public BigDecimal getPriceTickSize() { return priceTickSize; }
    public BigDecimal getQtyTickSize() { return qtyTickSize; }
    public String getMaxLeverage() { return maxLeverage; }
    public boolean isTradable() { return tradable; }
    public Long getExpiryTimestampMs() { return expiryTimestampMs; }
    public String getUnderlyingSymbol() { return underlyingSymbol; }

    // Setters (optional)
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setInstType(String instType) { this.instType = instType; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setBaseCcy(String baseCcy) { this.baseCcy = baseCcy; }
    public void setQuoteCcy(String quoteCcy) { this.quoteCcy = quoteCcy; }
    public void setQuoteDecimals(int quoteDecimals) { this.quoteDecimals = quoteDecimals; }
    public void setQuantityDecimals(int quantityDecimals) { this.quantityDecimals = quantityDecimals; }
    public void setPriceTickSize(BigDecimal priceTickSize) { this.priceTickSize = priceTickSize; }
    public void setQtyTickSize(BigDecimal qtyTickSize) { this.qtyTickSize = qtyTickSize; }
    public void setMaxLeverage(String maxLeverage) { this.maxLeverage = maxLeverage; }
    public void setTradable(boolean tradable) { this.tradable = tradable; }
    public void setExpiryTimestampMs(Long expiryTimestampMs) { this.expiryTimestampMs = expiryTimestampMs; }
    public void setUnderlyingSymbol(String underlyingSymbol) { this.underlyingSymbol = underlyingSymbol; }

    @Override
    public String toString() {
        return "CryptoComInstrument{" +
               "symbol='" + symbol + '\'' +
               ", instType='" + instType + '\'' +
               ", tradable=" + tradable +
               '}';
    }
}

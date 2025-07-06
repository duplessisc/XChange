package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComPositionEvent {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("type") // e.g., PERPETUAL_SWAP, SPOT
    private String type;

    @JsonProperty("quantity")
    private BigDecimal quantity; // Position quantity (can be negative for shorts)

    @JsonProperty("cost") // Position cost or value in USD
    private BigDecimal cost;

    @JsonProperty("open_position_pnl")
    private BigDecimal openPositionPnl;

    @JsonProperty("session_unrealized_pnl") // From user.positions channel
    private BigDecimal sessionUnrealizedPnl;

    @JsonProperty("open_pos_cost") // From user.positions channel
    private BigDecimal openPosCost;

    @JsonProperty("session_pnl") // From user.positions channel
    private BigDecimal sessionPnl;

    @JsonProperty("pos_initial_margin") // From user.positions channel
    private BigDecimal posInitialMargin;

    @JsonProperty("pos_maintenance_margin") // From user.positions channel
    private BigDecimal posMaintenanceMargin;

    @JsonProperty("market_value") // From user.positions channel
    private BigDecimal marketValue;

    @JsonProperty("mark_price")
    private BigDecimal markPrice;

    @JsonProperty("target_leverage") // From user.positions channel
    private BigDecimal targetLeverage;


    @JsonProperty("update_timestamp_ms")
    private long updateTimestampMs;

    // Getters
    public String getAccountId() { return accountId; }
    public String getInstrumentName() { return instrumentName; }
    public String getType() { return type; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getCost() { return cost; }
    public BigDecimal getOpenPositionPnl() { return openPositionPnl; }
    public BigDecimal getSessionUnrealizedPnl() { return sessionUnrealizedPnl; }
    public BigDecimal getOpenPosCost() { return openPosCost; }
    public BigDecimal getSessionPnl() { return sessionPnl; }
    public BigDecimal getPosInitialMargin() { return posInitialMargin; }
    public BigDecimal getPosMaintenanceMargin() { return posMaintenanceMargin; }
    public BigDecimal getMarketValue() { return marketValue; }
    public BigDecimal getMarkPrice() { return markPrice; }
    public BigDecimal getTargetLeverage() { return targetLeverage; }
    public long getUpdateTimestampMs() { return updateTimestampMs; }

    // Setters
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public void setType(String type) { this.type = type; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public void setOpenPositionPnl(BigDecimal openPositionPnl) { this.openPositionPnl = openPositionPnl; }
    public void setSessionUnrealizedPnl(BigDecimal sessionUnrealizedPnl) { this.sessionUnrealizedPnl = sessionUnrealizedPnl; }
    public void setOpenPosCost(BigDecimal openPosCost) { this.openPosCost = openPosCost; }
    public void setSessionPnl(BigDecimal sessionPnl) { this.sessionPnl = sessionPnl; }
    public void setPosInitialMargin(BigDecimal posInitialMargin) { this.posInitialMargin = posInitialMargin; }
    public void setPosMaintenanceMargin(BigDecimal posMaintenanceMargin) { this.posMaintenanceMargin = posMaintenanceMargin; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    public void setMarkPrice(BigDecimal markPrice) { this.markPrice = markPrice; }
    public void setTargetLeverage(BigDecimal targetLeverage) { this.targetLeverage = targetLeverage; }
    public void setUpdateTimestampMs(long updateTimestampMs) { this.updateTimestampMs = updateTimestampMs; }

    @Override
    public String toString() {
        return "CryptoComPositionEvent{" +
               "instrumentName='" + instrumentName + '\'' +
               ", quantity=" + quantity +
               ", cost=" + cost +
               ", markPrice=" + markPrice +
               ", openPositionPnl=" + openPositionPnl +
               ", updateTimestampMs=" + updateTimestampMs +
               '}';
    }
}

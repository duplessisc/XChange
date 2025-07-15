package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
// Assuming CryptoComBalanceEvent is already created (for elements of position_balances)
// import org.knowm.xchange.cryptocom.dto.account.CryptoComBalanceEvent;


import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComUserBalanceDetail {

    @JsonProperty("total_available_balance")
    private BigDecimal totalAvailableBalance;

    @JsonProperty("total_margin_balance")
    private BigDecimal totalMarginBalance;

    @JsonProperty("total_initial_margin")
    private BigDecimal totalInitialMargin;

    @JsonProperty("total_position_im")
    private BigDecimal totalPositionIm;

    @JsonProperty("total_haircut")
    private BigDecimal totalHaircut;

    @JsonProperty("total_maintenance_margin")
    private BigDecimal totalMaintenanceMargin;

    @JsonProperty("total_position_cost")
    private BigDecimal totalPositionCost;

    @JsonProperty("total_cash_balance")
    private BigDecimal totalCashBalance;

    @JsonProperty("total_collateral_value")
    private BigDecimal totalCollateralValue;

    @JsonProperty("total_session_unrealized_pnl")
    private BigDecimal totalSessionUnrealizedPnl;

    @JsonProperty("instrument_name") // e.g., "USD", the currency of these totals
    private String instrumentName;

    @JsonProperty("total_session_realized_pnl")
    private BigDecimal totalSessionRealizedPnl;

    @JsonProperty("is_liquidating")
    private boolean isLiquidating;

    @JsonProperty("total_effective_leverage")
    private BigDecimal totalEffectiveLeverage;

    @JsonProperty("position_limit")
    private BigDecimal positionLimit;

    @JsonProperty("used_position_limit")
    private BigDecimal usedPositionLimit;

    @JsonProperty("position_balances")
    private List<CryptoComBalanceEvent> positionBalances; // Reusing the DTO for individual balances

    // Getters
    public BigDecimal getTotalAvailableBalance() { return totalAvailableBalance; }
    public BigDecimal getTotalMarginBalance() { return totalMarginBalance; }
    public BigDecimal getTotalInitialMargin() { return totalInitialMargin; }
    public BigDecimal getTotalPositionIm() { return totalPositionIm; }
    public BigDecimal getTotalHaircut() { return totalHaircut; }
    public BigDecimal getTotalMaintenanceMargin() { return totalMaintenanceMargin; }
    public BigDecimal getTotalPositionCost() { return totalPositionCost; }
    public BigDecimal getTotalCashBalance() { return totalCashBalance; }
    public BigDecimal getTotalCollateralValue() { return totalCollateralValue; }
    public BigDecimal getTotalSessionUnrealizedPnl() { return totalSessionUnrealizedPnl; }
    public String getInstrumentName() { return instrumentName; }
    public BigDecimal getTotalSessionRealizedPnl() { return totalSessionRealizedPnl; }
    public boolean isLiquidating() { return isLiquidating; }
    public BigDecimal getTotalEffectiveLeverage() { return totalEffectiveLeverage; }
    public BigDecimal getPositionLimit() { return positionLimit; }
    public BigDecimal getUsedPositionLimit() { return usedPositionLimit; }
    public List<CryptoComBalanceEvent> getPositionBalances() { return positionBalances; }

    // Setters (optional)
    public void setTotalAvailableBalance(BigDecimal totalAvailableBalance) { this.totalAvailableBalance = totalAvailableBalance; }
    public void setTotalMarginBalance(BigDecimal totalMarginBalance) { this.totalMarginBalance = totalMarginBalance; }
    public void setTotalInitialMargin(BigDecimal totalInitialMargin) { this.totalInitialMargin = totalInitialMargin; }
    public void setTotalPositionIm(BigDecimal totalPositionIm) { this.totalPositionIm = totalPositionIm; }
    public void setTotalHaircut(BigDecimal totalHaircut) { this.totalHaircut = totalHaircut; }
    public void setTotalMaintenanceMargin(BigDecimal totalMaintenanceMargin) { this.totalMaintenanceMargin = totalMaintenanceMargin; }
    public void setTotalPositionCost(BigDecimal totalPositionCost) { this.totalPositionCost = totalPositionCost; }
    public void setTotalCashBalance(BigDecimal totalCashBalance) { this.totalCashBalance = totalCashBalance; }
    public void setTotalCollateralValue(BigDecimal totalCollateralValue) { this.totalCollateralValue = totalCollateralValue; }
    public void setTotalSessionUnrealizedPnl(BigDecimal totalSessionUnrealizedPnl) { this.totalSessionUnrealizedPnl = totalSessionUnrealizedPnl; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public void setTotalSessionRealizedPnl(BigDecimal totalSessionRealizedPnl) { this.totalSessionRealizedPnl = totalSessionRealizedPnl; }
    public void setLiquidating(boolean liquidating) { isLiquidating = liquidating; }
    public void setTotalEffectiveLeverage(BigDecimal totalEffectiveLeverage) { this.totalEffectiveLeverage = totalEffectiveLeverage; }
    public void setPositionLimit(BigDecimal positionLimit) { this.positionLimit = positionLimit; }
    public void setUsedPositionLimit(BigDecimal usedPositionLimit) { this.usedPositionLimit = usedPositionLimit; }
    public void setPositionBalances(List<CryptoComBalanceEvent> positionBalances) { this.positionBalances = positionBalances; }
}

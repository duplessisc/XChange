package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComBalanceEvent {

    // These fields are from the "position_balances" array elements within the "user.balance" channel data
    @JsonProperty("instrument_name")
    private String instrumentName; // This is the currency code, e.g., "CRO", "USD"

    @JsonProperty("quantity")
    private BigDecimal quantity; // Total quantity of this currency

    @JsonProperty("market_value")
    private BigDecimal marketValue; // Market value of this quantity

    @JsonProperty("collateral_eligible")
    private boolean collateralEligible;

    @JsonProperty("haircut")
    private BigDecimal haircut; // Haircut rate for this collateral

    @JsonProperty("collateral_amount")
    private BigDecimal collateralAmount; // Amount usable as collateral (market_value - haircut_value)

    @JsonProperty("max_withdrawal_balance")
    private BigDecimal maxWithdrawalBalance; // Max amount that can be withdrawn

    @JsonProperty("reserved_qty")
    private BigDecimal reservedQty; // Quantity reserved for open orders or other holds

    // Getters
    public String getInstrumentName() {
        return instrumentName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public boolean isCollateralEligible() {
        return collateralEligible;
    }

    public BigDecimal getHaircut() {
        return haircut;
    }

    public BigDecimal getCollateralAmount() {
        return collateralAmount;
    }

    public BigDecimal getMaxWithdrawalBalance() {
        return maxWithdrawalBalance;
    }

    public BigDecimal getReservedQty() {
        return reservedQty;
    }

    // Setters
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public void setCollateralEligible(boolean collateralEligible) {
        this.collateralEligible = collateralEligible;
    }

    public void setHaircut(BigDecimal haircut) {
        this.haircut = haircut;
    }

    public void setCollateralAmount(BigDecimal collateralAmount) {
        this.collateralAmount = collateralAmount;
    }

    public void setMaxWithdrawalBalance(BigDecimal maxWithdrawalBalance) {
        this.maxWithdrawalBalance = maxWithdrawalBalance;
    }

    public void setReservedQty(BigDecimal reservedQty) {
        this.reservedQty = reservedQty;
    }

    @Override
    public String toString() {
        return "CryptoComBalanceEvent{" +
               "instrumentName='" + instrumentName + '\'' +
               ", quantity=" + quantity +
               ", marketValue=" + marketValue +
               ", collateralEligible=" + collateralEligible +
               ", collateralAmount=" + collateralAmount +
               ", reservedQty=" + reservedQty +
               '}';
    }
}

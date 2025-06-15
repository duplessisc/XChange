package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComUserOrderEvent {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("client_oid")
    private String clientOid;

    @JsonProperty("order_type") // API uses "order_type", will map to "type" in XChange Order
    private String type;

    @JsonProperty("time_in_force")
    private String timeInForce;

    @JsonProperty("side")
    private String side;

    @JsonProperty("exec_inst")
    private List<String> execInst;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("price") // API uses "price" for limit price
    private BigDecimal limitPrice;

    @JsonProperty("order_value")
    private BigDecimal orderValue;

    @JsonProperty("maker_fee_rate")
    private BigDecimal makerFeeRate;

    @JsonProperty("taker_fee_rate")
    private BigDecimal takerFeeRate;

    @JsonProperty("avg_price")
    private BigDecimal averagePrice;

    @JsonProperty("cumulative_quantity")
    private BigDecimal cumulativeQuantity;

    @JsonProperty("cumulative_value")
    private BigDecimal cumulativeValue;

    @JsonProperty("cumulative_fee")
    private BigDecimal cumulativeFee;

    @JsonProperty("status")
    private String status;

    @JsonProperty("update_user_id")
    private String updateUserId;

    @JsonProperty("order_date")
    private String orderDate; // Date string "YYYY-MM-DD"

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("fee_instrument_name")
    private String feeInstrumentName;

    @JsonProperty("reason") // reason code for REJECTED, CANCELED etc.
    private String reason;


    @JsonProperty("create_time")
    private long createTime; // Milliseconds

    @JsonProperty("create_time_ns")
    private String createTimeNs; // Nanoseconds as string

    @JsonProperty("update_time")
    private long updateTime; // Milliseconds

    @JsonProperty("transaction_time_ns") // Order transaction timestamp (nanosecond)
    private String transactionTimeNs;


    // Getters
    public String getAccountId() { return accountId; }
    public String getOrderId() { return orderId; }
    public String getClientOid() { return clientOid; }
    public String getType() { return type; }
    public String getTimeInForce() { return timeInForce; }
    public String getSide() { return side; }
    public List<String> getExecInst() { return execInst; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getLimitPrice() { return limitPrice; }
    public BigDecimal getOrderValue() { return orderValue; }
    public BigDecimal getMakerFeeRate() { return makerFeeRate; }
    public BigDecimal getTakerFeeRate() { return takerFeeRate; }
    public BigDecimal getAveragePrice() { return averagePrice; }
    public BigDecimal getCumulativeQuantity() { return cumulativeQuantity; }
    public BigDecimal getCumulativeValue() { return cumulativeValue; }
    public BigDecimal getCumulativeFee() { return cumulativeFee; }
    public String getStatus() { return status; }
    public String getUpdateUserId() { return updateUserId; }
    public String getOrderDate() { return orderDate; }
    public String getInstrumentName() { return instrumentName; }
    public String getFeeInstrumentName() { return feeInstrumentName; }
    public String getReason() { return reason; }
    public long getCreateTime() { return createTime; }
    public String getCreateTimeNs() { return createTimeNs; }
    public long getUpdateTime() { return updateTime; }
    public String getTransactionTimeNs() { return transactionTimeNs; }

    // Setters (optional, depending on usage pattern)
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setClientOid(String clientOid) { this.clientOid = clientOid; }
    public void setType(String type) { this.type = type; }
    public void setTimeInForce(String timeInForce) { this.timeInForce = timeInForce; }
    public void setSide(String side) { this.side = side; }
    public void setExecInst(List<String> execInst) { this.execInst = execInst; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public void setLimitPrice(BigDecimal limitPrice) { this.limitPrice = limitPrice; }
    public void setOrderValue(BigDecimal orderValue) { this.orderValue = orderValue; }
    public void setMakerFeeRate(BigDecimal makerFeeRate) { this.makerFeeRate = makerFeeRate; }
    public void setTakerFeeRate(BigDecimal takerFeeRate) { this.takerFeeRate = takerFeeRate; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }
    public void setCumulativeQuantity(BigDecimal cumulativeQuantity) { this.cumulativeQuantity = cumulativeQuantity; }
    public void setCumulativeValue(BigDecimal cumulativeValue) { this.cumulativeValue = cumulativeValue; }
    public void setCumulativeFee(BigDecimal cumulativeFee) { this.cumulativeFee = cumulativeFee; }
    public void setStatus(String status) { this.status = status; }
    public void setUpdateUserId(String updateUserId) { this.updateUserId = updateUserId; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public void setFeeInstrumentName(String feeInstrumentName) { this.feeInstrumentName = feeInstrumentName; }
    public void setReason(String reason) { this.reason = reason; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public void setCreateTimeNs(String createTimeNs) { this.createTimeNs = createTimeNs; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
    public void setTransactionTimeNs(String transactionTimeNs) { this.transactionTimeNs = transactionTimeNs; }

    @Override
    public String toString() {
        return "CryptoComUserOrderEvent{" +
               "orderId='" + orderId + '\'' +
               ", instrumentName='" + instrumentName + '\'' +
               ", status='" + status + '\'' +
               ", side='" + side + '\'' +
               ", type='" + type + '\'' +
               ", quantity=" + quantity +
               ", limitPrice=" + limitPrice +
               ", cumulativeQuantity=" + cumulativeQuantity +
               ", createTime=" + createTime +
               '}';
    }
}

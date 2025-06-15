package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoComUserTradeEvent {

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("event_date") // Not typically in XChange UserTrade, but available
    private String eventDate;

    @JsonProperty("journal_type") // Should be "TRADING"
    private String journalType;

    @JsonProperty("traded_quantity")
    private BigDecimal tradedQuantity;

    @JsonProperty("traded_price")
    private BigDecimal tradedPrice;

    @JsonProperty("fees")
    private BigDecimal fees;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("trade_id")
    private String tradeId;

    @JsonProperty("trade_match_id")
    private String tradeMatchId;

    @JsonProperty("client_oid")
    private String clientOid;

    @JsonProperty("taker_side") // MAKER or TAKER
    private String takerSide;

    @JsonProperty("side") // BUY or SELL
    private String side;

    @JsonProperty("instrument_name")
    private String instrumentName;

    @JsonProperty("fee_instrument_name")
    private String feeInstrumentName;

    @JsonProperty("create_time")
    private long createTime; // Milliseconds

    @JsonProperty("create_time_ns")
    private String createTimeNs; // Nanoseconds as string

    @JsonProperty("transaction_time") // API docs say "transaction_time" (nanosecond) for user.trade, but REST uses "transact_time_ns"
    private String transactionTime; // Nanoseconds as string, matches user.trade subscription example in docs

    @JsonProperty("match_count")
    private String matchCount;

    @JsonProperty("match_index")
    private String matchIndex;


    // Getters
    public String getAccountId() { return accountId; }
    public String getEventDate() { return eventDate; }
    public String getJournalType() { return journalType; }
    public BigDecimal getTradedQuantity() { return tradedQuantity; }
    public BigDecimal getTradedPrice() { return tradedPrice; }
    public BigDecimal getFees() { return fees; }
    public String getOrderId() { return orderId; }
    public String getTradeId() { return tradeId; }
    public String getTradeMatchId() { return tradeMatchId; }
    public String getClientOid() { return clientOid; }
    public String getTakerSide() { return takerSide; }
    public String getSide() { return side; }
    public String getInstrumentName() { return instrumentName; }
    public String getFeeInstrumentName() { return feeInstrumentName; }
    public long getCreateTime() { return createTime; }
    public String getCreateTimeNs() { return createTimeNs; }
    public String getTransactionTime() { return transactionTime; }
    public String getMatchCount() { return matchCount; }
    public String getMatchIndex() { return matchIndex; }

    // Setters
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setJournalType(String journalType) { this.journalType = journalType; }
    public void setTradedQuantity(BigDecimal tradedQuantity) { this.tradedQuantity = tradedQuantity; }
    public void setTradedPrice(BigDecimal tradedPrice) { this.tradedPrice = tradedPrice; }
    public void setFees(BigDecimal fees) { this.fees = fees; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setTradeId(String tradeId) { this.tradeId = tradeId; }
    public void setTradeMatchId(String tradeMatchId) { this.tradeMatchId = tradeMatchId; }
    public void setClientOid(String clientOid) { this.clientOid = clientOid; }
    public void setTakerSide(String takerSide) { this.takerSide = takerSide; }
    public void setSide(String side) { this.side = side; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public void setFeeInstrumentName(String feeInstrumentName) { this.feeInstrumentName = feeInstrumentName; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public void setCreateTimeNs(String createTimeNs) { this.createTimeNs = createTimeNs; }
    public void setTransactionTime(String transactionTime) { this.transactionTime = transactionTime; }
    public void setMatchCount(String matchCount) { this.matchCount = matchCount; }
    public void setMatchIndex(String matchIndex) { this.matchIndex = matchIndex; }

    @Override
    public String toString() {
        return "CryptoComUserTradeEvent{" +
               "tradeId='" + tradeId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", instrumentName='" + instrumentName + '\'' +
               ", side='" + side + '\'' +
               ", tradedPrice=" + tradedPrice +
               ", tradedQuantity=" + tradedQuantity +
               ", fees=" + fees +
               ", createTime=" + createTime +
               '}';
    }
}

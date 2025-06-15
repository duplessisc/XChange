package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"price", "quantity", "count"})
public class CryptoComOrderBookEntry {

    private final BigDecimal price;
    private final BigDecimal quantity;
    private final long count; // Number of orders

    @JsonCreator
    public CryptoComOrderBookEntry(BigDecimal price, BigDecimal quantity, long count) {
        this.price = price;
        this.quantity = quantity;
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "CryptoComOrderBookEntry{" +
               "price=" + price +
               ", quantity=" + quantity +
               ", count=" + count +
               '}';
    }
}

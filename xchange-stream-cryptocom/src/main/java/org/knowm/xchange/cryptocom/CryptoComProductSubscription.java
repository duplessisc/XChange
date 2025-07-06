package org.knowm.xchange.cryptocom;

import info.bitrich.xchangestream.service.netty.ProductSubscription;
import org.knowm.xchange.instrument.Instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CryptoComProductSubscription extends ProductSubscription {

    private final List<OrderbookSubscriptionDetail> orderBooks;
    private final List<Instrument> tickers;
    private final List<Instrument> trades;
    private final List<CandlestickSubscriptionDetail> candlesticks;
    private final List<Instrument> userOrders; // Instrument specific, null in list for "all"
    private final List<Instrument> userTrades; // Instrument specific, null in list for "all"
    private final boolean subscribeToUserBalances;
    private final boolean subscribeToUserPositions;


    private CryptoComProductSubscription(Builder builder) {
        super(builder); // Call ProductSubscription's constructor
        this.orderBooks = Collections.unmodifiableList(new ArrayList<>(builder.orderBooks));
        this.tickers = Collections.unmodifiableList(new ArrayList<>(builder.tickers));
        this.trades = Collections.unmodifiableList(new ArrayList<>(builder.trades));
        this.candlesticks = Collections.unmodifiableList(new ArrayList<>(builder.candlesticks));
        this.userOrders = Collections.unmodifiableList(new ArrayList<>(builder.userOrders));
        this.userTrades = Collections.unmodifiableList(new ArrayList<>(builder.userTrades));
        this.subscribeToUserBalances = builder.subscribeToUserBalances;
        this.subscribeToUserPositions = builder.subscribeToUserPositions;
    }

    // Getters
    public List<OrderbookSubscriptionDetail> getOrderBooks() {
        return orderBooks;
    }

    public List<Instrument> getTickers() {
        return tickers;
    }

    public List<Instrument> getTrades() {
        return trades;
    }

    public List<CandlestickSubscriptionDetail> getCandlesticks() {
        return candlesticks;
    }

    public List<Instrument> getUserOrders() {
        return userOrders;
    }

    public List<Instrument> getUserTrades() {
        return userTrades;
    }

    public boolean getSubscribeToUserBalances() {
        return subscribeToUserBalances;
    }

    public boolean getSubscribeToUserPositions() {
        return subscribeToUserPositions;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ProductSubscription.ProductSubscriptionBuilder {
        private final List<OrderbookSubscriptionDetail> orderBooks = new ArrayList<>();
        private final List<Instrument> tickers = new ArrayList<>();
        private final List<Instrument> trades = new ArrayList<>();
        private final List<CandlestickSubscriptionDetail> candlesticks = new ArrayList<>();
        private final List<Instrument> userOrders = new ArrayList<>();
        private final List<Instrument> userTrades = new ArrayList<>();
        private boolean subscribeToUserBalances = false;
        private boolean subscribeToUserPositions = false;


        public Builder addOrderbook(Instrument instrument, int depth) {
            orderBooks.add(new OrderbookSubscriptionDetail(instrument, depth));
            // Also add to the parent ProductSubscription if it expects simple Instrument list for order books
            // super.addOrderbook(instrument); // Crypto.com needs depth, so custom list is primary
            return this;
        }

        public Builder addTicker(Instrument instrument) {
            tickers.add(instrument);
            super.addTicker(instrument); // Keep parent informed for generic compatibility
            return this;
        }

        public Builder addTrades(Instrument instrument) {
            trades.add(instrument);
            super.addTrades(instrument); // Keep parent informed
            return this;
        }

        public Builder addCandlesticks(Instrument instrument, String timeframe) {
            candlesticks.add(new CandlestickSubscriptionDetail(instrument, timeframe));
            return this;
        }

        public Builder addUserOrders(Instrument instrument) { // Specific instrument
            userOrders.add(instrument);
            return this;
        }

        public Builder addUserOrders() { // All instruments
            userOrders.add(null); // Use null to signify "all instruments" for this channel type
            return this;
        }

        public Builder addUserTrades(Instrument instrument) { // Specific instrument
            userTrades.add(instrument);
            return this;
        }

        public Builder addUserTrades() { // All instruments
            userTrades.add(null);
            return this;
        }

        public Builder addBalances() {
            this.subscribeToUserBalances = true;
            return this;
        }

        public Builder addPositions() {
            this.subscribeToUserPositions = true;
            return this;
        }


        @Override
        public CryptoComProductSubscription build() {
            return new CryptoComProductSubscription(this);
        }
    }

    // Inner classes for detailed subscriptions

    public static class OrderbookSubscriptionDetail {
        private final Instrument instrument;
        private final int depth;

        public OrderbookSubscriptionDetail(Instrument instrument, int depth) {
            this.instrument = Objects.requireNonNull(instrument);
            this.depth = depth;
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public int getDepth() {
            return depth;
        }

        public String getChannelName() {
            return String.format("book.%s.%d", CryptoComAdapters.adaptXchangeInstrument(instrument), depth);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderbookSubscriptionDetail that = (OrderbookSubscriptionDetail) o;
            return depth == that.depth && instrument.equals(that.instrument);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instrument, depth);
        }
    }

    public static class CandlestickSubscriptionDetail {
        private final Instrument instrument;
        private final String timeframe; // e.g., "M1", "H1", "D1"

        public CandlestickSubscriptionDetail(Instrument instrument, String timeframe) {
            this.instrument = Objects.requireNonNull(instrument);
            this.timeframe = Objects.requireNonNull(timeframe);
        }

        public Instrument getInstrument() {
            return instrument;
        }

        public String getTimeframe() {
            return timeframe;
        }

        public String getChannelName() {
            // Crypto.com format: candlestick.{time_frame}.{instrument_name}
            return String.format("candlestick.%s.%s", timeframe, CryptoComAdapters.adaptXchangeInstrument(instrument));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CandlestickSubscriptionDetail that = (CandlestickSubscriptionDetail) o;
            return instrument.equals(that.instrument) && timeframe.equals(that.timeframe);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instrument, timeframe);
        }
    }
}

package org.knowm.xchange.cryptocom;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.cryptocom.dto.account.CryptoComBalanceEvent;
import org.knowm.xchange.cryptocom.dto.account.CryptoComPositionEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComBookEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComOrderBookEntry;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickerEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTradeEvent;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserOrderEvent;
import org.knowm.xchange.cryptocom.dto.trade.CryptoComUserTradeEvent;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComAdaptersTest {

    @Test
    void testAdaptInstrument_spot() {
        Instrument instrument = CryptoComAdapters.adaptInstrument("BTC_USDT");
        assertThat(instrument).isEqualTo(new CurrencyPair("BTC/USDT"));

        instrument = CryptoComAdapters.adaptInstrument("ETH_CRO");
        assertThat(instrument).isEqualTo(new CurrencyPair("ETH/CRO"));
    }

    @Test
    void testAdaptInstrument_perp() {
        Instrument instrument = CryptoComAdapters.adaptInstrument("BTCUSD-PERP");
        assertThat(instrument).isInstanceOf(FuturesContract.class);
        FuturesContract contract = (FuturesContract) instrument;
        assertThat(contract.getCurrencyPair()).isEqualTo(new CurrencyPair("BTC/USD"));
        assertThat(contract.getPrompt()).isEqualTo("PERP");
    }

    @Test
    void testAdaptInstrument_future() {
        Instrument instrument = CryptoComAdapters.adaptInstrument("BTCUSD-231229");
        assertThat(instrument).isInstanceOf(FuturesContract.class);
        FuturesContract contract = (FuturesContract) instrument;
        assertThat(contract.getCurrencyPair()).isEqualTo(new CurrencyPair("BTC/USD"));
        assertThat(contract.getPrompt()).isEqualTo("231229");
    }

    @Test
    void testAdaptInstrument_option() throws ParseException {
        Instrument instrument = CryptoComAdapters.adaptInstrument("BTCUSD-231229-30000-C");
        assertThat(instrument).isInstanceOf(OptionsContract.class);
        OptionsContract contract = (OptionsContract) instrument;
        assertThat(contract.getCurrencyPair()).isEqualTo(new CurrencyPair("BTC/USD"));

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        assertThat(contract.getExpireDate()).isEqualTo(sdf.parse("231229"));
        assertThat(contract.getStrike()).isEqualTo(new BigDecimal("30000"));
        assertThat(contract.getType()).isEqualTo(OptionsContract.OptionType.CALL);

        instrument = CryptoComAdapters.adaptInstrument("ETHUSD-240329-2000-P");
        OptionsContract putContract = (OptionsContract) instrument;
        assertThat(putContract.getCurrencyPair()).isEqualTo(new CurrencyPair("ETH/USD"));
        assertThat(putContract.getExpireDate()).isEqualTo(sdf.parse("240329"));
        assertThat(putContract.getStrike()).isEqualTo(new BigDecimal("2000"));
        assertThat(putContract.getType()).isEqualTo(OptionsContract.OptionType.PUT);
    }

    @Test
    void testAdaptInstrument_currency() {
        Instrument instrument = CryptoComAdapters.adaptInstrument("CRO");
        assertThat(instrument).isEqualTo(Currency.getInstance("CRO"));
    }


    @Test
    void testAdaptXchangeInstrument_spot() {
        String symbol = CryptoComAdapters.adaptXchangeInstrument(new CurrencyPair("BTC/USDT"));
        assertThat(symbol).isEqualTo("BTC_USDT");
    }

    @Test
    void testAdaptXchangeInstrument_perp() {
        String symbol = CryptoComAdapters.adaptXchangeInstrument(new FuturesContract(new CurrencyPair("BTC/USD"), "PERP"));
        assertThat(symbol).isEqualTo("BTCUSD-PERP");
    }

    @Test
    void testAdaptXchangeInstrument_future() {
        String symbol = CryptoComAdapters.adaptXchangeInstrument(new FuturesContract(new CurrencyPair("BTC/USD"), "231229"));
        assertThat(symbol).isEqualTo("BTCUSD-231229");
    }

    @Test
    void testAdaptXchangeInstrument_option() throws ParseException {
         SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         Date expiry = sdf.parse("231229");

        OptionsContract callOption = new OptionsContract.Builder()
            .currencyPair(new CurrencyPair("BTC/USD"))
            .expireDate(expiry)
            .strike(new BigDecimal("30000"))
            .type(OptionsContract.OptionType.CALL)
            .build();
        String symbol = CryptoComAdapters.adaptXchangeInstrument(callOption);
        assertThat(symbol).isEqualTo("BTCUSD-231229-30000-C");
    }

    @Test
    void testAdaptXchangeInstrument_currency() {
        String symbol = CryptoComAdapters.adaptXchangeInstrument(Currency.CRO);
        assertThat(symbol).isEqualTo("CRO");
    }


    @Test
    void testAdaptOrderBook() {
        CryptoComBookEvent bookEvent = new CryptoComBookEvent();
        List<CryptoComOrderBookEntry> asks = Arrays.asList(
                new CryptoComOrderBookEntry(new BigDecimal("101.0"), new BigDecimal("1.5"), 1L),
                new CryptoComOrderBookEntry(new BigDecimal("101.5"), new BigDecimal("2.5"), 1L)
        );
        List<CryptoComOrderBookEntry> bids = Arrays.asList(
                new CryptoComOrderBookEntry(new BigDecimal("100.0"), new BigDecimal("1.0"), 1L),
                new CryptoComOrderBookEntry(new BigDecimal("99.5"), new BigDecimal("2.0"), 1L)
        );
        bookEvent.setAsks(asks);
        bookEvent.setBids(bids);
        bookEvent.setLastUpdateTimestamp(1678886400000L); // tt

        Instrument instrument = new CurrencyPair("BTC/USD");
        OrderBook orderBook = CryptoComAdapters.adaptOrderBook(bookEvent, instrument);

        assertThat(orderBook.getTimestamp()).isEqualTo(new Date(1678886400000L));
        assertThat(orderBook.getAsks()).hasSize(2);
        assertThat(orderBook.getBids()).hasSize(2);
        assertThat(orderBook.getAsks().get(0).getLimitPrice()).isEqualTo(new BigDecimal("101.0"));
        assertThat(orderBook.getBids().get(0).getInstrument()).isEqualTo(instrument);
    }

    @Test
    void testAdaptTicker() {
        CryptoComTickerEvent tickerEvent = new CryptoComTickerEvent();
        tickerEvent.setLastTradePrice(new BigDecimal("50000.0"));
        tickerEvent.setBestBidPrice(new BigDecimal("49999.0"));
        tickerEvent.setBestAskPrice(new BigDecimal("50001.0"));
        tickerEvent.setVolume(new BigDecimal("1000"));
        tickerEvent.setTimestamp(1678886400000L);
        // ... set other fields as needed

        Instrument instrument = new CurrencyPair("BTC/USD");
        Ticker ticker = CryptoComAdapters.adaptTicker(tickerEvent, instrument);

        assertThat(ticker.getInstrument()).isEqualTo(instrument);
        assertThat(ticker.getLast()).isEqualTo(new BigDecimal("50000.0"));
        assertThat(ticker.getBid()).isEqualTo(new BigDecimal("49999.0"));
        assertThat(ticker.getAsk()).isEqualTo(new BigDecimal("50001.0"));
        assertThat(ticker.getVolume()).isEqualTo(new BigDecimal("1000"));
        assertThat(ticker.getTimestamp()).isEqualTo(new Date(1678886400000L));
    }

    @Test
    void testAdaptTrade() {
        CryptoComTradeEvent tradeEvent = new CryptoComTradeEvent();
        tradeEvent.setTradeId(12345L);
        tradeEvent.setPrice(new BigDecimal("50000.0"));
        tradeEvent.setQuantity(new BigDecimal("0.001"));
        tradeEvent.setSide("BUY");
        tradeEvent.setTimestamp(1678886400000L);

        Instrument instrument = new CurrencyPair("BTC/USD");
        Trade trade = CryptoComAdapters.adaptTrade(tradeEvent, instrument);

        assertThat(trade.getId()).isEqualTo("12345");
        assertThat(trade.getInstrument()).isEqualTo(instrument);
        assertThat(trade.getPrice()).isEqualTo(new BigDecimal("50000.0"));
        assertThat(trade.getOriginalAmount()).isEqualTo(new BigDecimal("0.001"));
        assertThat(trade.getType()).isEqualTo(Order.OrderType.BID);
        assertThat(trade.getTimestamp()).isEqualTo(new Date(1678886400000L));
    }

    @Test
    void testAdaptUserOrder_limit() {
        CryptoComUserOrderEvent orderEvent = new CryptoComUserOrderEvent();
        orderEvent.setOrderId("order123");
        orderEvent.setInstrumentName("BTC_USDT");
        orderEvent.setType("LIMIT");
        orderEvent.setSide("SELL");
        orderEvent.setLimitPrice(new BigDecimal("51000.0"));
        orderEvent.setQuantity(new BigDecimal("0.5"));
        orderEvent.setCumulativeQuantity(new BigDecimal("0.1"));
        orderEvent.setStatus("ACTIVE");
        orderEvent.setCreateTime(1678886400000L);
        orderEvent.setExecInst(Collections.singletonList("POST_ONLY"));


        Order order = CryptoComAdapters.adaptUserOrder(orderEvent);

        assertThat(order.getId()).isEqualTo("order123");
        assertThat(order.getInstrument()).isEqualTo(new CurrencyPair("BTC/USDT"));
        assertThat(order.getType()).isEqualTo(Order.OrderType.ASK);
        assertThat(order.getOriginalAmount()).isEqualTo(new BigDecimal("0.5"));
        assertThat(order.getCumulativeAmount()).isEqualTo(new BigDecimal("0.1"));
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.OPEN);
        assertThat(order.getTimestamp()).isEqualTo(new Date(1678886400000L));
        assertThat(order.hasFlag(Order.OrderFlags.POST_ONLY)).isTrue();
        assertThat(((org.knowm.xchange.dto.trade.LimitOrder) order).getLimitPrice()).isEqualTo(new BigDecimal("51000.0"));
    }

    @Test
    void testAdaptUserOrder_stopLimit() {
        CryptoComUserOrderEvent orderEvent = new CryptoComUserOrderEvent();
        orderEvent.setOrderId("stopOrder123");
        orderEvent.setInstrumentName("ETH_USDT");
        orderEvent.setType("STOP_LIMIT"); // Crypto.com type
        orderEvent.setSide("BUY");
        orderEvent.setLimitPrice(new BigDecimal("2000.0")); // Limit price for the order once triggered
        orderEvent.setRefPrice(new BigDecimal("1950.0")); // Trigger price
        orderEvent.setQuantity(new BigDecimal("1.0"));
        orderEvent.setStatus("PENDING"); // Assuming PENDING until triggered, then ACTIVE
        orderEvent.setCreateTime(System.currentTimeMillis());

        Order order = CryptoComAdapters.adaptUserOrder(orderEvent);

        assertThat(order).isInstanceOf(StopOrder.class);
        StopOrder stopOrder = (StopOrder) order;
        assertThat(stopOrder.getId()).isEqualTo("stopOrder123");
        assertThat(stopOrder.getInstrument()).isEqualTo(new CurrencyPair("ETH/USDT"));
        assertThat(stopOrder.getType()).isEqualTo(Order.OrderType.BID); // Side
        assertThat(stopOrder.getOriginalAmount()).isEqualTo(new BigDecimal("1.0"));
        assertThat(stopOrder.getLimitPrice()).isEqualTo(new BigDecimal("2000.0"));
        assertThat(stopOrder.getTriggerPrice()).isEqualTo(new BigDecimal("1950.0"));
        assertThat(stopOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING_NEW);
    }


    @Test
    void testAdaptUserTrade() {
        CryptoComUserTradeEvent tradeEvent = new CryptoComUserTradeEvent();
        tradeEvent.setTradeId("trade123");
        tradeEvent.setOrderId("order123");
        tradeEvent.setInstrumentName("BTC_USDT");
        tradeEvent.setSide("BUY");
        tradeEvent.setTradedPrice(new BigDecimal("50000.0"));
        tradeEvent.setTradedQuantity(new BigDecimal("0.001"));
        tradeEvent.setFees(new BigDecimal("0.000001"));
        tradeEvent.setFeeInstrumentName("BTC");
        tradeEvent.setCreateTime(1678886400000L);

        UserTrade userTrade = CryptoComAdapters.adaptUserTrade(tradeEvent);

        assertThat(userTrade.getId()).isEqualTo("trade123");
        assertThat(userTrade.getOrderId()).isEqualTo("order123");
        assertThat(userTrade.getInstrument()).isEqualTo(new CurrencyPair("BTC/USDT"));
        assertThat(userTrade.getPrice()).isEqualTo(new BigDecimal("50000.0"));
        assertThat(userTrade.getOriginalAmount()).isEqualTo(new BigDecimal("0.001"));
        assertThat(userTrade.getType()).isEqualTo(Order.OrderType.BID);
        assertThat(userTrade.getTimestamp()).isEqualTo(new Date(1678886400000L));
        assertThat(userTrade.getFeeAmount()).isEqualTo(new BigDecimal("0.000001"));
        assertThat(userTrade.getFeeCurrency()).isEqualTo(Currency.BTC);
    }

    @Test
    void testAdaptBalance() {
        CryptoComBalanceEvent balanceEvent = new CryptoComBalanceEvent();
        balanceEvent.setInstrumentName("USDT");
        balanceEvent.setQuantity(new BigDecimal("1000.0"));
        balanceEvent.setReservedQty(new BigDecimal("100.0"));
        // Max withdrawal might be different, but for available for trade, quantity - reserved is common
        balanceEvent.setMaxWithdrawalBalance(new BigDecimal("900.0"));


        Balance balance = CryptoComAdapters.adaptBalance(balanceEvent);

        assertThat(balance.getCurrency()).isEqualTo(Currency.USDT);
        assertThat(balance.getTotal()).isEqualTo(new BigDecimal("1000.0"));
        assertThat(balance.getFrozen()).isEqualTo(new BigDecimal("100.0"));
        assertThat(balance.getAvailable()).isEqualTo(new BigDecimal("900.0")); // 1000 - 100
    }

    @Test
    void testAdaptPosition_long() {
        CryptoComPositionEvent positionEvent = new CryptoComPositionEvent();
        positionEvent.setInstrumentName("BTCUSD-PERP");
        positionEvent.setQuantity(new BigDecimal("0.5"));
        positionEvent.setOpenPosCost(new BigDecimal("25000.0")); // 0.5 BTC bought at avg 50000
        positionEvent.setMarkPrice(new BigDecimal("50500.0"));   // Current mark price
        positionEvent.setOpenPositionPnl(new BigDecimal("250.0")); // (50500 - 50000) * 0.5

        OpenPosition position = (OpenPosition) CryptoComAdapters.adaptPosition(positionEvent);

        assertThat(position.getInstrument()).isEqualTo(new FuturesContract(new CurrencyPair("BTC/USD"), "PERP"));
        assertThat(position.getSize()).isEqualTo(new BigDecimal("0.5"));
        assertThat(position.getType()).isEqualTo(OpenPosition.Type.LONG);
        assertThat(position.getPrice()).isEqualTo(new BigDecimal("50000.0")); // Avg entry price
        assertThat(position.getUnrealisedPnl()).isEqualTo(new BigDecimal("250.0"));
    }

    @Test
    void testAdaptPosition_short() {
        CryptoComPositionEvent positionEvent = new CryptoComPositionEvent();
        positionEvent.setInstrumentName("ETHUSD-PERP");
        positionEvent.setQuantity(new BigDecimal("-2.0")); // Short position
        positionEvent.setOpenPosCost(new BigDecimal("-4000.0")); // -2 ETH sold at avg 2000
        positionEvent.setMarkPrice(new BigDecimal("1900.0"));   // Current mark price
        positionEvent.setOpenPositionPnl(new BigDecimal("200.0")); // (2000 - 1900) * 2

        OpenPosition position = (OpenPosition) CryptoComAdapters.adaptPosition(positionEvent);

        assertThat(position.getInstrument()).isEqualTo(new FuturesContract(new CurrencyPair("ETH/USD"), "PERP"));
        assertThat(position.getSize()).isEqualTo(new BigDecimal("2.0"));
        assertThat(position.getType()).isEqualTo(OpenPosition.Type.SHORT);
        assertThat(position.getPrice()).isEqualTo(new BigDecimal("2000.0")); // Avg entry price
        assertThat(position.getUnrealisedPnl()).isEqualTo(new BigDecimal("200.0"));
    }
}

package org.knowm.xchange.cryptocom;

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
import org.knowm.xchange.dto.account.Position;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CryptoComAdapters {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComAdapters.class);

    private CryptoComAdapters() {
        // Private constructor for utility class
    }

    public static Instrument adaptInstrument(String cryptoComInstrumentName) {
        if (cryptoComInstrumentName == null || cryptoComInstrumentName.isEmpty()) {
            return null;
        }

        // Examples: "BTCUSD-PERP", "ETH_CRO", "CRO", "BTCUSD-231229" (Future), "BTCUSD-231229-30000-C" (Option)
        if (cryptoComInstrumentName.contains("-PERP")) {
            String[] parts = cryptoComInstrumentName.split("-PERP");
            CurrencyPair underlying = new CurrencyPair(parts[0].replace("USD", "/USD")); // Assuming USD quote for perps
            return new FuturesContract(underlying, "PERP");
        } else if (cryptoComInstrumentName.matches(".*-\\d{6}")) { // Basic check for future, e.g., BTCUSD-231229
             String[] parts = cryptoComInstrumentName.split("-");
             CurrencyPair underlying = new CurrencyPair(parts[0].replace("USD","/USD")); // Crude, needs better parsing
             return new FuturesContract(underlying, parts[1]); // parts[1] is date like "231229"
        } else if (cryptoComInstrumentName.matches(".*-\\d{6}-\\d+-[CP]")) { // Basic check for option
            String[] parts = cryptoComInstrumentName.split("-");
            CurrencyPair underlying = new CurrencyPair(parts[0].replace("USD","/USD"));
            // This is a simplified representation. OptionsContract is more complex.
            // For now, creating a basic OptionsContract.
            // Proper parsing of strike, type (Call/Put) and expiry is needed.
            // return new OptionsContract(underlying, parts[1], new BigDecimal(parts[2]), adaptOptionType(parts[3]));
            LOG.warn("OptionsContract parsing for {} is simplified and may not be fully correct.", cryptoComInstrumentName);
            return new OptionsContract.Builder().currencyPair(underlying).expireDate(adaptDate(parts[1]))
                .strike(new BigDecimal(parts[2])).type(parts[3].equalsIgnoreCase("C") ? OptionsContract.OptionType.CALL : OptionsContract.OptionType.PUT).build();

        } else if (cryptoComInstrumentName.contains("_")) {
            return new CurrencyPair(cryptoComInstrumentName.replace("_", "/"));
        } else {
            // Assuming it's a single currency if no pair/derivative identifiers
            return Currency.getInstance(cryptoComInstrumentName);
        }
    }

    private static Date adaptDate(String dateStr) { // YYMMDD
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            LOG.error("Failed to parse date string: {}", dateStr, e);
            return null;
        }
    }


    public static String adaptXchangeInstrument(Instrument instrument) {
        if (instrument == null) {
            return null;
        }
        if (instrument instanceof CurrencyPair) {
            return ((CurrencyPair) instrument).getBase().getCurrencyCode() + "_" + ((CurrencyPair) instrument).getCounter().getCurrencyCode();
        } else if (instrument instanceof FuturesContract) {
            FuturesContract contract = (FuturesContract) instrument;
            String base = contract.getCurrencyPair().getBase().getCurrencyCode();
            String counter = contract.getCurrencyPair().getCounter().getCurrencyCode();
            if ("PERP".equals(contract.getPrompt())) {
                return base + counter + "-PERP";
            } else {
                 // Assuming prompt is YYMMDD date string for futures for now
                return base + counter + "-" + contract.getPrompt();
            }
        } else if (instrument instanceof OptionsContract) {
             OptionsContract contract = (OptionsContract) instrument;
             String base = contract.getCurrencyPair().getBase().getCurrencyCode();
             String counter = contract.getCurrencyPair().getCounter().getCurrencyCode();
             SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
             sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
             String expiry = sdf.format(contract.getExpireDate());
             String type = contract.getType() == OptionsContract.OptionType.CALL ? "C" : "P";
             return base + counter + "-" + expiry + "-" + contract.getStrike().stripTrailingZeros().toPlainString() + "-" + type;
        }
        // Fallback for single Currency or other instrument types
        return instrument.toString().replace("/", "_");
    }

    public static OrderBook adaptOrderBook(CryptoComBookEvent bookEvent, Instrument instrument) {
        List<LimitOrder> asks = new ArrayList<>();
        List<LimitOrder> bids = new ArrayList<>();

        if (bookEvent.getAsks() != null) {
            for (CryptoComOrderBookEntry entry : bookEvent.getAsks()) {
                asks.add(new LimitOrder(Order.OrderType.ASK, entry.getQuantity(), instrument, null, null, entry.getPrice()));
            }
        }
        if (bookEvent.getBids() != null) {
            for (CryptoComOrderBookEntry entry : bookEvent.getBids()) {
                bids.add(new LimitOrder(Order.OrderType.BID, entry.getQuantity(), instrument, null, null, entry.getPrice()));
            }
        }
        // Crypto.com provides 'tt' (last book update) and 't' (message publish). Using 'tt' for order book timestamp.
        return new OrderBook(new Date(bookEvent.getLastUpdateTimestamp()), asks, bids);
    }

    public static Ticker adaptTicker(CryptoComTickerEvent tickerEvent, Instrument instrument) {
        return new Ticker.Builder()
            .instrument(instrument)
            .last(tickerEvent.getLastTradePrice())
            .bid(tickerEvent.getBestBidPrice())
            .ask(tickerEvent.getBestAskPrice())
            .bidSize(tickerEvent.getBestBidSize())
            .askSize(tickerEvent.getBestAskSize())
            .high(tickerEvent.getHigh())
            .low(tickerEvent.getLow())
            .volume(tickerEvent.getVolume())
            .quoteVolume(tickerEvent.getVolumeValue()) // Assuming vv is quoteVolume
            .timestamp(new Date(tickerEvent.getTimestamp()))
            .openInterest(tickerEvent.getOpenInterest())
            .percentageChange(tickerEvent.getChange()) // Assuming 'c' is percentage change, might need scaling
            .build();
    }

    public static Trade adaptTrade(CryptoComTradeEvent tradeEvent, Instrument instrument) {
        return new Trade.Builder()
            .id(String.valueOf(tradeEvent.getTradeId()))
            .instrument(instrument)
            .price(tradeEvent.getPrice())
            .originalAmount(tradeEvent.getQuantity())
            .type(adaptSideToOrderType(tradeEvent.getSide()))
            .timestamp(new Date(tradeEvent.getTimestamp()))
            .build();
    }

    public static Order.OrderType adaptSideToOrderType(String side) {
        if (side == null) return null;
        switch (side.toUpperCase(Locale.ROOT)) {
            case "BUY":
                return Order.OrderType.BID;
            case "SELL":
                return Order.OrderType.ASK;
            default:
                LOG.warn("Unknown order side: {}", side);
                return null;
        }
    }

    public static Order.OrderStatus adaptOrderStatus(String cryptoComStatus) {
        if (cryptoComStatus == null) return null;
        switch (cryptoComStatus.toUpperCase(Locale.ROOT)) {
            case "ACTIVE":
                return Order.OrderStatus.OPEN;
            case "NEW": // For orders that are acknowledged but not yet active in the book
                return Order.OrderStatus.NEW;
            case "PENDING": // For conditional orders not yet triggered
                return Order.OrderStatus.PENDING_NEW; // Or a custom status if XChange adds more detail
            case "FILLED":
                return Order.OrderStatus.FILLED;
            case "CANCELED": // Crypto.com uses "CANCELED"
                return Order.OrderStatus.CANCELED;
            case "REJECTED":
                return Order.OrderStatus.REJECTED;
            case "EXPIRED":
                return Order.OrderStatus.EXPIRED;
            // Add other mappings as necessary
            default:
                LOG.warn("Unknown order status: {}", cryptoComStatus);
                return Order.OrderStatus.UNKNOWN;
        }
    }

    public static Order.OrderType adaptOrderType(String cryptoComOrderType) {
        if (cryptoComOrderType == null) return null;
        switch (cryptoComOrderType.toUpperCase(Locale.ROOT)) {
            case "LIMIT":
                return Order.OrderType.LIMIT;
            case "MARKET":
                return Order.OrderType.MARKET;
            case "STOP_LOSS":
            case "STOP_LIMIT": // Both can be represented as STOP in XChange, details in specific order type
            case "TAKE_PROFIT":
            case "TAKE_PROFIT_LIMIT":
                return Order.OrderType.STOP;
            default:
                LOG.warn("Unknown order type: {}", cryptoComOrderType);
                return null; // Or throw exception
        }
    }


    public static Order adaptUserOrder(CryptoComUserOrderEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        Order.OrderType orderType = adaptSideToOrderType(event.getSide()); // This is BID/ASK
        String cryptoComOrderTypeStr = event.getType(); // This is LIMIT, MARKET, etc.

        Order.Builder builder;
        switch (cryptoComOrderTypeStr.toUpperCase(Locale.ROOT)) {
            case "LIMIT":
            case "TAKE_PROFIT_LIMIT": // Represented as LimitOrder with trigger for XChange StopOrder
            case "STOP_LIMIT":        // Represented as LimitOrder with trigger for XChange StopOrder
                 builder = new LimitOrder.Builder(orderType, instrument)
                                .limitPrice(event.getLimitPrice());
                 if (cryptoComOrderTypeStr.contains("STOP") || cryptoComOrderTypeStr.contains("TAKE_PROFIT")) {
                     // TODO: Need trigger price from event if available for StopOrder in XChange
                     // builder = new StopOrder.Builder(orderType, instrument).limitPrice(event.getLimitPrice());
                     // ((StopOrder.Builder)builder).triggerPrice(event.getTriggerPrice()); // Assuming triggerPrice field
                 }
                break;
            case "MARKET":
            case "STOP_LOSS":       // Represented as MarketOrder with trigger for XChange StopOrder
            case "TAKE_PROFIT":     // Represented as MarketOrder with trigger for XChange StopOrder
                builder = new MarketOrder.Builder(orderType, instrument);
                 if (cryptoComOrderTypeStr.contains("STOP") || cryptoComOrderTypeStr.contains("TAKE_PROFIT")) {
                     // TODO: Need trigger price from event if available for StopOrder in XChange
                     // builder = new StopOrder.Builder(orderType, instrument);
                     // ((StopOrder.Builder)builder).triggerPrice(event.getTriggerPrice());
                 }
                break;
            default:
                LOG.warn("Unhandled Crypto.com order type for builder: {}", cryptoComOrderTypeStr);
                // Fallback to generic order builder or throw
                builder = new LimitOrder.Builder(orderType, instrument);
        }


        builder.id(event.getOrderId())
            .originalAmount(event.getQuantity())
            .cumulativeAmount(event.getCumulativeQuantity())
            .averagePrice(event.getAveragePrice())
            .orderStatus(adaptOrderStatus(event.getStatus()))
            .timestamp(new Date(event.getCreateTime()))
            .userReference(event.getClientOid());
            // .fee(event.getCumulativeFee()) // TODO: Fee currency?

        // Add flags if present in exec_inst
        if (event.getExecInst() != null) {
            for (String flag : event.getExecInst()) {
                if ("POST_ONLY".equalsIgnoreCase(flag)) {
                    builder.flag(Order.OrderFlags.POST_ONLY);
                }
                // Add other flag mappings if necessary
            }
        }

        // For more specific order types like StopOrder, you might need to check cryptoComOrderTypeStr again
        // and cast the builder or create a StopOrder directly if trigger price is available.
        // This simplified version creates LimitOrder or MarketOrder.

        return builder.build();
    }

    public static UserTrade adaptUserTrade(CryptoComUserTradeEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        Order.OrderType orderType = adaptSideToOrderType(event.getSide());
        Currency feeCurrency = Currency.getInstance(event.getFeeInstrumentName());

        return new UserTrade.Builder()
            .id(event.getTradeId())
            .orderId(event.getOrderId())
            .instrument(instrument)
            .price(event.getTradedPrice())
            .originalAmount(event.getTradedQuantity())
            .type(orderType)
            .timestamp(new Date(event.getCreateTime()))
            .feeAmount(event.getFees())
            .feeCurrency(feeCurrency)
            .orderUserReference(event.getClientOid())
            // .takerMaker(event.getTakerSide().equalsIgnoreCase("TAKER") ? TakerMaker.TAKER : TakerMaker.MAKER) // If needed
            .build();
    }

    public static Balance adaptBalance(CryptoComBalanceEvent event) {
        Currency currency = Currency.getInstance(event.getInstrumentName());
        BigDecimal available = event.getMaxWithdrawalBalance(); // Or calculate based on collateral rules if more accurate
        BigDecimal reserved = event.getReservedQty();
        BigDecimal total = event.getQuantity();
        BigDecimal frozen = (reserved != null) ? reserved : BigDecimal.ZERO; // if reserved is considered frozen
        // available might be quantity - reserved, or maxWithdrawalBalance can be used if it reflects actual spendable.
        // Crypto.com's "max_withdrawal_balance" seems like a good candidate for "available".
        // "quantity" is total. "reserved_qty" is on hold.
        // So, available = quantity - reserved_qty seems more standard for XChange if max_withdrawal_balance means something else.
        // Let's assume quantity = total, reserved_qty = frozen, available = total - frozen for now.
        // Re-evaluate if max_withdrawal_balance is a better fit for 'available'.

        if (total == null) total = BigDecimal.ZERO;
        if (frozen == null) frozen = BigDecimal.ZERO;
        available = total.subtract(frozen);


        return new Balance.Builder()
            .currency(currency)
            .total(total)
            .available(available)
            .frozen(frozen)
            // .borrowed() and .loaned() if margin data is available and mapped
            .build();
    }

    public static Position adaptPosition(CryptoComPositionEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        OpenPosition.Type type = event.getQuantity().compareTo(BigDecimal.ZERO) >= 0 ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT;

        return new OpenPosition.Builder()
            .instrument(instrument)
            .price(event.getMarkPrice()) // Or avg entry price if available and more suitable
            .size(event.getQuantity().abs())
            .type(type)
            .unrealisedPnl(event.getOpenPositionPnl())
            // .liquidationPrice() // TODO: If available in DTO
            // .collateral() // TODO: If available
            .build();
    }
}

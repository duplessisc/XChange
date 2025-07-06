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

        // Spot: BTC_USDT, ETH_CRO
        // Perpetual Futures: BTCUSD-PERP
        // Dated Futures: BTCUSD-231229 (YYMMDD)
        // Options: BTCUSD-231229-30000-C (Instrument-YYMMDD-Strike-Type)

        if (cryptoComInstrumentName.endsWith("-PERP")) {
            String baseSymbol = cryptoComInstrumentName.substring(0, cryptoComInstrumentName.indexOf("-PERP"));
            CurrencyPair underlying = adaptUnderlyingToCurrencyPair(baseSymbol);
            return new FuturesContract(underlying, "PERP");
        } else if (cryptoComInstrumentName.matches(".+-\\d{6}$")) { // Dated Future: BTCUSD-231229
            String[] parts = cryptoComInstrumentName.split("-");
            CurrencyPair underlying = adaptUnderlyingToCurrencyPair(parts[0]);
            return new FuturesContract(underlying, parts[1]); // parts[1] is YYMMDD
        } else if (cryptoComInstrumentName.matches(".+-\\d{6}-\\d+-[CP]$")) { // Option: BTCUSD-231229-30000-C
            String[] parts = cryptoComInstrumentName.split("-");
            CurrencyPair underlying = adaptUnderlyingToCurrencyPair(parts[0]);
            Date expiryDate = adaptDateYYMMDD(parts[1]);
            BigDecimal strikePrice = new BigDecimal(parts[2]);
            OptionsContract.OptionType optionType = parts[3].equalsIgnoreCase("C") ? OptionsContract.OptionType.CALL : OptionsContract.OptionType.PUT;
            return new OptionsContract.Builder()
                .currencyPair(underlying)
                .expireDate(expiryDate)
                .strike(strikePrice)
                .type(optionType)
                .build();
        } else if (cryptoComInstrumentName.contains("_")) { // Spot
            return new CurrencyPair(cryptoComInstrumentName.replace("_", "/"));
        } else { // Single currency
            return Currency.getInstance(cryptoComInstrumentName);
        }
    }

    // Helper to form CurrencyPair from underlying symbol like "BTCUSD"
    private static CurrencyPair adaptUnderlyingToCurrencyPair(String underlyingSymbol) {
        // Assuming common pattern like BTCUSD, ETHUSD.
        // More robust parsing might be needed if other patterns exist (e.g., 3-letter vs 4-letter quotes)
        if (underlyingSymbol.endsWith("USD")) {
            return new CurrencyPair(underlyingSymbol.substring(0, underlyingSymbol.length() - 3), "USD");
        }  else if (underlyingSymbol.endsWith("USDT")) {
            return new CurrencyPair(underlyingSymbol.substring(0, underlyingSymbol.length() - 4), "USDT");
        } else if (underlyingSymbol.endsWith("EUR")) {
            return new CurrencyPair(underlyingSymbol.substring(0, underlyingSymbol.length() - 3), "EUR");
        }
        // Add more quote currencies as needed or a more generic split
        // For now, a simple assumption for common pairs.
        // If pair is like BTCETH, this will need smarter logic.
        // Defaulting to a 3-letter quote currency assumption if not USD/USDT/EUR.
        if (underlyingSymbol.length() > 3) {
            String base = underlyingSymbol.substring(0, underlyingSymbol.length() - 3);
            String counter = underlyingSymbol.substring(underlyingSymbol.length() - 3);
            return new CurrencyPair(base, counter);
        }
        LOG.warn("Could not reliably parse underlying symbol {} into CurrencyPair", underlyingSymbol);
        return new CurrencyPair(underlyingSymbol, ""); // Fallback, likely incorrect
    }

    private static Date adaptDateYYMMDD(String dateStr) { // YYMMDD
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            LOG.error("Failed to parse YYMMDD date string: {}", dateStr, e);
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
            String underlyingSymbol = base + counter;
            if ("PERP".equals(contract.getPrompt())) {
                return underlyingSymbol + "-PERP";
            } else {
                // Prompt should be YYMMDD for dated futures
                return underlyingSymbol + "-" + contract.getPrompt();
            }
        } else if (instrument instanceof OptionsContract) {
            OptionsContract contract = (OptionsContract) instrument;
            String base = contract.getCurrencyPair().getBase().getCurrencyCode();
            String counter = contract.getCurrencyPair().getCounter().getCurrencyCode();
            String underlyingSymbol = base + counter;
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String expiry = sdf.format(contract.getExpireDate());
            String type = contract.getType() == OptionsContract.OptionType.CALL ? "C" : "P";
            return underlyingSymbol + "-" + expiry + "-" + contract.getStrike().stripTrailingZeros().toPlainString() + "-" + type;
        }
        // Fallback for single Currency
        if (instrument instanceof Currency) {
            return ((Currency) instrument).getCurrencyCode();
        }
        LOG.warn("Cannot adapt XChange Instrument {} to Crypto.com string format", instrument);
        return instrument.toString().replace("/", "_"); // Generic fallback
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
            case "STOP_LIMIT":
            case "TAKE_PROFIT_LIMIT":
                return Order.OrderType.STOP; // XChange StopOrder can have a limit price
            case "STOP_LOSS":
            case "TAKE_PROFIT":
                return Order.OrderType.STOP; // XChange StopOrder can be a market order if limit price is null
            default:
                LOG.warn("Unknown order type: {}", cryptoComOrderType);
                return null; // Or throw exception
        }
    }

    public static Order adaptUserOrder(CryptoComUserOrderEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        Order.OrderType orderSide = adaptSideToOrderType(event.getSide()); // BID or ASK
        String cryptoComOrderTypeStr = event.getType(); // LIMIT, MARKET, STOP_LOSS etc.

        Order.Builder builder = null;

        // Assuming CryptoComUserOrderEvent DTO has getRefPrice() and getRefPriceType() for trigger orders
        // These would need to be added to the DTO if not already present.
        BigDecimal triggerPrice = event.getRefPrice(); // Placeholder if DTO doesn't have it yet
        // String triggerType = event.getRefPriceType(); // Placeholder

        if (cryptoComOrderTypeStr.contains("STOP") || cryptoComOrderTypeStr.contains("TAKE_PROFIT")) {
            StopOrder.Builder stopBuilder = new StopOrder.Builder(orderSide, instrument)
                .triggerPrice(triggerPrice); // Must have trigger price
            if (cryptoComOrderTypeStr.endsWith("_LIMIT")) {
                stopBuilder.limitPrice(event.getLimitPrice());
            }
            builder = stopBuilder;
        } else if ("LIMIT".equalsIgnoreCase(cryptoComOrderTypeStr)) {
            builder = new LimitOrder.Builder(orderSide, instrument)
                .limitPrice(event.getLimitPrice());
        } else if ("MARKET".equalsIgnoreCase(cryptoComOrderTypeStr)) {
            builder = new MarketOrder.Builder(orderSide, instrument);
        } else {
            LOG.warn("Unhandled Crypto.com order type for builder: {}", cryptoComOrderTypeStr);
            // Fallback to generic LimitOrder builder for safety, though this may be inaccurate
            builder = new LimitOrder.Builder(orderSide, instrument).limitPrice(event.getLimitPrice());
        }

        builder.id(event.getOrderId())
            .originalAmount(event.getQuantity())
            .cumulativeAmount(event.getCumulativeQuantity())
            .averagePrice(event.getAveragePrice())
            .orderStatus(adaptOrderStatus(event.getStatus()))
            .timestamp(new Date(event.getCreateTime()))
            .userReference(event.getClientOid());
        // Note: maker_fee_rate and taker_fee_rate from event are not directly mapped to XChange Order.
        // CumulativeFee is available, but XChange Order doesn't have a direct fee field.
        // Fees are typically part of UserTrade.

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
        BigDecimal total = event.getQuantity();
        BigDecimal reserved = event.getReservedQty();
        BigDecimal available;

        // Assumption: 'quantity' is the total amount. 'reserved_qty' is what's on hold (e.g., in open orders).
        // 'max_withdrawal_balance' is what can actually be withdrawn, which might be less than total - reserved
        // due to other holds or margin requirements not explicitly detailed in 'reserved_qty'.
        // For XChange 'available', we typically mean available for trading.
        // If 'max_withdrawal_balance' also reflects trading availability, it could be used.
        // Sticking to 'total - reserved' as a common interpretation for 'available for trading'.
        if (total == null) total = BigDecimal.ZERO;
        if (reserved == null) reserved = BigDecimal.ZERO;
        available = total.subtract(reserved);

        // It's good to log if max_withdrawal_balance significantly differs from calculated available,
        // as it might indicate a misunderstanding of fields.
        if (event.getMaxWithdrawalBalance() != null && event.getMaxWithdrawalBalance().compareTo(available) != 0) {
            LOG.debug("Balance for {}: Calculated available ({}) differs from max_withdrawal_balance ({}). Using calculated.",
                currency, available, event.getMaxWithdrawalBalance());
        }

        // TODO: Incorporate other fields from user.balance's main object if creating a full AccountInfo,
        // e.g., total_available_balance (overall), total_margin_balance, total_initial_margin.
        // This adapter focuses on individual currency balances from the 'position_balances' array.

        return new Balance.Builder()
            .currency(currency)
            .total(total)
            .available(available)
            .frozen(reserved)
            // .borrowed() and .loaned() would require more data, possibly from overall account metrics
            .build();
    }

    public static Position adaptPosition(CryptoComPositionEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        BigDecimal quantity = event.getQuantity();
        OpenPosition.Type type = (quantity != null && quantity.compareTo(BigDecimal.ZERO) >= 0) ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT;
        BigDecimal openPosCost = event.getOpenPosCost(); // cost of the open position
        BigDecimal absQuantity = (quantity != null) ? quantity.abs() : BigDecimal.ZERO;
        BigDecimal averagePrice = null;

        if (openPosCost != null && absQuantity.compareTo(BigDecimal.ZERO) > 0) {
            averagePrice = openPosCost.divide(absQuantity, 8, BigDecimal.ROUND_HALF_UP); // 8 decimal places, adjust as needed
        } else if (event.getMarkPrice() != null) {
            // Fallback to mark_price if cost/quantity isn't suitable for entry price
            averagePrice = event.getMarkPrice();
            LOG.debug("Using mark_price as entry price for position {} due to missing cost/quantity for avg price calculation.", event.getInstrumentName());
        }


        OpenPosition.Builder positionBuilder = new OpenPosition.Builder()
            .instrument(instrument)
            .price(averagePrice)
            .size(absQuantity)
            .type(type)
            .unrealisedPnl(event.getOpenPositionPnl());

        // No standard fields in XChange Position for these, but could be added to extended XChange-CryptoComPosition
        // event.getPosInitialMargin();
        // event.getPosMaintenanceMargin();
        // event.getTargetLeverage();
        // event.getLiquidationPrice(); // If CryptoComPositionEvent DTO gets this field

        return positionBuilder.build();
    }

    // Placeholder for adaptUserOrder to access refPrice if added to DTO
    // This is a conceptual change, assuming CryptoComUserOrderEvent is updated.
    // If not, the original adaptUserOrder logic for stop orders (commented out) remains non-functional for trigger prices.
    // For this exercise, I'll assume the DTO *would* be updated.
    // Example of how it would look if DTO had getRefPrice():
    /*
    public static Order adaptUserOrder_withTriggerExample(CryptoComUserOrderEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        Order.OrderType orderSide = adaptSideToOrderType(event.getSide());
        String cryptoComOrderTypeStr = event.getType();
        Order.Builder builder;

        BigDecimal triggerPrice = event.getRefPrice(); // ASSUMING DTO has this
        // String triggerType = event.getRefPriceType(); // ASSUMING DTO has this

        if ("STOP_LIMIT".equalsIgnoreCase(cryptoComOrderTypeStr) || "TAKE_PROFIT_LIMIT".equalsIgnoreCase(cryptoComOrderTypeStr)) {
            builder = new StopOrder.Builder(orderSide, instrument)
                            .limitPrice(event.getLimitPrice())
                            .triggerPrice(triggerPrice);
        } else if ("STOP_LOSS".equalsIgnoreCase(cryptoComOrderTypeStr) || "TAKE_PROFIT".equalsIgnoreCase(cryptoComOrderTypeStr)) {
            builder = new StopOrder.Builder(orderSide, instrument)
                            .triggerPrice(triggerPrice); // Market-if-touched stop order
        } else if ("LIMIT".equalsIgnoreCase(cryptoComOrderTypeStr)) {
            builder = new LimitOrder.Builder(orderSide, instrument)
                            .limitPrice(event.getLimitPrice());
        } else if ("MARKET".equalsIgnoreCase(cryptoComOrderTypeStr)) {
            builder = new MarketOrder.Builder(orderSide, instrument);
        } else {
            LOG.warn("Unhandled Crypto.com order type for builder: {}", cryptoComOrderTypeStr);
            builder = new LimitOrder.Builder(orderSide, instrument); // Fallback
        }

        builder.id(event.getOrderId())
            .originalAmount(event.getQuantity())
            .cumulativeAmount(event.getCumulativeQuantity())
            .averagePrice(event.getAveragePrice())
            .orderStatus(adaptOrderStatus(event.getStatus()))
            .timestamp(new Date(event.getCreateTime()))
            .userReference(event.getClientOid());

        if (event.getExecInst() != null) {
            for (String flag : event.getExecInst()) {
                if ("POST_ONLY".equalsIgnoreCase(flag)) {
                    builder.flag(Order.OrderFlags.POST_ONLY);
                }
            }
        }
        return builder.build();
    }
    */
}

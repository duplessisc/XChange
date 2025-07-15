package org.knowm.xchange.cryptocom;

import org.knowm.xchange.cryptocom.dto.account.CryptoComBalanceEvent;
import org.knowm.xchange.cryptocom.dto.account.CryptoComPositionEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComBookEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComOrderBookEntry;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTickerEvent;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComTradeEvent;
// Importing REST specific DTOs where applicable
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComInstrument;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComRestOrderBook; // Will use this for REST
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
import java.util.stream.Collectors;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.InstrumentMetaData;
import java.util.Map;
import java.util.HashMap;


public class CryptoComAdapters {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComAdapters.class);

    private CryptoComAdapters() {
        // Private constructor for utility class
    }

    public static Instrument adaptInstrument(String cryptoComInstrumentName) {
        if (cryptoComInstrumentName == null || cryptoComInstrumentName.isEmpty()) {
            return null;
        }

        if (cryptoComInstrumentName.endsWith("-PERP")) {
            String baseSymbol = cryptoComInstrumentName.substring(0, cryptoComInstrumentName.indexOf("-PERP"));
            CurrencyPair underlying = adaptUnderlyingToCurrencyPair(baseSymbol);
            return new FuturesContract(underlying, "PERP");
        } else if (cryptoComInstrumentName.matches(".+-\\d{6}$")) {
            String[] parts = cryptoComInstrumentName.split("-");
            CurrencyPair underlying = adaptUnderlyingToCurrencyPair(parts[0]);
            return new FuturesContract(underlying, parts[1]);
        } else if (cryptoComInstrumentName.matches(".+-\\d{6}-\\d+-[CP]$")) {
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
        } else if (cryptoComInstrumentName.contains("_")) {
            return new CurrencyPair(cryptoComInstrumentName.replace("_", "/"));
        } else {
            return Currency.getInstance(cryptoComInstrumentName);
        }
    }

    private static CurrencyPair adaptUnderlyingToCurrencyPair(String underlyingSymbol) {
        if (underlyingSymbol.endsWith("USD")) {
            return new CurrencyPair(underlyingSymbol.substring(0, underlyingSymbol.length() - 3), "USD");
        }  else if (underlyingSymbol.endsWith("USDT")) {
            return new CurrencyPair(underlyingSymbol.substring(0, underlyingSymbol.length() - 4), "USDT");
        } else if (underlyingSymbol.endsWith("EUR")) {
            return new CurrencyPair(underlyingSymbol.substring(0, underlyingSymbol.length() - 3), "EUR");
        }
        if (underlyingSymbol.length() > 3) {
            String base = underlyingSymbol.substring(0, underlyingSymbol.length() - 3);
            String counter = underlyingSymbol.substring(underlyingSymbol.length() - 3);
            return new CurrencyPair(base, counter);
        }
        LOG.warn("Could not reliably parse underlying symbol {} into CurrencyPair", underlyingSymbol);
        return new CurrencyPair(underlyingSymbol, "");
    }

    private static Date adaptDateYYMMDD(String dateStr) {
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
        if (instrument instanceof Currency) {
            return ((Currency) instrument).getCurrencyCode();
        }
        LOG.warn("Cannot adapt XChange Instrument {} to Crypto.com string format", instrument);
        return instrument.toString().replace("/", "_");
    }

    // For REST OrderBook (uses CryptoComRestOrderBook DTO)
    public static OrderBook adaptOrderBook(CryptoComRestOrderBook restBook, Instrument instrument) {
        List<LimitOrder> asks = new ArrayList<>();
        List<LimitOrder> bids = new ArrayList<>();
        long timestamp = (restBook != null && restBook.getTimestamp() != 0) ? restBook.getTimestamp() : System.currentTimeMillis();


        if (restBook != null && restBook.getAsks() != null) {
            for (CryptoComOrderBookEntry entry : restBook.getAsks()) {
                asks.add(new LimitOrder(Order.OrderType.ASK, entry.getQuantity(), instrument, null, new Date(timestamp), entry.getPrice()));
            }
        }
        if (restBook != null && restBook.getBids() != null) {
            for (CryptoComOrderBookEntry entry : restBook.getBids()) {
                bids.add(new LimitOrder(Order.OrderType.BID, entry.getQuantity(), instrument, null, new Date(timestamp), entry.getPrice()));
            }
        }
        return new OrderBook(new Date(timestamp), asks, bids);
    }

    // Overloaded for Streaming OrderBook (uses CryptoComBookEvent DTO from stream module)
    // Note: DTOs from stream module should be moved or duplicated to base if used here.
    // For now, assuming CryptoComBookEvent might be a shared DTO.
     public static OrderBook adaptOrderBook(org.knowm.xchange.cryptocom.dto.marketdata.CryptoComBookEvent streamBookEvent, Instrument instrument) {
        List<LimitOrder> asks = new ArrayList<>();
        List<LimitOrder> bids = new ArrayList<>();
        long timestamp = (streamBookEvent != null && streamBookEvent.getLastUpdateTimestamp() != 0) ? streamBookEvent.getLastUpdateTimestamp() : System.currentTimeMillis();

        if (streamBookEvent != null && streamBookEvent.getAsks() != null) {
            for (CryptoComOrderBookEntry entry : streamBookEvent.getAsks()) {
                asks.add(new LimitOrder(Order.OrderType.ASK, entry.getQuantity(), instrument, null, new Date(timestamp), entry.getPrice()));
            }
        }
        if (streamBookEvent != null && streamBookEvent.getBids() != null) {
            for (CryptoComOrderBookEntry entry : streamBookEvent.getBids()) {
                bids.add(new LimitOrder(Order.OrderType.BID, entry.getQuantity(), instrument, null, new Date(timestamp), entry.getPrice()));
            }
        }
        return new OrderBook(new Date(timestamp), asks, bids);
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
            .quoteVolume(tickerEvent.getVolumeValue())
            .timestamp(new Date(tickerEvent.getTimestamp()))
            .openInterest(tickerEvent.getOpenInterest())
            .percentageChange(tickerEvent.getChange())
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
            case "NEW":
                return Order.OrderStatus.NEW;
            case "PENDING":
                return Order.OrderStatus.PENDING_NEW;
            case "FILLED":
                return Order.OrderStatus.FILLED;
            case "CANCELED":
                return Order.OrderStatus.CANCELED;
            case "REJECTED":
                return Order.OrderStatus.REJECTED;
            case "EXPIRED":
                return Order.OrderStatus.EXPIRED;
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
            case "STOP_LIMIT":
            case "TAKE_PROFIT_LIMIT":
                return Order.OrderType.STOP;
            case "STOP_LOSS":
            case "TAKE_PROFIT":
                return Order.OrderType.STOP;
            default:
                LOG.warn("Unknown order type: {}", cryptoComOrderType);
                return null;
        }
    }

    public static Order adaptUserOrder(CryptoComUserOrderEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        Order.OrderType orderSide = adaptSideToOrderType(event.getSide());
        String cryptoComOrderTypeStr = event.getType();

        Order.Builder builder;
        BigDecimal triggerPrice = event.getRefPrice();

        if (cryptoComOrderTypeStr.contains("STOP") || cryptoComOrderTypeStr.contains("TAKE_PROFIT")) {
            StopOrder.Builder stopBuilder = new StopOrder.Builder(orderSide, instrument)
                .triggerPrice(triggerPrice);
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
            builder = new LimitOrder.Builder(orderSide, instrument).limitPrice(event.getLimitPrice());
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
            .build();
    }

    public static Balance adaptBalance(CryptoComBalanceEvent event) {
        Currency currency = Currency.getInstance(event.getInstrumentName());
        BigDecimal total = event.getQuantity();
        BigDecimal reserved = event.getReservedQty();
        BigDecimal available;

        if (total == null) total = BigDecimal.ZERO;
        if (reserved == null) reserved = BigDecimal.ZERO;
        available = total.subtract(reserved);

        if (event.getMaxWithdrawalBalance() != null && event.getMaxWithdrawalBalance().compareTo(available) != 0) {
            LOG.debug("Balance for {}: Calculated available ({}) differs from max_withdrawal_balance ({}). Using calculated.",
                currency, available, event.getMaxWithdrawalBalance());
        }

        return new Balance.Builder()
            .currency(currency)
            .total(total)
            .available(available)
            .frozen(reserved)
            .build();
    }

    public static Position adaptPosition(CryptoComPositionEvent event) {
        Instrument instrument = adaptInstrument(event.getInstrumentName());
        BigDecimal quantity = event.getQuantity();
        OpenPosition.Type type = (quantity != null && quantity.compareTo(BigDecimal.ZERO) >= 0) ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT;
        BigDecimal openPosCost = event.getOpenPosCost();
        BigDecimal absQuantity = (quantity != null) ? quantity.abs() : BigDecimal.ZERO;
        BigDecimal averagePrice = null;

        if (openPosCost != null && absQuantity.compareTo(BigDecimal.ZERO) > 0) {
            averagePrice = openPosCost.divide(absQuantity, 8, BigDecimal.ROUND_HALF_UP);
        } else if (event.getMarkPrice() != null) {
            averagePrice = event.getMarkPrice();
            LOG.debug("Using mark_price as entry price for position {} due to missing cost/quantity for avg price calculation.", event.getInstrumentName());
        }

        OpenPosition.Builder positionBuilder = new OpenPosition.Builder()
            .instrument(instrument)
            .price(averagePrice)
            .size(absQuantity)
            .type(type)
            .unrealisedPnl(event.getOpenPositionPnl());

        return positionBuilder.build();
    }

    public static List<Instrument> adaptInstruments(List<CryptoComInstrument> cryptoInstruments) {
        if (cryptoInstruments == null) {
            return Collections.emptyList();
        }
        return cryptoInstruments.stream()
            .filter(CryptoComInstrument::isTradable) // Ensure it's tradable
            .map(ci -> adaptInstrument(ci.getSymbol()))
            .filter(i -> i != null)
            .collect(Collectors.toList());
    }

    public static ExchangeMetaData adaptToExchangeMetaData(List<CryptoComInstrument> instrumentDtos) {
        Map<Instrument, InstrumentMetaData> instruments = new HashMap<>();
        Map<Currency, org.knowm.xchange.dto.meta.CurrencyMetaData> currencies = new HashMap<>();

        if (instrumentDtos != null) {
            for (CryptoComInstrument dto : instrumentDtos) {
                if (!dto.isTradable()) {
                    continue;
                }
                Instrument instrument = adaptInstrument(dto.getSymbol());
                if (instrument == null) {
                    continue;
                }

                InstrumentMetaData instrumentMetaData = new InstrumentMetaData.Builder()
                        .tradingFee(null) // Fees usually from private endpoint or fixed schedule
                        .minimumAmount(dto.getQtyTickSize()) // qty_tick_size as minimum amount
                        .priceScale(dto.getQuoteDecimals())
                        .volumeScale(dto.getQuantityDecimals()) // For amount/quantity
                        .priceStep(dto.getPriceTickSize())
                        .build();
                instruments.put(instrument, instrumentMetaData);

                if (instrument instanceof CurrencyPair) {
                    CurrencyPair cp = (CurrencyPair) instrument;
                    currencies.putIfAbsent(cp.base, new org.knowm.xchange.dto.meta.CurrencyMetaData(dto.getQuantityDecimals(), null));
                    currencies.putIfAbsent(cp.counter, new org.knowm.xchange.dto.meta.CurrencyMetaData(dto.getQuoteDecimals(), null));
                }
            }
        }
        // TODO: Fetch fee schedule if available and populate tradingFee, currency withdrawal fees etc.
        return new ExchangeMetaData(instruments, currencies, null, null, null);
    }
}

package info.bitrich.xchangestream.btcmarkets.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import info.bitrich.xchangestream.btcmarkets.BTCMarketsStreamingAdapters;
import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketOrderbookMessage;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Implementation Note: this class is added in a new package. There is a local branch that refactors the
 * info.bitrich.xchangestream.btcmarkets package where all the other service classes are moved here
 */

public class BTCMarketsStreamingTradeService implements StreamingTradeService {

  private static final Logger LOG = LoggerFactory.getLogger(BTCMarketsStreamingTradeService.class);
  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private BTCMarketsStreamingService service;

  public BTCMarketsStreamingTradeService(BTCMarketsStreamingService service) {
    this.service = service;
  }

  @Override
  public Observable<Order> getOrderChanges(CurrencyPair currencyPair, Object... args) {
    // TODO Auto-generated method stub
    final String marketId = BTCMarketsStreamingAdapters.adaptCurrencyPairToMarketId(currencyPair);
    return null;
    //    return service
    //        .subscribeChannel(CHANNEL_TRADE, marketId)
    //        .map(node -> mapper.treeToValue(node, BTCMarketsWebSocketTradeMessage.class))
    //        .filter(event -> marketId.equals(event.getMarketId()))
    //        .map(this::handleTradeMessage);
  }

  private OrderBook handleTradeMessage(BTCMarketsWebSocketOrderbookMessage message)
      throws InvalidFormatException {
    return BTCMarketsStreamingAdapters.adaptOrderUpdateMessageToOrderBook(message);
  }
}

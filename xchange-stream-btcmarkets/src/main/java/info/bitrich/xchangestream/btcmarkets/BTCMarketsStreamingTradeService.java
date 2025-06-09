package info.bitrich.xchangestream.btcmarkets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import info.bitrich.xchangestream.btcmarkets.dto.BTCMarketsWebSocketOrderChangeMessage;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.rxjava3.core.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class BTCMarketsStreamingTradeService implements StreamingTradeService {

//  private static final Logger LOG = LoggerFactory.getLogger(BTCMarketsStreamingTradeService.class);
  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

  private BTCMarketsStreamingService service;

  public BTCMarketsStreamingTradeService(BTCMarketsStreamingService service) {
    this.service = service;
  }

  @Override
  public Observable<Order> getOrderChanges(CurrencyPair currencyPair, Object... args) {
    final String marketId = BTCMarketsStreamingAdapters.adaptCurrencyPairToMarketId(currencyPair);
//    return null;
        return service
            .subscribeChannel(BTCMarketsStreamingService.CHANNEL_ORDERCHANGE, marketId)
            .map(node -> mapper.treeToValue(node, BTCMarketsWebSocketOrderChangeMessage.class))
            .filter(event -> marketId.equals(event.getMarketId()))
            .map(this::handleOrderChangeMessage);
  }

  private Order handleOrderChangeMessage(BTCMarketsWebSocketOrderChangeMessage message)
      throws InvalidFormatException {
    return BTCMarketsStreamingAdapters.adaptOrderChangeMessageToOrder(message);
  }
  
//  private Order  
}

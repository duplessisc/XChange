package info.bitrich.xchangestream.btcmarkets;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingAccountService;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.NettyStreamingService;
import info.bitrich.xchangestream.service.netty.ConnectionStateModel.State;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.btcmarkets.BTCMarketsExchange;
import org.knowm.xchange.btcmarkets.service.BTCMarketsAccountService;
import org.knowm.xchange.btcmarkets.service.BTCMarketsTradeService;
import org.knowm.xchange.service.trade.TradeService;

public class BTCMarketsStreamingExchange extends BTCMarketsExchange implements StreamingExchange {

  private static final String API_URI = "wss://socket.btcmarkets.net/v2";

	private BTCMarketsStreamingService streamingService;
	private BTCMarketsStreamingMarketDataService streamingMarketDataService;
	private BTCMarketsStreamingTradeService streamingTradeService;
	private BTCMarketsStreamingAccountService streamingAccountService;


  @Override
  public Completable connect(ProductSubscription... args) {
    return streamingService.connect();
  }
  @Override
  public Observable<State> connectionStateObservable() {
    return streamingService.subscribeConnectionState();
  }

  @Override
  public Observable<Object> connectionSuccess() {
    return streamingService.subscribeConnectionSuccess();
  }

  private BTCMarketsStreamingService createStreamingService() {
    BTCMarketsStreamingService streamingService = new BTCMarketsStreamingService(API_URI, getNonceFactory());
    applyStreamingSpecification(getExchangeSpecification(), streamingService);
    if (StringUtils.isNotEmpty(exchangeSpecification.getApiKey())) {
	    streamingService.setApiKey(getExchangeSpecification().getApiKey());
	    streamingService.setApiSecret(getExchangeSpecification().getSecretKey());
    }
    return streamingService;
  }

  @Override
  public Completable disconnect() {
    return streamingService.disconnect();
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {
    ExchangeSpecification spec = super.getDefaultExchangeSpecification();
    spec.setShouldLoadRemoteMetaData(false);
    return spec;
  }

  @Override
  public StreamingMarketDataService getStreamingMarketDataService() {
    return streamingMarketDataService;
  }

  @Override
public StreamingTradeService getStreamingTradeService() {
	
	return (StreamingTradeService) this.streamingTradeService;
}

  @Override
  protected void initServices() {
    super.initServices();

    this.streamingService = createStreamingService();
    this.streamingMarketDataService = new BTCMarketsStreamingMarketDataService(streamingService);
    if (exchangeSpecification.getApiKey() != null && exchangeSpecification.getSecretKey() != null) {
        this.streamingTradeService = new BTCMarketsStreamingTradeService(this.streamingService);
        this.streamingAccountService = new BTCMarketsStreamingAccountService(this);
      }
  }

  @Override
  public boolean isAlive() {
    return streamingService.isSocketOpen();
  }

  @Override
  public Observable<Throwable> reconnectFailure() {
    return streamingService.subscribeReconnectFailure();
  }

  @Override
  public void useCompressedMessages(boolean compressedMessages) {
    streamingService.useCompressedMessages(compressedMessages);
  }
}

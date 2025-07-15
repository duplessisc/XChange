package org.knowm.xchange.cryptocom;

import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.cryptocom.service.CryptoComAccountService;
import org.knowm.xchange.cryptocom.service.CryptoComAPI;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.marketdata.CryptoComInstrumentsResponse;
import org.knowm.xchange.cryptocom.service.CryptoComAPI;
import org.knowm.xchange.cryptocom.service.CryptoComAuthenticatedAPI;
import org.knowm.xchange.cryptocom.service.CryptoComDigest;
import org.knowm.xchange.cryptocom.service.CryptoComAccountService;
import org.knowm.xchange.cryptocom.service.CryptoComMarketDataService;
import org.knowm.xchange.cryptocom.service.CryptoComTradeService;
import org.knowm.xchange.utils.nonce.CurrentTimeNonceFactory;
import si.mazi.rescu.RestProxyFactory;
import si.mazi.rescu.SynchronizedValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;


// Note: These service classes (CryptoComAccountService, etc.) are for the REST API.
// They will be different from the streaming service classes.
// For now, they can be placeholders if not yet implemented.

public class CryptoComExchange extends BaseExchange implements Exchange {
    private static final Logger LOG = LoggerFactory.getLogger(CryptoComExchange.class);
    private SynchronizedValueFactory<Long> nonceFactory = new CurrentTimeNonceFactory();
    private CryptoComAPI publicApi;
    private CryptoComAuthenticatedAPI authenticatedApi;
    private CryptoComDigest signatureCreator;

    @Override
    protected void initServices() {
        ExchangeSpecification spec = getExchangeSpecification();
        ClientConfig clientConfig = getClientConfig();

        this.publicApi = RestProxyFactory.createProxy(CryptoComAPI.class, spec.getSslUri(), clientConfig);

        if (spec.getApiKey() != null && spec.getSecretKey() != null) {
            this.signatureCreator = new CryptoComDigest(spec.getSecretKey()); // Use constructor
            // Note: For Crypto.com, the signature (and other auth params) are part of the request body.
            // ResCU's standard way of applying ParamsDigest (like ApiKeyHeaderSignatureCreator) won't directly work
            // to put `sig` into the JSON body. The raw service methods will need to:
            // 1. Prepare a CryptoComRequest DTO.
            // 2. Call signatureCreator.digestParams() by manually constructing RestInvocationParams from the DTO,
            //    or (better) have CryptoComDigest provide a method to sign a CryptoComRequest DTO directly.
            // 3. Put the returned signature into the CryptoComRequest DTO.
            // 4. Pass this DTO to the authenticatedApi proxy.
            // For now, we initialize authenticatedApi. The signing process will be handled in the Raw services.
            this.authenticatedApi = RestProxyFactory.createProxy(CryptoComAuthenticatedAPI.class, spec.getSslUri(), clientConfig);
        }


        this.marketDataService = new CryptoComMarketDataService(this);
        this.accountService = new CryptoComAccountService(this);
        this.tradeService = new CryptoComTradeService(this);
    }

    public CryptoComAPI getPublicApi() {
        return publicApi;
    }

    public CryptoComAuthenticatedAPI getAuthenticatedApi() {
        if (authenticatedApi == null) {
            throw new ExchangeException("Authenticated API not configured. Missing API key/secret in specification.");
        }
        return authenticatedApi;
    }

    public CryptoComDigest getSignatureCreator() {
         if (signatureCreator == null) {
            throw new ExchangeException("Signature creator not configured. Missing secret key in specification.");
        }
        return signatureCreator;
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
        exchangeSpecification.setSslUri("https://api.crypto.com/exchange/v1"); // Base REST API URL
        exchangeSpecification.setHost("api.crypto.com");
        exchangeSpecification.setPort(443);
        exchangeSpecification.setExchangeName("Crypto.com");
        exchangeSpecification.setExchangeDescription("Crypto.com Exchange (REST API)");
        return exchangeSpecification;
    }

    @Override
    public SynchronizedValueFactory<Long> getNonceFactory() {
        return nonceFactory;
    }

    @Override
    public void remoteInit() throws IOException {
        LOG.debug("Attempting remoteInit for Crypto.com");
        try {
            // marketDataService is an instance of CryptoComMarketDataService, which extends CryptoComMarketDataServiceRaw
            CryptoComMarketDataServiceRaw dataServiceRaw = (CryptoComMarketDataServiceRaw) this.marketDataService;
            CryptoComResponse<CryptoComInstrumentsResponse> instrumentsResponse = dataServiceRaw.getCryptoComInstruments();

            if (instrumentsResponse != null && instrumentsResponse.getResult() != null && instrumentsResponse.getResult().getInstruments() != null) {
                if (instrumentsResponse.getCode() == 0) { // Success code from API
                    this.exchangeMetaData = CryptoComAdapters.adaptToExchangeMetaData(instrumentsResponse.getResult().getInstruments());
                    LOG.debug("Remote init successful, loaded exchange metadata for Crypto.com.");
                } else {
                    throw new ExchangeException("Failed to load instruments for metadata from Crypto.com. Error code: " + instrumentsResponse.getCode() + ", Message: " + instrumentsResponse.getMessage());
                }
            } else {
                throw new ExchangeException("Failed to load instruments for metadata from Crypto.com, received null or empty response.");
            }
        } catch (IOException e) {
            LOG.warn("Remote init for Crypto.com failed due to IOException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.warn("Remote init for Crypto.com failed due to an unexpected exception: {}", e.getMessage());
            throw new ExchangeException("Remote init failed for Crypto.com", e);
        }
    }
}

package org.knowm.xchange.cryptocom.service;

import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.service.BaseExchangeService;
import org.knowm.xchange.service.BaseService;

public class CryptoComService extends BaseExchangeService implements BaseService {

    protected CryptoComService(CryptoComExchange exchange) {
        super(exchange);
    }

    // Common methods for REST services can go here
    // e.g., creating signed requests if this module handles authenticated REST calls.
}

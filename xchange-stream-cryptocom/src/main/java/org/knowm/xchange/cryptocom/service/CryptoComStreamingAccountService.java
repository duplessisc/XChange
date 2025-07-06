package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingAccountService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.rxjava3.core.Observable;
import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.dto.account.CryptoComBalanceEvent;
import org.knowm.xchange.cryptocom.dto.account.CryptoComPositionEvent;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CryptoComStreamingAccountService implements StreamingAccountService {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComStreamingAccountService.class);
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();

    private final CryptoComAuthenticatedStreamingService streamingService;

    public CryptoComStreamingAccountService(CryptoComAuthenticatedStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    /**
     * Subscribes to the user.balance channel for updates on account balances.
     * The Crypto.com user.balance message contains a list of 'position_balances',
     * each of which can be adapted to an XChange Balance object.
     * This method will emit each of these balances individually.
     */
    @Override
    public Observable<Balance> getBalanceChanges(Currency currency, Object... args) {
        // Crypto.com's user.balance channel sends all currency balances,
        // so we subscribe once and then filter if a specific currency is requested.
        final String channelName = "user.balance";

        return streamingService
            .subscribeChannel(channelName)
            .flatMap(jsonNode -> {
                // Docs: result.data is an array, but typically with one element containing all balance info.
                // Inside that element, result.data[0].position_balances is the array of individual currency balances.
                JsonNode dataContainerNode = jsonNode.at("/result/data/0"); // This should be the object holding "position_balances"
                if (dataContainerNode == null || dataContainerNode.isMissingNode() || !dataContainerNode.has("position_balances")) {
                    LOG.warn("Balance data (position_balances) not found or not in expected format in message: {}", jsonNode);
                    return Observable.empty();
                }

                JsonNode positionBalancesArray = dataContainerNode.get("position_balances");
                if (positionBalancesArray == null || !positionBalancesArray.isArray()) {
                    LOG.warn("'position_balances' is not an array or is missing in message: {}", jsonNode);
                    return Observable.empty();
                }

                List<Balance> balances = new ArrayList<>();
                for (JsonNode balanceNode : positionBalancesArray) {
                    try {
                        CryptoComBalanceEvent balanceEvent = objectMapper.treeToValue(balanceNode, CryptoComBalanceEvent.class);
                        Balance balance = CryptoComAdapters.adaptBalance(balanceEvent);
                        if (currency == null || balance.getCurrency().equals(currency)) {
                            balances.add(balance);
                        }
                    } catch (IOException e) {
                        LOG.error("Error parsing balance event: {}", balanceNode, e);
                    }
                }
                return Observable.fromIterable(balances);
            })
            .filter(balance -> balance != null);
    }

    /**
     * Subscribes to the user.positions channel for updates on open positions.
     * The Crypto.com user.positions message contains a list of open positions in the 'data' array.
     */
    @Override
    public Observable<Position> getPositionChanges(Object... args) {
        final String channelName = "user.positions";

        return streamingService
            .subscribeChannel(channelName)
            .flatMap(jsonNode -> {
                // Docs: result.data is an array of position objects
                JsonNode dataArray = jsonNode.at("/result/data");
                if (dataArray != null && dataArray.isArray()) {
                    List<Position> positions = new ArrayList<>();
                    for (JsonNode positionNode : dataArray) {
                        try {
                            CryptoComPositionEvent positionEvent = objectMapper.treeToValue(positionNode, CryptoComPositionEvent.class);
                            positions.add(CryptoComAdapters.adaptPosition(positionEvent));
                        } catch (IOException e) {
                            LOG.error("Error parsing position event: {}", positionNode, e);
                        }
                    }
                    return Observable.fromIterable(positions);
                } else {
                    LOG.warn("Position data is not an array or is missing in message: {}", jsonNode);
                }
                return Observable.empty();
            })
            .filter(position -> position != null);
    }
}

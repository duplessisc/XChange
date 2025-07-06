package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComPositionEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializePositionEvent() throws IOException {
        // Example from user.positions channel documentation
        String json = "{\n" +
                "      \"account_id\": \"52e7c00f-8716-4d6f-afdf-de334bde8ea5\",\n" +
                "      \"quantity\": \"0.0500\",\n" +
                "      \"session_unrealized_pnl\": \"-14.884000\",\n" +
                "      \"cost\": \"2561.516000\",\n" + // Position cost or value in USD
                "      \"open_position_pnl\": \"-7.302460\",\n" +
                "      \"open_pos_cost\": \"2561.328000\",\n" +
                "      \"session_pnl\": \"0.000000\",\n" +
                "      \"pos_initial_margin\": \"64.684453\",\n" +
                "      \"pos_maintenance_margin\": \"44.311397\",\n" +
                "      \"market_value\": \"2546.632000\",\n" +
                "      \"mark_price\": \"50932.6\",\n" +
                "      \"target_leverage\": \"50.00\",\n" +
                "      \"update_timestamp_ms\": 1613578676735,\n" +
                "      \"instrument_name\": \"BTCUSD-PERP\",\n" +
                "      \"type\": \"PERPETUAL_SWAP\"\n" +
                "    }";

        CryptoComPositionEvent event = mapper.readValue(json, CryptoComPositionEvent.class);

        assertThat(event.getAccountId()).isEqualTo("52e7c00f-8716-4d6f-afdf-de334bde8ea5");
        assertThat(event.getQuantity()).isEqualTo(new BigDecimal("0.0500"));
        assertThat(event.getSessionUnrealizedPnl()).isEqualTo(new BigDecimal("-14.884000"));
        assertThat(event.getCost()).isEqualTo(new BigDecimal("2561.516000"));
        assertThat(event.getOpenPositionPnl()).isEqualTo(new BigDecimal("-7.302460"));
        assertThat(event.getOpenPosCost()).isEqualTo(new BigDecimal("2561.328000"));
        assertThat(event.getSessionPnl()).isEqualTo(new BigDecimal("0.000000"));
        assertThat(event.getPosInitialMargin()).isEqualTo(new BigDecimal("64.684453"));
        assertThat(event.getPosMaintenanceMargin()).isEqualTo(new BigDecimal("44.311397"));
        assertThat(event.getMarketValue()).isEqualTo(new BigDecimal("2546.632000"));
        assertThat(event.getMarkPrice()).isEqualTo(new BigDecimal("50932.6"));
        assertThat(event.getTargetLeverage()).isEqualTo(new BigDecimal("50.00"));
        assertThat(event.getUpdateTimestampMs()).isEqualTo(1613578676735L);
        assertThat(event.getInstrumentName()).isEqualTo("BTCUSD-PERP");
        assertThat(event.getType()).isEqualTo("PERPETUAL_SWAP");
    }
}

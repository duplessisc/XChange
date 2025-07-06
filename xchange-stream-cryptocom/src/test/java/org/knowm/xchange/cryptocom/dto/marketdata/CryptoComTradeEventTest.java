package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComTradeEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializeTradeEvent() throws IOException {
        // Example from trade.{instrument_name} channel documentation
        String json = "{\n" +
                "      \"d\" : \"2030407068\",\n" +
                "      \"t\": 1613581138462,\n" +
                "      \"p\": \"51327.500000\",\n" +
                "      \"q\": \"0.000100\",\n" +
                "      \"s\": \"SELL\",\n" +
                "      \"i\": \"BTCUSD-PERP\",\n" +
                "      \"tn\": \"1613581138462000000\",\n" + // Added from public/get-trades example for completeness
                "      \"m\": \"76423\"\n" + // Added from public/get-trades example
                "    }";

        CryptoComTradeEvent event = mapper.readValue(json, CryptoComTradeEvent.class);

        assertThat(event.getTradeId()).isEqualTo(2030407068L);
        assertThat(event.getTimestamp()).isEqualTo(1613581138462L);
        assertThat(event.getPrice()).isEqualTo(new BigDecimal("51327.500000"));
        assertThat(event.getQuantity()).isEqualTo(new BigDecimal("0.000100"));
        assertThat(event.getSide()).isEqualTo("SELL");
        assertThat(event.getInstrumentName()).isEqualTo("BTCUSD-PERP");
        assertThat(event.getTimestampNano()).isEqualTo("1613581138462000000");
        assertThat(event.getMatchId()).isEqualTo("76423");

    }
}

package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComTickerEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializeTickerEvent() throws IOException {
        String json = "{\n" +
                "      \"h\": \"51790.00\",\n" +
                "      \"l\": \"47895.50\",\n" +
                "      \"a\": \"51174.500000\",\n" +
                "      \"i\": \"BTCUSD-PERP\",\n" +
                "      \"v\": \"879.5024\",\n" +
                "      \"vv\": \"26370000.12\",\n" +
                "      \"oi\": \"12345.12\",\n" +
                "      \"c\": \"0.03955106\",\n" +
                "      \"b\": \"51170.000000\",\n" +
                "      \"bs\": \"0.1000\",\n" +
                "      \"k\": \"51180.000000\",\n" +
                "      \"ks\": \"0.2000\",\n" +
                "      \"t\": 1613580710768\n" +
                "    }";

        CryptoComTickerEvent event = mapper.readValue(json, CryptoComTickerEvent.class);

        assertThat(event.getHigh()).isEqualTo(new BigDecimal("51790.00"));
        assertThat(event.getLow()).isEqualTo(new BigDecimal("47895.50"));
        assertThat(event.getLastTradePrice()).isEqualTo(new BigDecimal("51174.500000"));
        assertThat(event.getInstrumentName()).isEqualTo("BTCUSD-PERP");
        assertThat(event.getVolume()).isEqualTo(new BigDecimal("879.5024"));
        assertThat(event.getVolumeValue()).isEqualTo(new BigDecimal("26370000.12"));
        assertThat(event.getOpenInterest()).isEqualTo(new BigDecimal("12345.12"));
        assertThat(event.getChange()).isEqualTo(new BigDecimal("0.03955106"));
        assertThat(event.getBestBidPrice()).isEqualTo(new BigDecimal("51170.000000"));
        assertThat(event.getBestBidSize()).isEqualTo(new BigDecimal("0.1000"));
        assertThat(event.getBestAskPrice()).isEqualTo(new BigDecimal("51180.000000"));
        assertThat(event.getBestAskSize()).isEqualTo(new BigDecimal("0.2000"));
        assertThat(event.getTimestamp()).isEqualTo(1613580710768L);
    }
}

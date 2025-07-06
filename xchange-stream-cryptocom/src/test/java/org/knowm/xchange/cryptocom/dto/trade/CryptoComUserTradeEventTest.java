package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComUserTradeEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializeUserTradeEvent() throws IOException {
        // Example from user.trade.{instrument_name} channel documentation
        String json = "{\n" +
                "      \"account_id\": \"52e7c00f-1324-5a6z-bfgt-de445bde21a5\",\n" +
                "      \"event_date\": \"2021-02-17\",\n" +
                "      \"journal_type\": \"TRADING\",\n" +
                "      \"traded_quantity\": \"0.0500\",\n" +
                "      \"traded_price\": \"51278.5\",\n" +
                "      \"fees\": \"-1.025570\",\n" +
                "      \"order_id\": \"19708564\",\n" +
                "      \"trade_id\": \"38554669\",\n" +
                "      \"trade_match_id\": \"76423\",\n" +
                "      \"client_oid\":\"6ac2421d-5078-4ef6-a9d5-9680602ce123\",\n" +
                "      \"taker_side\":\"MAKER\",\n" +
                "      \"side\": \"BUY\",\n" +
                "      \"instrument_name\": \"BTCUSD-PERP\",\n" +
                "      \"fee_instrument_name\": \"USD\",\n" +
                "      \"create_time\": 1613570791060,\n" +
                "      \"create_time_ns\": \"1613570791060123456\",\n" +
                "      \"transaction_time\": \"1613570791060827635\",\n" + // Field name from user.trade doc
                "      \"match_count\": \"1\",\n" +
                "      \"match_index\": \"0\"\n" +
                "    }";

        CryptoComUserTradeEvent event = mapper.readValue(json, CryptoComUserTradeEvent.class);

        assertThat(event.getAccountId()).isEqualTo("52e7c00f-1324-5a6z-bfgt-de445bde21a5");
        assertThat(event.getEventDate()).isEqualTo("2021-02-17");
        assertThat(event.getJournalType()).isEqualTo("TRADING");
        assertThat(event.getTradedQuantity()).isEqualTo(new BigDecimal("0.0500"));
        assertThat(event.getTradedPrice()).isEqualTo(new BigDecimal("51278.5"));
        assertThat(event.getFees()).isEqualTo(new BigDecimal("-1.025570"));
        assertThat(event.getOrderId()).isEqualTo("19708564");
        assertThat(event.getTradeId()).isEqualTo("38554669");
        assertThat(event.getTradeMatchId()).isEqualTo("76423");
        assertThat(event.getClientOid()).isEqualTo("6ac2421d-5078-4ef6-a9d5-9680602ce123");
        assertThat(event.getTakerSide()).isEqualTo("MAKER");
        assertThat(event.getSide()).isEqualTo("BUY");
        assertThat(event.getInstrumentName()).isEqualTo("BTCUSD-PERP");
        assertThat(event.getFeeInstrumentName()).isEqualTo("USD");
        assertThat(event.getCreateTime()).isEqualTo(1613570791060L);
        assertThat(event.getCreateTimeNs()).isEqualTo("1613570791060123456");
        assertThat(event.getTransactionTime()).isEqualTo("1613570791060827635");
        assertThat(event.getMatchCount()).isEqualTo("1");
        assertThat(event.getMatchIndex()).isEqualTo("0");
    }
}

package org.knowm.xchange.cryptocom.dto.trade;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComUserOrderEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializeUserOrderEvent() throws IOException {
        // Example from user.order.{instrument_name} channel documentation
        String json = "{\n" +
                "      \"account_id\": \"52e7c00f-1324-5a6z-bfgt-de445bde21a5\",\n" +
                "      \"order_id\": \"19848525\",\n" +
                "      \"client_oid\": \"1613571154900\",\n" +
                "      \"order_type\": \"LIMIT\",\n" + // API uses "order_type"
                "      \"time_in_force\": \"GOOD_TILL_CANCEL\",\n" +
                "      \"side\": \"BUY\",\n" +
                "      \"exec_inst\": [\"POST_ONLY\"],\n" +
                "      \"quantity\": \"0.0100\",\n" +
                "      \"price\": \"50000.0\",\n" + // API uses "price" for limit price
                "      \"order_value\": \"500.000000\",\n" +
                "      \"maker_fee_rate\": \"0.000250\",\n" +
                "      \"taker_fee_rate\": \"0.000400\",\n" +
                "      \"avg_price\": \"0.0\",\n" +
                "      \"cumulative_quantity\": \"0.0000\",\n" +
                "      \"cumulative_value\": \"0.000000\",\n" +
                "      \"cumulative_fee\": \"0.000000\",\n" +
                "      \"status\": \"ACTIVE\",\n" +
                "      \"update_user_id\": \"fd797356-55db-48c2-a44d-157aabf702e8\",\n" +
                "      \"order_date\": \"2021-02-17\",\n" +
                "      \"instrument_name\": \"BTCUSD-PERP\",\n" +
                "      \"fee_instrument_name\": \"USD\",\n" +
                "      \"reason\": \"\",\n" + // Added for completeness
                "      \"create_time\": 1613575617173,\n" +
                "      \"create_time_ns\": \"1613575617173123456\",\n" +
                "      \"update_time\": 1613575617173,\n" +
                "      \"transaction_time_ns\": \"1613570791060827635\"\n" + // Added from docs
                "    }";

        CryptoComUserOrderEvent event = mapper.readValue(json, CryptoComUserOrderEvent.class);

        assertThat(event.getAccountId()).isEqualTo("52e7c00f-1324-5a6z-bfgt-de445bde21a5");
        assertThat(event.getOrderId()).isEqualTo("19848525");
        assertThat(event.getClientOid()).isEqualTo("1613571154900");
        assertThat(event.getType()).isEqualTo("LIMIT"); // DTO field is "type"
        assertThat(event.getTimeInForce()).isEqualTo("GOOD_TILL_CANCEL");
        assertThat(event.getSide()).isEqualTo("BUY");
        assertThat(event.getExecInst()).isEqualTo(Arrays.asList("POST_ONLY"));
        assertThat(event.getQuantity()).isEqualTo(new BigDecimal("0.0100"));
        assertThat(event.getLimitPrice()).isEqualTo(new BigDecimal("50000.0")); // DTO field is "limitPrice"
        assertThat(event.getOrderValue()).isEqualTo(new BigDecimal("500.000000"));
        assertThat(event.getMakerFeeRate()).isEqualTo(new BigDecimal("0.000250"));
        assertThat(event.getTakerFeeRate()).isEqualTo(new BigDecimal("0.000400"));
        assertThat(event.getAveragePrice()).isEqualTo(new BigDecimal("0.0"));
        assertThat(event.getCumulativeQuantity()).isEqualTo(new BigDecimal("0.0000"));
        assertThat(event.getCumulativeValue()).isEqualTo(new BigDecimal("0.000000"));
        assertThat(event.getCumulativeFee()).isEqualTo(new BigDecimal("0.000000"));
        assertThat(event.getStatus()).isEqualTo("ACTIVE");
        assertThat(event.getUpdateUserId()).isEqualTo("fd797356-55db-48c2-a44d-157aabf702e8");
        assertThat(event.getOrderDate()).isEqualTo("2021-02-17");
        assertThat(event.getInstrumentName()).isEqualTo("BTCUSD-PERP");
        assertThat(event.getFeeInstrumentName()).isEqualTo("USD");
        assertThat(event.getReason()).isEqualTo("");
        assertThat(event.getCreateTime()).isEqualTo(1613575617173L);
        assertThat(event.getCreateTimeNs()).isEqualTo("1613575617173123456");
        assertThat(event.getUpdateTime()).isEqualTo(1613575617173L);
        assertThat(event.getTransactionTimeNs()).isEqualTo("1613570791060827635");

        // TODO: Add test for order with trigger price (ref_price) once DTO is confirmed to have it
        // String jsonStopOrder = "..." (with ref_price, ref_price_type)
        // CryptoComUserOrderEvent stopEvent = mapper.readValue(jsonStopOrder, CryptoComUserOrderEvent.class);
        // assertThat(stopEvent.getRefPrice()).isEqualTo(new BigDecimal("..."));
    }
}

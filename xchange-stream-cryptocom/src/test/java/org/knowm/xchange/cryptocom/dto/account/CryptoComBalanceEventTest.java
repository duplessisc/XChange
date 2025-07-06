package org.knowm.xchange.cryptocom.dto.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComBalanceEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializeBalanceEvent() throws IOException {
        // Example element from "position_balances" array in user.balance channel
        String json = "{\n" +
                "          \"instrument_name\": \"CRO\",\n" +
                "          \"quantity\": \"24422.72427884\",\n" +
                "          \"market_value\": \"4776.107959969951\",\n" +
                "          \"collateral_eligible\": true,\n" + // Note: boolean in JSON
                "          \"haircut\": \"0.5\",\n" +
                "          \"collateral_amount\": \"4776.007959969951\",\n" + // Example value, might differ based on actual haircut calc
                "          \"max_withdrawal_balance\": \"24422.72427884\",\n" +
                "          \"reserved_qty\" : \"0.00000000\"\n" +
                "        }";

        CryptoComBalanceEvent event = mapper.readValue(json, CryptoComBalanceEvent.class);

        assertThat(event.getInstrumentName()).isEqualTo("CRO");
        assertThat(event.getQuantity()).isEqualTo(new BigDecimal("24422.72427884"));
        assertThat(event.getMarketValue()).isEqualTo(new BigDecimal("4776.107959969951"));
        assertThat(event.isCollateralEligible()).isTrue();
        assertThat(event.getHaircut()).isEqualTo(new BigDecimal("0.5"));
        assertThat(event.getCollateralAmount()).isEqualTo(new BigDecimal("4776.007959969951"));
        assertThat(event.getMaxWithdrawalBalance()).isEqualTo(new BigDecimal("24422.72427884"));
        assertThat(event.getReservedQty()).isEqualTo(new BigDecimal("0.00000000"));
    }
}

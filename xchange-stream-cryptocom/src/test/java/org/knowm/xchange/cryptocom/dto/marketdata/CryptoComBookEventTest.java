package org.knowm.xchange.cryptocom.dto.marketdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComBookEventTest {

    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @Test
    void testDeserializeBookSnapshotEvent() throws IOException {
        // Example from book.{instrument_name}.{depth} channel documentation (SNAPSHOT)
        String json = "{\n" +
                "    \"instrument_name\": \"BTCUSD-PERP\",\n" + // This is usually in the wrapper, but DTO can handle it
                "    \"depth\": 10,\n" + // Also usually in wrapper
                "    \"asks\": [\n" +
                "        [\"30082.5\", \"0.1689\", \"1\"],\n" +
                "        [\"30083.0\", \"0.1288\", \"1\"]\n" +
                "      ],\n" +
                "    \"bids\": [\n" +
                "        [\"30079.0\", \"0.0505\", \"1\"],\n" +
                "        [\"30077.5\", \"1.0527\", \"2\"]\n" +
                "      ],\n" +
                "    \"t\": 1654780033786,\n" +  // Message publish time
                "    \"tt\": 1654780033755,\n" + // Last book update time
                "    \"u\": 542048017824\n" +
                "  }";
        // Note: The actual API sends this structure within result.data[0] for snapshots.
        // The DTO is designed to map the inner structure.
        // The CryptoComStreamingMarketDataService handles extracting this inner dataNode.

        CryptoComBookEvent event = mapper.readValue(json, CryptoComBookEvent.class);

        // Assertions for direct fields (if the JSON matches the event structure directly)
        // assertThat(event.getInstrumentName()).isEqualTo("BTCUSD-PERP"); // This would be populated by the service usually
        // assertThat(event.getDepth()).isEqualTo(10); // This would be populated by the service

        assertThat(event.getLastUpdateTimestamp()).isEqualTo(1654780033755L); // tt
        assertThat(event.getTimestamp()).isEqualTo(1654780033786L);       // t
        assertThat(event.getUpdateSequence()).isEqualTo(542048017824L);    // u

        List<CryptoComOrderBookEntry> asks = event.getAsks();
        assertThat(asks).hasSize(2);
        assertThat(asks.get(0).getPrice()).isEqualTo(new BigDecimal("30082.5"));
        assertThat(asks.get(0).getQuantity()).isEqualTo(new BigDecimal("0.1689"));
        assertThat(asks.get(0).getCount()).isEqualTo(1L);

        List<CryptoComOrderBookEntry> bids = event.getBids();
        assertThat(bids).hasSize(2);
        assertThat(bids.get(1).getPrice()).isEqualTo(new BigDecimal("30077.5"));
        assertThat(bids.get(1).getQuantity()).isEqualTo(new BigDecimal("1.0527"));
        assertThat(bids.get(1).getCount()).isEqualTo(2L);
    }

    @Test
    void testDeserializeBookSnapshotEventWithDataWrapper() throws IOException {
        // Simulating the structure where the book data is wrapped in a "data": [{...}] array
        // as it comes in the actual websocket message: result.data[0]
        String jsonWrapper = "{\n" +
            "  \"instrument_name\": \"ETH_CRO\",\n" + // This is in the outer result usually
            "  \"subscription\": \"book.ETH_CRO.10\",\n" + // This is in the outer result
            "  \"channel\": \"book\",\n" + // This is in the outer result
            "  \"depth\": 10,\n" + // This is in the outer result
            "  \"data\": [\n" +
            "    {\n" +
            "      \"asks\": [\n" +
            "        [\"500.0\", \"10.0\", \"1\"],\n" +
            "        [\"500.1\", \"15.5\", \"2\"]\n" +
            "      ],\n" +
            "      \"bids\": [\n" +
            "        [\"499.9\", \"20.0\", \"3\"],\n" +
            "        [\"499.8\", \"25.2\", \"4\"]\n" +
            "      ],\n" +
            "      \"t\": 1678886400100,\n" +
            "      \"tt\": 1678886400000,\n" +
            "      \"u\": 1234567890123\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        // The CryptoComBookEvent DTO is designed to map the *content* of data[0]
        // or the direct structure if data[0] is passed to it.
        // So, we first extract the data[0] node.
        JsonNode rootNode = mapper.readTree(jsonWrapper);
        JsonNode dataContentNode = rootNode.path("data").get(0);

        CryptoComBookEvent event = mapper.treeToValue(dataContentNode, CryptoComBookEvent.class);

        // Populate fields that would normally be in the wrapper if needed for full DTO state
        // event.setInstrumentName(rootNode.path("instrument_name").asText());
        // event.setDepth(rootNode.path("depth").asInt());


        assertThat(event.getLastUpdateTimestamp()).isEqualTo(1678886400000L);
        assertThat(event.getTimestamp()).isEqualTo(1678886400100L);
        assertThat(event.getUpdateSequence()).isEqualTo(1234567890123L);

        List<CryptoComOrderBookEntry> asks = event.getAsks();
        assertThat(asks).hasSize(2);
        assertThat(asks.get(0).getPrice()).isEqualTo(new BigDecimal("500.0"));
        assertThat(asks.get(0).getQuantity()).isEqualTo(new BigDecimal("10.0"));

        List<CryptoComOrderBookEntry> bids = event.getBids();
        assertThat(bids).hasSize(2);
        assertThat(bids.get(0).getPrice()).isEqualTo(new BigDecimal("499.9"));
        assertThat(bids.get(0).getQuantity()).isEqualTo(new BigDecimal("20.0"));
    }
}

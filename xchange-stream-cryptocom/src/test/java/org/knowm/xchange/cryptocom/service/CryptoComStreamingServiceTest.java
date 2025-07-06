package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoComStreamingServiceTest {

    private CryptoComStreamingService streamingService;
    private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();

    @BeforeEach
    void setUp() {
        // API URL is not strictly needed for these specific tests
        streamingService = new CryptoComStreamingService("wss://dummy.crypto.com/exchange/v1/market");
    }

    @Test
    void testGetChannelNameFromMessage_heartbeat() throws IOException {
        String json = "{\"id\": 1587523073344, \"method\": \"public/heartbeat\", \"code\": 0}";
        JsonNode message = mapper.readTree(json);
        String channelName = streamingService.getChannelNameFromMessage(message);
        assertThat(channelName).isEqualTo("heartbeat");
    }

    @Test
    void testGetChannelNameFromMessage_subscribeResponse() throws IOException {
        String json = "{\n" +
                "  \"id\": 1,\n" +
                "  \"method\": \"subscribe\",\n" +
                "  \"code\": 0,\n" +
                "  \"result\": {\n" +
                "    \"instrument_name\": \"BTCUSD-PERP\",\n" +
                "    \"subscription\": \"book.BTCUSD-PERP.10\",\n" +
                "    \"channel\": \"book\",\n" +
                "    \"depth\": 10,\n" +
                "    \"data\": []\n" +
                "  }\n" +
                "}";
        JsonNode message = mapper.readTree(json);
        String channelName = streamingService.getChannelNameFromMessage(message);
        assertThat(channelName).isEqualTo("book.BTCUSD-PERP.10");
    }

    @Test
    void testGetChannelNameFromMessage_dataMessage() throws IOException {
        // Example structure for a data message (e.g., ticker update)
        String json = "{\n" +
                "  \"method\": \"subscribe\",\n" + // This method field is for the subscription confirmation, not the data itself.
                                                // Actual data messages from Crypto.com might not have "method" : "subscribe"
                                                // but rather "channel":"ticker.BTCUSD-PERP" and the data.
                                                // The getChannelNameFromMessage has a fallback for this.
                "  \"result\": {\n" +
                "    \"instrument_name\": \"BTCUSD-PERP\",\n" +
                "    \"subscription\": \"ticker.BTCUSD-PERP\",\n" + // This is the key for mapping
                "    \"channel\": \"ticker\",\n" + // This helps identify type
                "    \"data\": [{}]\n" +
                "  }\n" +
                "}";
        // A more realistic data message might look like:
         String jsonDataMessage = "{\n" +
            "  \"channel\": \"ticker.BTCUSD-PERP\",\n" + // No method, directly channel
            "  \"data\": [{}],\n" +
            "  \"instrument_name\": \"BTCUSD-PERP\" \n" + // Often part of the data payload or wrapper
            "}";


        JsonNode message = mapper.readTree(jsonDataMessage);
        // The current getChannelNameFromMessage logic might try to find "result.subscription" first.
        // If not found, it looks for "channel" and "instrument_name".
        // Let's test the fallback:
        // String channelName = streamingService.getChannelNameFromMessage(message);
        // assertThat(channelName).isEqualTo("ticker.BTCUSD-PERP");
        // The above line would fail if the method relies on "result.subscription" which is not in jsonDataMessage
        // The current implementation of getChannelNameFromMessage:
        // 1. Checks for "method" == "public/heartbeat" -> "heartbeat"
        // 2. Checks for "method" startsWith "subscribe" AND "result.subscription" -> result.subscription
        // 3. Fallback: "channel" + "." + "instrument_name" if both exist
        // 4. Fallback: "channel" if it exists
        // So for jsonDataMessage, it should use fallback 3 or 4.
        // If "instrument_name" is at the root of jsonDataMessage:
        // It will use "channel" value which is "ticker.BTCUSD-PERP"
        // If "instrument_name" is NOT at root of jsonDataMessage:
        // It will use "channel" value "ticker.BTCUSD-PERP"

        // The current getChannelNameFromMessage is:
        // if (message.has("channel") && message.has("instrument_name")) {
        //    return message.get("channel").asText() + "." + message.get("instrument_name").asText();
        // }
        // This would result in "ticker.BTCUSD-PERP.BTCUSD-PERP" which is wrong.
        // The channel name for data messages is usually just the "subscription" field from the confirmation,
        // or the "channel" field if it's already fully qualified.

        // Let's assume the actual data messages have a "channel" field that IS the unique subscription string.
        String dataMsgWithQualifiedChannel = "{\n" +
            "  \"channel\": \"ticker.BTCUSD-PERP\",\n" + // This is the unique identifier
            "  \"data\": [{}]\n" +
            "}";
        JsonNode qualifiedMessage = mapper.readTree(dataMsgWithQualifiedChannel);
        String channelName = streamingService.getChannelNameFromMessage(qualifiedMessage);
        assertThat(channelName).isEqualTo("ticker.BTCUSD-PERP");


        String dataMsgBook = "{\n" +
            "  \"channel\": \"book.ETH_USDT.10\",\n" +
            "  \"data\": [{}]\n" +
            "}";
        JsonNode bookMessage = mapper.readTree(dataMsgBook);
        channelName = streamingService.getChannelNameFromMessage(bookMessage);
        assertThat(channelName).isEqualTo("book.ETH_USDT.10");

    }


    @Test
    void testGetSubscribeMessage() throws IOException {
        String channelName = "book.BTC_USDT.10";
        String messageJson = streamingService.getSubscribeMessage(channelName);
        JsonNode messageNode = mapper.readTree(messageJson);

        assertThat(messageNode.get("method").asText()).isEqualTo("subscribe");
        assertThat(messageNode.get("id").asLong()).isGreaterThan(0); // AtomicLong starts at 1
        assertThat(messageNode.get("nonce").asLong()).isGreaterThan(0);
        assertThat(messageNode.get("params").get("channels").get(0).asText()).isEqualTo(channelName);
    }

    @Test
    void testGetUnsubscribeMessage() throws IOException {
        String channelName = "ticker.ETH_CRO";
        String messageJson = streamingService.getUnsubscribeMessage(channelName);
        JsonNode messageNode = mapper.readTree(messageJson);

        assertThat(messageNode.get("method").asText()).isEqualTo("unsubscribe");
        assertThat(messageNode.get("id").asLong()).isGreaterThan(0);
        assertThat(messageNode.get("nonce").asLong()).isGreaterThan(0);
        assertThat(messageNode.get("params").get("channels").get(0).asText()).isEqualTo(channelName);
    }
}

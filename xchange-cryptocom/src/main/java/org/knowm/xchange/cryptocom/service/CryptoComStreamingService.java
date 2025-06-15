package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import org.knowm.xchange.cryptocom.dto.CryptoComRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class CryptoComStreamingService extends JsonNettyStreamingService {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoComStreamingService.class);
    private final ObjectMapper objectMapper = StreamingObjectMapperHelper.getObjectMapper();
    private final AtomicLong requestCounter = new AtomicLong(1);

    private final String apiKey;
    private final String secretKey;

    public CryptoComStreamingService(String apiUrl, String apiKey, String secretKey) {
        super(apiUrl, Integer.MAX_VALUE);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public CryptoComStreamingService(String apiUrl) {
        this(apiUrl, null, null);
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        if (message.has("method")) {
            String method = message.get("method").asText();
            if ("public/heartbeat".equals(method)) {
                return "heartbeat";
            }
            if (method.startsWith("subscribe") && message.has("result")) {
                JsonNode result = message.get("result");
                if (result.has("subscription")) {
                    return result.get("subscription").asText();
                }
            }
        }
        // Fallback or more specific parsing needed for actual data messages
        if (message.has("channel") && message.has("instrument_name")) {
            return message.get("channel").asText() + "." + message.get("instrument_name").asText();
        }
        if (message.has("channel")) {
            return message.get("channel").asText();
        }
        LOG.warn("Cannot determine channel from message: {}", message.toString());
        return "unknown";
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(requestCounter.getAndIncrement());
        request.setMethod("subscribe");
        request.setNonce(System.currentTimeMillis());

        ObjectNode params = objectMapper.createObjectNode();
        params.putArray("channels").add(channelName);
        request.setParams(params);

        // TODO: Add signature for private channels if apiKey and secretKey are present
        // if (isPrivateChannel(channelName) && apiKey != null && secretKey != null) {
        //    signRequest(request);
        // }

        return objectMapper.writeValueAsString(request);
    }

    @Override
    public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(requestCounter.getAndIncrement());
        request.setMethod("unsubscribe");
        request.setNonce(System.currentTimeMillis());

        ObjectNode params = objectMapper.createObjectNode();
        params.putArray("channels").add(channelName);
        request.setParams(params);

        // TODO: Add signature for private channels
        // if (isPrivateChannel(channelName) && apiKey != null && secretKey != null) {
        //    signRequest(request);
        // }
        return objectMapper.writeValueAsString(request);
    }

    public String getAuthenticateMessage() throws IOException {
        if (apiKey == null || secretKey == null) {
            throw new IllegalStateException("API key and secret key required for authentication");
        }
        CryptoComRequest request = new CryptoComRequest();
        request.setId(requestCounter.getAndIncrement());
        request.setMethod("public/auth");
        request.setApiKey(apiKey);
        request.setNonce(System.currentTimeMillis());
        // Signature must be calculated based on Crypto.com's specific algorithm
        // request.setSig(calculateSignature(request, secretKey));
        // For now, we'll omit the actual signature calculation as it's complex
        // and requires a separate utility. This will likely fail authentication.
        LOG.warn("Signature calculation for public/auth is not yet implemented. Authentication will likely fail.");
        request.setSig("DUMMY_SIGNATURE_NEEDS_IMPLEMENTATION");
        return objectMapper.writeValueAsString(request);
    }


    @Override
    protected void handleMessage(JsonNode message) {
        super.handleMessage(message);
        if (message.has("method") && "public/heartbeat".equals(message.get("method").asText())) {
            try {
                long heartbeatId = message.get("id").asLong();
                sendMessage(getRespondHeartbeatMessage(heartbeatId));
            } catch (IOException e) {
                LOG.error("Failed to send heartbeat response", e);
            }
        }
    }

    private String getRespondHeartbeatMessage(long id) throws IOException {
        CryptoComRequest request = new CryptoComRequest();
        request.setId(id); // Echo the id from the heartbeat request
        request.setMethod("public/respond-heartbeat");
        // Nonce might not be strictly required for respond-heartbeat by all exchanges,
        // but Crypto.com docs show nonce in general request format.
        // For simplicity, we can add it. Or check if it's truly needed for this specific message.
        // request.setNonce(System.currentTimeMillis());
        return objectMapper.writeValueAsString(request);
    }

    // TODO: Implement actual signature calculation logic
    // private String calculateSignature(CryptoComRequest request, String secret) {
    //    // ... implementation based on Crypto.com documentation ...
    //    // method + id + api_key + paramsString + nonce
    //    return "generated_signature";
    // }

    // TODO: Determine if a channel is private
    // private boolean isPrivateChannel(String channelName) {
    //    return channelName.startsWith("user.");
    // }

    @Override
    public void resubscribeChannels() {
        try {
            if (this.isSocketOpen()) {
                // Resubscribe to all channels
                for (String channelName : channels.keySet()) {
                    // For Crypto.com, channels are identified like "book.BTC_USDT.10"
                    // The 'args' used during initial subscription might be needed if they were more complex
                    // than just the channel name itself. Here, assuming channelName is the full identifier.
                    String subscribeMessage = getSubscribeMessage(channelName);
                    this.sendMessage(subscribeMessage);
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to resubscribe channels", e);
        }
    }
}

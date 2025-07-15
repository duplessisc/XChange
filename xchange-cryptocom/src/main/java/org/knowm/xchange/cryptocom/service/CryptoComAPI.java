package org.knowm.xchange.cryptocom.service;

import com.fasterxml.jackson.databind.JsonNode; // Using JsonNode for generic responses for now
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Interface for Crypto.com public REST API endpoints.
 * Base URL for ResCU can be set in ExchangeSpecification (e.g., https://api.crypto.com)
 * Common path prefix for these methods is /exchange/v1
 */
@Path("/exchange/v1")
@Produces(MediaType.APPLICATION_JSON)
public interface CryptoComAPI {

    @GET
    @Path("public/get-instruments")
    JsonNode getInstruments() throws IOException;

    @GET
    @Path("public/get-book")
    JsonNode getBook(
            @QueryParam("instrument_name") String instrumentName,
            @QueryParam("depth") Integer depth) throws IOException; // Depth: e.g., 10, 50, 150

    @GET
    @Path("public/get-candlestick")
    JsonNode getCandlestick(
            @QueryParam("instrument_name") String instrumentName,
            @QueryParam("timeframe") String timeframe, // e.g., 1m, 5m, 15m, 30m, 1h, 2h, 4h, 12h, 1D, 7D, 14D, 1M
            @QueryParam("count") Integer count, // Default 25, Max 1000 according to some general API behaviors
            @QueryParam("start_ts") Long startTs, // Unix timestamp ms
            @QueryParam("end_ts") Long endTs     // Unix timestamp ms
    ) throws IOException;

    @GET
    @Path("public/get-trades")
    JsonNode getTrades(
            @QueryParam("instrument_name") String instrumentName, // Optional, can be for all or specific
            @QueryParam("count") Integer count, // Default 25, Max 150
            @QueryParam("start_ts") Long startTs, // Unix timestamp ms, inclusive
            @QueryParam("end_ts") Long endTs     // Unix timestamp ms, exclusive
    ) throws IOException;

    @GET
    @Path("public/get-tickers")
    JsonNode getTickers(@QueryParam("instrument_name") String instrumentName) throws IOException; // Optional for all tickers

}

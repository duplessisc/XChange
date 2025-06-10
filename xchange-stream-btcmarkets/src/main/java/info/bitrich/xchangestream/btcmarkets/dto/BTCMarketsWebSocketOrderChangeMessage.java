package info.bitrich.xchangestream.btcmarkets.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.knowm.xchange.utils.jackson.ISO8601DateDeserializer;
import org.knowm.xchange.utils.jackson.MillisecTimestampDeserializer;

/**
 * 
 * Example Message: { orderId: 79033, marketId: 'BTC-AUD', side: 'Bid', type:
 * 'Limit', openVolume: '0', status: 'Fully Matched', triggerStatus: '',
 * timestamp: '2019-04-08T20:50:39.658Z', trades: [{ tradeId: 31727, price:
 * '0.1634', volume: '10', fee: '0.001', liquidityType: 'Taker',
 * valueInQuoteAsset: '1.634' }], messageType: 'orderChange', clientOrderId:
 * 'abc-id' }
 */
public class BTCMarketsWebSocketOrderChangeMessage {
	public class OrderChangeTrade {

		private final String tradeId;

		private final BigDecimal price;

		private final BigDecimal volume;

		private final BigDecimal fee;

		private final String liquidityType;

		private final BigDecimal valueInQouteAsset;

		public OrderChangeTrade(@JsonProperty("tradeId") String tradeId, @JsonProperty("price") BigDecimal price,
				@JsonProperty("volume") BigDecimal volume, @JsonProperty("fee") BigDecimal fee,
				@JsonProperty("liquidityType") String liquidityType,
				@JsonProperty("valueInQouteAsset") BigDecimal valueInQouteAsset) {
			// super();
			this.tradeId = tradeId;
			this.price = price;
			this.volume = volume;
			this.fee = fee;
			this.liquidityType = liquidityType;
			this.valueInQouteAsset = valueInQouteAsset;
		}

		public BigDecimal getFee() {
			return fee;
		}

		public String getLiquidityType() {
			return liquidityType;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public String getTradeId() {
			return tradeId;
		}

		public BigDecimal getValueInQouteAsset() {
			return valueInQouteAsset;
		}

		public BigDecimal getVolume() {
			return volume;
		}

	}

	private final String orderId;

	private final String marketId;

	private final String side;

	private final String type;

	private final BigDecimal openVolume;
	
	private final String status;

	private final String triggerStatus;

	 @JsonDeserialize(using = ISO8601DateDeserializer.class)
	private final Date timestamp;

	private final List<OrderChangeTrade> trades;

	private final String messageType;

	private final String clientOrderId;

	public BTCMarketsWebSocketOrderChangeMessage(@JsonProperty("orderId") String orderId,
			@JsonProperty("marketId") String marketId, @JsonProperty("side") String side,
			@JsonProperty("type") String type, @JsonProperty("openVolume") BigDecimal openVolume,
			@JsonProperty("triggerStatus") String triggerStatus, @JsonProperty("status") String status,
			@JsonProperty("timestamp") Date timestamp,
			@JsonProperty("trades") List<OrderChangeTrade> trades, @JsonProperty("messageType") String messageType,
			@JsonProperty("clientOrderId") String clientOrderId) {

		this.orderId = orderId;
		this.marketId = marketId;
		this.side = side;
		this.type = type;
		this.openVolume = openVolume;
		this.status = status;
		this.triggerStatus = triggerStatus;
		this.timestamp = timestamp;
		this.trades = trades;
		this.messageType = messageType;
		this.clientOrderId = clientOrderId;
	}

	public String getClientOrderId() {
		return clientOrderId;
	}

	public String getMarketId() {
		return marketId;
	}

	public String getMessageType() {
		return messageType;
	}

	public BigDecimal getOpenVolume() {
		return openVolume;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getSide() {
		return side;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public List<OrderChangeTrade> getTrades() {
		return trades;
	}

	public String getTriggerStatus() {
		return triggerStatus;
	}

	public String getType() {
		return type;
	}

	public String getStatus() {
		return status;
	}

}

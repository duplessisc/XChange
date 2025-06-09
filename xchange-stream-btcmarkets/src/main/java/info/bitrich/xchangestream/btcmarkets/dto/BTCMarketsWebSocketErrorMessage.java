package info.bitrich.xchangestream.btcmarkets.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class BTCMarketsWebSocketErrorMessage {

  private final String code;

  private final String message;

  private final String messageType;

  public BTCMarketsWebSocketErrorMessage(
      @JsonProperty("code") String code,
      @JsonProperty("message") String message,
      @JsonProperty("messageType") String messageType) {
    this.code = code;
    this.message = message;
    this.messageType = messageType;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String getMessageType() {
    return messageType;
  }

}

package org.knowm.xchange.btcmarkets.dto.v3.account;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/** @author Christiaan du Plessis **/ 
public class BTCMarketsAccountBalanceResponse {
//	Sample message
//	"assetName": "LTC",
//	"balance": "5",
//	"available": "5",
//	"locked": "0"
	
	private final String assetName;
	private final BigDecimal balance;
	private final BigDecimal available;
	private final BigDecimal locked;
	
	public BTCMarketsAccountBalanceResponse( 
			@JsonProperty("assetName") String assetName,  
			@JsonProperty("balance") BigDecimal balance,  
			@JsonProperty("available") BigDecimal available,
			 @JsonProperty("locked") BigDecimal locked) {
		super();
		this.assetName = assetName;
		this.balance = balance;
		this.available = available;
		this.locked = locked;
	}
	
	public String getAssetName() {
		return assetName;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public BigDecimal getAvailable() {
		return available;
	}

	public BigDecimal getLocked() {
		return locked;
	}


	
	
	
}

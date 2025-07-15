package org.knowm.xchange.cryptocom.service;

import org.knowm.xchange.cryptocom.CryptoComAdapters;
import org.knowm.xchange.cryptocom.CryptoComExchange;
import org.knowm.xchange.cryptocom.dto.CryptoComResponse;
import org.knowm.xchange.cryptocom.dto.account.CryptoComAccountBalanceResponse;
import org.knowm.xchange.cryptocom.dto.account.CryptoComUserBalanceDetail;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.Wallet; // Import Wallet
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CryptoComAccountService extends CryptoComAccountServiceRaw implements AccountService {

    public CryptoComAccountService(CryptoComExchange exchange) {
        super(exchange);
    }

    @Override
    public AccountInfo getAccountInfo() throws IOException {
        CryptoComResponse<CryptoComAccountBalanceResponse> response = getCryptoComAccountBalance();

        if (response == null || response.getResult() == null || response.getResult().getPrimaryBalanceDetail() == null) {
            throw new IOException("Failed to get account balance, response or primary balance detail is null. Response: " + response);
        }

        CryptoComUserBalanceDetail balanceDetail = response.getResult().getPrimaryBalanceDetail();
        String username = exchange.getExchangeSpecification().getUserName(); // Or derive from API if possible

        List<Balance> balances = balanceDetail.getPositionBalances().stream()
                .map(CryptoComAdapters::adaptBalance)
                .collect(Collectors.toList());

        // Create a default wallet with these balances
        Wallet wallet = Wallet.Builder.from(balances).id("spot").features(Wallet.WalletFeature.TRADING, Wallet.WalletFeature.FUNDING).build();


        // TODO: Fetch and include open positions if desired for AccountInfo,
        // though often getOpenPositions() is on TradeService.
        // For now, AccountInfo will primarily contain balances in one wallet.

        // The overall account values (like total_available_balance, total_margin_balance)
        // are in USD (or equivalent) according to the instrument_name in balanceDetail.
        // These are not directly part of XChange AccountInfo's simple List<Balance> structure,
        // but could be logged or used to populate custom extended AccountInfo if needed.
        // For now, we focus on the list of individual currency balances.

        return new AccountInfo(username, wallet); // Use wallet
    }

    @Override
    public String withdrawFunds(Currency currency, BigDecimal amount, String address) throws IOException {
        throw new NotYetImplementedForExchangeException("withdrawFunds not yet implemented.");
    }

    @Override
    public String withdrawFunds(WithdrawFundsParams params) throws IOException {
        throw new NotYetImplementedForExchangeException("withdrawFunds with params not yet implemented.");
    }

    @Override
    public String requestDepositAddress(Currency currency, Object... args) throws IOException {
        throw new NotYetImplementedForExchangeException("requestDepositAddress not yet implemented.");
    }

    @Override
    public TradeHistoryParams createFundingHistoryParams() {
        throw new NotYetImplementedForExchangeException("createFundingHistoryParams not yet implemented.");
    }

    @Override
    public List<FundingRecord> getFundingHistory(TradeHistoryParams params) throws IOException {
        throw new NotYetImplementedForExchangeException("getFundingHistory not yet implemented.");
    }

    // TODO: Implement getOpenPositions if it's to be part of AccountService,
    // otherwise it's typically in TradeService.
    // public List<OpenPosition> getOpenPositions() throws IOException;
}

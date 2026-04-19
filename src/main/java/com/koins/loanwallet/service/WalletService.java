package com.koins.loanwallet.service;

import com.koins.loanwallet.dto.request.FundWalletRequest;
import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.dto.response.WalletResponse;

import java.util.List;

public interface WalletService {

    WalletResponse getMyWallet();

    WalletResponse fundWallet(FundWalletRequest request);

    List<TransactionResponse> getMyTransactions();
}
package com.koins.loanwallet.service;

import com.koins.loanwallet.dto.request.ApplyLoanRequest;
import com.koins.loanwallet.dto.request.RepayLoanRequest;
import com.koins.loanwallet.dto.response.LoanResponse;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    LoanResponse applyForLoan(ApplyLoanRequest request);

    LoanResponse approveLoan(UUID loanId);

    LoanResponse disburseLoan(UUID loanId);

    LoanResponse repayLoan(UUID loanId, RepayLoanRequest request);

    LoanResponse getLoanById(UUID loanId);

    List<LoanResponse> getMyLoans();
}
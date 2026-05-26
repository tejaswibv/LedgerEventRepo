package com.event.ledger.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceResponse {

    private String accountId;

    private BigDecimal balance;
    
    public BalanceResponse(String accountId, BigDecimal balance) {
    	this.accountId = accountId;
    	this.balance = balance;
    }
}
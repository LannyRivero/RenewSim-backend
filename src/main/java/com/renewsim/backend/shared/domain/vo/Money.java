package com.renewsim.backend.shared.domain.vo;

import java.math.BigDecimal;
import java.util.Objects;

import com.renewsim.backend.shared.domain.exception.InvalidMoneyException;

public record Money(BigDecimal amount, String currency) {
    
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyException("Amount cannot be negative: " + amount);
        }
        if (currency.isBlank()) {
            throw new InvalidMoneyException("Currency cannot be blank");
        }
    }
}

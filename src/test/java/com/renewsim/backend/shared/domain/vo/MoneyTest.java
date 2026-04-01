package com.renewsim.backend.shared.domain.vo;

import com.renewsim.backend.shared.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class MoneyTest {
    
    @Test
    void shouldCreateValidMoney() {
        Money money = new Money(new BigDecimal("100.50"), "USD");
        assertThat(money.amount()).isEqualByComparingTo("100.50");
        assertThat(money.currency()).isEqualTo("USD");
    }
    
    @Test
    void shouldAcceptZeroAmount() {
        assertThatCode(() -> new Money(BigDecimal.ZERO, "EUR"))
            .doesNotThrowAnyException();
    }
    
    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> new Money(null, "USD"))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Amount cannot be null");
    }
    
    @Test
    void shouldRejectNullCurrency() {
        assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Currency cannot be null");
    }
    
    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-10"), "USD"))
            .isInstanceOf(InvalidMoneyException.class)
            .hasMessageContaining("cannot be negative");
    }
    
    @Test
    void shouldRejectBlankCurrency() {
        assertThatThrownBy(() -> new Money(BigDecimal.TEN, "   "))
            .isInstanceOf(InvalidMoneyException.class)
            .hasMessageContaining("Currency cannot be blank");
    }
}

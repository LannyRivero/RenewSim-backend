package com.renewsim.backend.shared.domain.exception;

public class InvalidMoneyException extends IllegalArgumentException  {
    public InvalidMoneyException(String message) {
        super(message);
    }

}

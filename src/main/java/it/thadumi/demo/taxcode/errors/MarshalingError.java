package it.thadumi.demo.taxcode.errors;

public class MarshalingError extends RuntimeException {
    public MarshalingError() {
    }

    public MarshalingError(String message) {
        super(message);
    }

    public MarshalingError(String message, Throwable cause) {
        super(message, cause);
    }

    public MarshalingError(Throwable cause) {
        super(cause);
    }

    public MarshalingError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

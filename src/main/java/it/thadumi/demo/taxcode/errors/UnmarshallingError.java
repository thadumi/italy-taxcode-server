package it.thadumi.demo.taxcode.errors;

public class UnmarshallingError extends RuntimeException {
    public UnmarshallingError() {
    }

    public UnmarshallingError(String message) {
        super(message);
    }

    public UnmarshallingError(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmarshallingError(Throwable cause) {
        super(cause);
    }

    public UnmarshallingError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static UnmarshallingError because(String message) {
        return new UnmarshallingError(message);
    }

    public static UnmarshallingError because(String message, Throwable cause) {
        return new UnmarshallingError(message, cause);
    }

}

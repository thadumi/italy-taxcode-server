package it.thadumi.demo.taxcode.errors;

public class MarshallingError extends RuntimeException {
    public MarshallingError() {
    }

    public MarshallingError(String message) {
        super(message);
    }

    public MarshallingError(String message, Throwable cause) {
        super(message, cause);
    }

    public MarshallingError(Throwable cause) {
        super(cause);
    }

    public MarshallingError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static MarshallingError because(String message) {
        return new MarshallingError(message);
    }

    public static MarshallingError because(String message, Throwable cause) {
        return new MarshallingError(message, cause);
    }

}

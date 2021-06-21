package it.thadumi.demo.taxcode.errors;

public class FragmentGenerationError extends MarshalingError {
    public FragmentGenerationError(String message) {
        super(message);
    }

    public FragmentGenerationError(String message, Throwable cause) {
        super(message, cause);
    }

    public static FragmentGenerationError because(String message) {
        return new FragmentGenerationError(message);
    }

    public static FragmentGenerationError because(String message, Throwable cause) {
        return new FragmentGenerationError(message, cause);
    }
}

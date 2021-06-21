package it.thadumi.demo.commons;

import io.vavr.collection.CharSeq;
import io.vavr.control.Try;

public class NumberUtils {

    public static Try<Integer> asInteger(String str) {
        return Try.of(() -> Integer.parseInt(str));
    }
    
    public static Try<Integer> asInteger(CharSeq str) {
        return asInteger(str.toString());
    }

    private NumberUtils() {}
}



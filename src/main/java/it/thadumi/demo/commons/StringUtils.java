package it.thadumi.demo.commons;

import io.vavr.collection.CharSeq;
import io.vavr.collection.Map;

import static io.vavr.API.*;

public class StringUtils {

    public static CharSeq extractConsonants(String str) {
        return CharSeq(str)
                .filter(CharUtils::isConsonant);
    }


    public static CharSeq extractVowels(String str) {
        return CharSeq(str)
                .filter(CharUtils::isVowel);
    }

    public static CharSeq replaceCharAtPosition(int index, char newCh, CharSeq cs) {
        return cs.subSequence(0, index).append(newCh).concat(cs.subSequence(index+1));
    }

    public static Map<CharUtils.CharType, CharSeq> extractConsonantsAndVowels(String str) {
        return extractConsonantsAndVowels(CharSeq(str));
    }

    public static Map<CharUtils.CharType, CharSeq> extractConsonantsAndVowels(CharSeq cs) {
        return CharSeq(cs)
                .groupBy(CharUtils.CharType::of);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private StringUtils() {}
}

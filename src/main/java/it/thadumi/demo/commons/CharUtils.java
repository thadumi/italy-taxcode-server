package it.thadumi.demo.commons;

import static it.thadumi.demo.commons.BoolUtils.not;

public class CharUtils {

    public static boolean isVowel(Character ch) {
        ch = Character.toUpperCase(ch);
        return ch == 'A'
                || ch == 'E'
                || ch == 'I'
                || ch == 'O'
                || ch == 'U';
    }

    public static boolean isConsonant(Character ch) {
        return not(isVowel(ch));
    }

    public static boolean isSpace(Character ch) {
        return  ch.equals(' ');
    }

    public enum CharType {
        CONSONANT, VOWEL;

        public static CharType of(Character ch) {
            return isVowel(ch) ? VOWEL : CONSONANT;
        }
    }

    private CharUtils() {}
}

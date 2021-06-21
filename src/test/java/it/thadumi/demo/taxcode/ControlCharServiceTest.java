package it.thadumi.demo.taxcode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.vavr.API.CharSeq;
import static org.junit.jupiter.api.Assertions.*;

class ControlCharServiceTest {
    static ControlCharService underTest;

    @BeforeAll
    public static void setUp() {
        underTest = new ControlCharService();
    }

    @Test
    void shouldAddTheControlCharacterU() {
        assertEquals("RSSMRA96H21H501U",
                underTest.appendControlCharacter(CharSeq("RSSMRA96H21H501")).toString());
    }

    @Test
    void shouldRemoveTheLastCharacter() {
        assertEquals("RSSMRA96H21H501",
                underTest.removeControlCharacters(CharSeq("RSSMRA96H21H501U")).toString());
    }
}

package it.thadumi.demo.taxcode;

import io.vavr.collection.CharSeq;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import javax.enterprise.context.ApplicationScoped;

import static io.vavr.collection.HashMap.ofEntries;
import static io.vavr.collection.Map.entry;
import static it.thadumi.demo.commons.StringUtils.replaceCharAtPosition;


@ApplicationScoped
class ControlCharService {

    public CharSeq appendControlCharacter(CharSeq taxCode) {
        var sum = taxCode.zipWithIndex()
                    .map(item -> isEven(item._2 + 1) ? evenCharCode(item._1) : oddCharCode(item._1))
                    .sum()
                    .intValue() % 26;
        var sumCode =   (char) (sum + 65);

        // if (exists(taxCode)) return withHomocodyControll(taxCode)
        return taxCode.append(sumCode);
    }

    public CharSeq removeControlCharacters(CharSeq taxCode) {
        return removeHomocodyChars(taxCode)
                .subSequence(0, taxCode.length()-1);
    }

    private CharSeq removeHomocodyChars(CharSeq taxCode) {
        for(var index : HOMOCODY_POSITION)
            taxCode = replaceCharAtPosition(index, decodeHomocodyChar(taxCode.get(index)), taxCode);
        return taxCode;
    }

    private Character decodeHomocodyChar(Character ch) {
        return HOMOCODY_DECODE_MAP.get(ch).getOrElse(ch);
    }

    private Character encodeHomocodyChar(Character ch) {
        return HOMOCODY_MAP.get(ch).get();
    }

    private boolean isEven(int index) {
        return index % 2 == 0;
    }

    private Integer evenCharCode(Character ch) {
        return EVEN_MAP.get(ch).get();
    }


    private Integer oddCharCode(Character ch) {
        return ODD_MAP.get(ch).get();
    }

    private static final Map<Character, Integer> ODD_MAP =
            ofEntries(
                    entry('0', 1),
                    entry('1', 0),
                    entry('2', 5),
                    entry('3', 7),
                    entry('4', 9),
                    entry('5', 13),
                    entry('6', 15),
                    entry('7', 17),
                    entry('8', 19),
                    entry('9', 21),

                    entry('A', 1),
                    entry('B', 0),
                    entry('C', 5),
                    entry('D', 7),
                    entry('E', 9),
                    entry('F', 13),
                    entry('G', 15),
                    entry('H', 17),
                    entry('I', 19),
                    entry('J', 21),
                    entry('K', 2),
                    entry('L', 4),
                    entry('M', 18),
                    entry('N', 20),
                    entry('O', 11),
                    entry('P', 3),
                    entry('Q', 6),
                    entry('R', 8),
                    entry('S', 12),
                    entry('T', 14),
                    entry('U', 16),
                    entry('V', 10),
                    entry('W', 22),
                    entry('X', 25),
                    entry('Y', 24),
                    entry('Z', 23)
            );

    private static final Map<Character, Integer> EVEN_MAP =
            ofEntries(
                    entry('0', 0),
                    entry('1', 1),
                    entry('2', 2),
                    entry('3', 3),
                    entry('4', 4),
                    entry('5', 5),
                    entry('6', 6),
                    entry('7', 7),
                    entry('8', 8),
                    entry('9', 9),

                    entry('A', 0),
                    entry('B', 1),
                    entry('C', 2),
                    entry('D', 3),
                    entry('E', 4),
                    entry('F', 5),
                    entry('G', 6),
                    entry('H', 7),
                    entry('I', 8),
                    entry('J', 9),
                    entry('K', 10),
                    entry('L', 11),
                    entry('M', 12),
                    entry('N', 13),
                    entry('O', 14),
                    entry('P', 15),
                    entry('Q', 16),
                    entry('R', 17),
                    entry('S', 18),
                    entry('T', 19),
                    entry('U', 20),
                    entry('V', 21),
                    entry('W', 22),
                    entry('X', 23),
                    entry('Y', 24),
                    entry('Z', 25)
            );

    private static final Map<Character, Character> HOMOCODY_MAP =
            ofEntries(
                    entry('0', 'L'),
                    entry('1', 'M'),
                    entry('2', 'N'),
                    entry('3', 'P'),
                    entry('4', 'Q'),
                    entry('5', 'R'),
                    entry('6', 'S'),
                    entry('7', 'T'),
                    entry('8', 'U'),
                    entry('9', 'V')
            );

    private static final Map<Character, Character> HOMOCODY_DECODE_MAP =
            HOMOCODY_MAP.toSet().toMap(entry -> entry._2,
                                       entry -> entry._1);

    private static final List<Integer> HOMOCODY_POSITION =
            List.of(6,7,9,10,12,13,14);
}

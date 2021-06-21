package it.thadumi.demo.taxcode;

import io.vavr.Function1;
import io.vavr.collection.CharSeq;
import io.vavr.control.Option;
import it.thadumi.demo.commons.CharUtils;
import it.thadumi.demo.taxcode.models.PhysicalPerson;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.Month;
import java.util.function.Predicate;

import static io.vavr.API.CharSeq;
import static it.thadumi.demo.commons.StringUtils.extractConsonantsAndVowels;
import static it.thadumi.demo.taxcode.Config.NAME_MARSHAL_LENGTH;
import static it.thadumi.demo.taxcode.Config.SURNAME_MARSHAL_LENGTH;

@ApplicationScoped
class TaxCodeMarshalling {
    @Inject
    private MonthService monthService;

    @Inject
    private NationService nationService;

    @Inject
    private ItalyService italyService;

    @Inject
    private ControlCharService controlCharService;

    public CharSeq marshalSurname(String surname) {
        Function1<CharSeq, CharSeq> consonantsRule = consonants ->
                consonants.length() >= SURNAME_MARSHAL_LENGTH
                 ? consonants.subSequence(0, SURNAME_MARSHAL_LENGTH)
                 : consonants;

        return marshalName(surname, SURNAME_MARSHAL_LENGTH, consonantsRule);
    }

    public CharSeq marshalFirstname(String fstName) {
        Function1<CharSeq, CharSeq> consonantsRule = consonants ->
                consonants.length() > NAME_MARSHAL_LENGTH
                 ? CharSeq.of(consonants.get(0), consonants.get(2), consonants.get(3))
                 : consonants;

        return marshalName(fstName, NAME_MARSHAL_LENGTH, consonantsRule);
    }

    public CharSeq marshalDateOfBirth(LocalDate dateOfBirth, PhysicalPerson.Gender gender) {
        return CharSeq.empty()
                    .concat(marshalYear(dateOfBirth.getYear()))
                    .concat(marshalMonth(dateOfBirth.getMonth()))
                    .concat(marshalDay(dateOfBirth.getDayOfMonth(), gender));
    }

    public Option<CharSeq> marshalBirthplace(String birthplace) {
        return italyService
                .istatCodeOf(birthplace)
                .orElse(() -> nationService.istatCodeOf(birthplace));
    }

    public CharSeq applyControlChar(CharSeq cs) {
        return controlCharService.appendControlCharacter(cs);
    }

    private CharSeq marshalYear(int year) {
        return CharSeq(Integer.toString(year % 100));
    }

    private CharSeq marshalMonth(Month month) {
        return monthService.mapMonth(month);
    }

    private CharSeq marshalDay(int day, PhysicalPerson.Gender gender) {
        if (gender == PhysicalPerson.Gender.FEMALE)
            day += 40;

        return CharSeq(Integer.toString(day)).leftPadTo(2, '0');
    }

    private CharSeq marshalName(String name,
                               int expectedLength,
                               Function1<CharSeq, CharSeq> consonantsMappingRule) {
        var nameSeq = removeGaps(name).toUpperCase();

        var consonantsAndVowels = extractConsonantsAndVowels(nameSeq);

        var consonants =
                consonantsMappingRule.apply(consonantsAndVowels.getOrElse(CharUtils.CharType.CONSONANT, CharSeq.empty()));

        var nrMissingConsonants = expectedLength - consonants.length();

        var allVowels = consonantsAndVowels.getOrElse(CharUtils.CharType.VOWEL, CharSeq.empty());
        var maxNumberOfValues = maxNumberOfVowelsAllowedInFragment(nrMissingConsonants, allVowels.length());

        var vowels = allVowels.subSequence(0, maxNumberOfValues);

        return CharSeq.empty()
                .concat(consonants)
                .concat(vowels)
                .padTo(expectedLength, 'X');
    }

    private CharSeq removeGaps(String str) {
        return CharSeq(str).filter(Predicate.not(CharUtils::isSpace));
    }

    private int maxNumberOfVowelsAllowedInFragment(int nrMissingConsonants, int nrAvailableVoles) {
        return Math.min(nrAvailableVoles, nrMissingConsonants);
    }
}

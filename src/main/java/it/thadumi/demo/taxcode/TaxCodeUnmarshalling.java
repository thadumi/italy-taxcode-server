package it.thadumi.demo.taxcode;

import java.time.Month;
import java.time.Year;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vavr.collection.CharSeq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import it.thadumi.demo.commons.NumberUtils;
import it.thadumi.demo.taxcode.models.PhysicalPerson.Gender;

@ApplicationScoped
public class TaxCodeUnmarshalling {
    @Inject
    private MonthService monthService;

    @Inject
    private NationService nationService;

    @Inject
    private ItalyService italyService;

    @Inject
    private ControlCharService controlCharService;

    public CharSeq removeControlChar(CharSeq taxCode) {
        return controlCharService.removeControlCharacters(taxCode);
    }

    public Try<Integer> unmarshalYear(CharSeq taxCode) {
        var yearChars = Try.of(() -> taxCode.subSequence(6, 8));

        return yearChars.flatMap(NumberUtils::asInteger)
                .map(byear -> byear > currentYear() ? (1900 + byear) : (2000 + byear));
    }

    public Option<Month> unmarshalMonth(CharSeq taxCode) {
        return monthService.mapMonth(taxCode.get(8));
    }

    public Try<Integer> unmarshalDay(CharSeq taxCode) {
        return extractDayOfBirthday(taxCode)
                .map(day -> day > 31 ? day - 40 : day);
    }

    public  Try<Gender> unmarshalGender(CharSeq taxCode) {
        var dayOfBirth = extractDayOfBirthday(taxCode);

        return dayOfBirth.map(day -> day > 31 ? Gender.FEMALE : Gender.MALE);
    }

    public Try<CharSeq> unmarshalBirthPlace(CharSeq taxCode) {
        var birthPlaceCode = Try.of(() -> taxCode.subSequence(11, 15));

        return birthPlaceCode.map(istatCode -> italyService.municipalityHavingIstatCode(istatCode)
                .orElse(() -> nationService.nationHavingIstatCode(istatCode)))
                .flatMap(Option::toTry);
    }

    private int currentYear() {
        return Year.now().getValue() % 100;
    }

    private Try<Integer> extractDayOfBirthday(CharSeq taxCode) {
        var dayOfBirthChars = Try.of(() -> taxCode.subSequence(9, 11));

        return dayOfBirthChars.flatMap(NumberUtils::asInteger);

    }
}

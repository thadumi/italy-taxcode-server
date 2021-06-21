package it.thadumi.demo.taxcode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vavr.Tuple2;
import io.vavr.Function1;
import io.vavr.collection.CharSeq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import it.thadumi.demo.taxcode.errors.MarshallingError;
import it.thadumi.demo.taxcode.models.PhysicalPerson;
import it.thadumi.demo.taxcode.models.PhysicalPerson.Gender;

import static io.vavr.API.*;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

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

    public Tuple2<CharSeq, CharSeq> unmarshalSurname(CharSeq taxCode) {
        var surnameChars = taxCode.subSequence(0, 3);
        var leftOver = taxCode.subSequence(3);
        return Tuple(leftOver, surnameChars);
    }

    public Tuple2<CharSeq, CharSeq> unmarshalFirstname(CharSeq leftOverTaxCode) {
        var firstnameChars = leftOverTaxCode.subSequence(0, 3);
        var leftOver = leftOverTaxCode.subSequence(3);

        return Tuple(leftOver, firstnameChars);
    }

    public Tuple2<CharSeq, Option<Integer>> unmarshalYear(CharSeq leftOverTaxCode) {
        var yearChars = leftOverTaxCode.subSequence(0, 2);

        var year = Try.of(() -> Integer.parseInt(yearChars.toString()))
                      .toOption();
        
        var leftOver = leftOverTaxCode.subSequence(2);

        return Tuple(leftOver, year);
    }

    public Tuple2<CharSeq, Option<Month>> unmarshalMonth(CharSeq leftOverTaxCode) {
        var month = monthService.mapMonth(leftOverTaxCode.get());
        var leftOver = leftOverTaxCode.subSequence(1);

        return Tuple(leftOver, month);
    }

    public Tuple2<CharSeq, Option<Gender>> unmarshalGender(CharSeq leftOverTaxCode) {
        var gender = extractDayOfBirth(leftOverTaxCode)
                        .map(day -> day > 31 ? Gender.FEMALE : Gender.MALE);
        
        return Tuple(leftOverTaxCode, gender);
    }

    public Tuple2<CharSeq, Option<Integer>> unmarshalDay(CharSeq leftOverTaxCode) {
        var dayOfBirth = extractDayOfBirth(leftOverTaxCode);
        var leftOver = leftOverTaxCode.substring(2);

        return Tuple(leftOver, dayOfBirth);
    }

    public Option<CharSeq> unmarshalBirthPlace(CharSeq leftOverTaxCode) {
        return italyService.municipalityHavingIstatCode(leftOverTaxCode)
                           .orElse(() -> nationService.nationHavingIstatCode(leftOverTaxCode));
    }

    private Option<Integer> extractDayOfBirth(CharSeq leftOverTaxCode) {
        var dayChars = leftOverTaxCode.subSequence(0, 2);

        return Try.of(() -> Integer.parseInt(dayChars.toString()))
                  .toOption();
    }
}

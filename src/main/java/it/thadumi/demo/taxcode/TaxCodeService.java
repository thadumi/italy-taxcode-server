package it.thadumi.demo.taxcode;


import io.vavr.API;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.collection.CharSeq;
import io.vavr.control.Either;
import it.thadumi.demo.taxcode.errors.FragmentGenerationError;
import it.thadumi.demo.taxcode.errors.MarshallingError;
import it.thadumi.demo.taxcode.errors.UnmarshallingError;
import it.thadumi.demo.taxcode.models.PhysicalPerson;
import it.thadumi.demo.taxcode.models.PhysicalPerson.Gender;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static io.vavr.API.*;
import static it.thadumi.demo.commons.BoolUtils.not;
import static it.thadumi.demo.commons.StringUtils.isEmpty;
import static java.util.Objects.isNull;

import java.time.LocalDate;
import java.time.Month;

@ApplicationScoped
public class TaxCodeService {
    @Inject
    TaxCodeMarshalling marshaller;

    @Inject
    TaxCodeUnmarshalling unmarshaller;

    public TaxCodeService() {}

    public TaxCodeService(TaxCodeMarshalling marshaller, TaxCodeUnmarshalling unmarshaller) {
        this.marshaller = marshaller;
        this.unmarshaller = unmarshaller;
    }

    public Either<MarshallingError, String> marshal(PhysicalPerson person) {
        return emptyMarshalling()
                .flatMap(marshallingStep(person, this::surnameMarshalling))
                .flatMap(marshallingStep(person, this::firstNameMarshalling))
                .flatMap(marshallingStep(person, this::birthDateMarshalling))
                .flatMap(marshallingStep(person, this::birthPlaceMarshalling))
                .flatMap(this::injectControlCodes)
                .map(CharSeq::toString);
    }

    public Either<UnmarshallingError, PhysicalPerson> unmarshal(String taxCode) {
        var tc = CharSeq(taxCode).toUpperCase();

        if(tc.length() != 16)
            return Left(UnmarshallingError.because("Length of tax-code expected to be 16 but was " + taxCode.length()));

        if(isNotAValidTaxCodeFormat(tc))
            return Left(UnmarshallingError.because("Illegal format of the tax code provided."));


        var cleanTaxCode = removeControlChars(tc);

        return cleanTaxCode.flatMap(this::extractPersonInformationFrom);
    }

    private  Either<UnmarshallingError, PhysicalPerson> extractPersonInformationFrom(CharSeq taxCode) {
        var surname = unmarshalSurname(taxCode);
        var firstname = unmarshalFirstname(taxCode);
        var dob = unmarshalDateOfBirth(taxCode);
        var gender = unmarshalGender(taxCode);
        var birthPlace = unmarshalBirthPlace(taxCode);

        var extractedData = For(surname, firstname, dob, birthPlace, gender).yield(Tuple::of);

        var extractedPerson = extractedData.map(PhysicalPerson::fromTuple);

        var possibleError =
                Lazy(() -> dob.flatMap(ignore -> gender).flatMap(ignore -> birthPlace).getLeft());

        return extractedPerson
                    .toEither(possibleError);
    }

    private Either<MarshallingError, CharSeq> emptyMarshalling() {
        return Right(CharSeq.empty());
    }

    private Either<MarshallingError, CharSeq> surnameMarshalling(PhysicalPerson person) {
        if(isEmpty(person.getSurname()))
            return Left(FragmentGenerationError.because("A person without surname was provided"));

        return Right(marshaller.marshalSurname(person.getSurname()));
    }

    private Either<MarshallingError, CharSeq> firstNameMarshalling(PhysicalPerson person) {
        if(isEmpty(person.getSurname()))
            return Left(FragmentGenerationError.because("A person without firstname was provided"));

        return Right(marshaller.marshalFirstname(person.getFirstname()));
    }

    private Either<MarshallingError, CharSeq> birthDateMarshalling(PhysicalPerson person) {
        if (isNull(person.getGender()))
            return Left(FragmentGenerationError.because("Unknown gender"));

        if (isNull(person.getDateOfBirth()))
            return Left(FragmentGenerationError.because("Unknown date of birth"));

        var todayDate = LocalDate.now();
        if (person.getDateOfBirth().isAfter(todayDate)) // the person is yet to be born
            return Left(FragmentGenerationError.because("Exceeded the date limit of " + todayDate));

        return Right(marshaller.marshalDateOfBirth(person.getDateOfBirth(), person.getGender()));
    }

    private Either<MarshallingError, CharSeq> birthPlaceMarshalling(PhysicalPerson person) {
        if (isEmpty(person.getBirthplace()))
            return Left(FragmentGenerationError.because("The birthplace was not provided"));

        return marshaller
                .marshalBirthplace(person.getBirthplace())
                .toEither(() -> FragmentGenerationError.because("Unknown ISTAT code for the birth place " + person.getBirthplace()));
    }

    private Either<MarshallingError, CharSeq> injectControlCodes(CharSeq taxCode) {
        if (taxCode.length() < 15)
            return Left(FragmentGenerationError.because("Something went wrong generating the tax code. Tax code too short."));

        return Right(marshaller.applyControlChar(taxCode));
    }

    private Either<UnmarshallingError, CharSeq> removeControlChars(CharSeq taxCode) {
        return Right(unmarshaller.removeControlChar(taxCode));
    }

    private Either<UnmarshallingError, CharSeq> unmarshalSurname(CharSeq taxCode) {
        return unmarshaller.unmarshalSurname(taxCode)
                            .toEither()
                            .mapLeft(cause -> UnmarshallingError.because("Unable to extract the surname " + taxCode , cause));
    }

    private Either<UnmarshallingError, CharSeq> unmarshalFirstname(CharSeq taxCode) {
        return unmarshaller.unmarshalFirstname(taxCode)
                .toEither()
                .mapLeft(cause -> UnmarshallingError.because("Unable to extract the firstname " + taxCode , cause));
    }

    private Either<UnmarshallingError, LocalDate> unmarshalDateOfBirth(CharSeq taxCode) {
        return  unmarshalYear(taxCode)
                .map(API::Tuple)
                .flatMap(y -> unmarshalMonth(taxCode).map(y::append))
                .flatMap(ym -> unmarshalDay(taxCode).map(ym::append))
                .map(ymd -> LocalDate.of(ymd._1, ymd._2, ymd._3));

    }

    private Either<UnmarshallingError, Integer> unmarshalYear(CharSeq taxCode) {
        var tryExtractYear = unmarshaller.unmarshalYear(taxCode);

        return tryExtractYear
                .toEither()
                .mapLeft(cause ->  UnmarshallingError.because("Unable to extract year from " + taxCode , cause));
    }


    private Either<UnmarshallingError, Month> unmarshalMonth(CharSeq taxCode) {
        var tryExtractMonth = unmarshaller.unmarshalMonth(taxCode);

        return tryExtractMonth
                .toEither(() -> UnmarshallingError.because("Unable to extract the month from " + taxCode));
    }

    private Either<UnmarshallingError, Integer> unmarshalDay(CharSeq taxCode) {
        var tryExtractDay = unmarshaller.unmarshalDay(taxCode);

        return tryExtractDay
                .toEither()
                .mapLeft(cause ->  UnmarshallingError.because("Unable to extract the month from " + taxCode , cause));
    }

    private Either<UnmarshallingError, Gender> unmarshalGender(CharSeq taxCode) {
        var tryExtractGender = unmarshaller.unmarshalGender(taxCode);

        return  tryExtractGender
                .toEither()
                .mapLeft(cause -> UnmarshallingError.because("Unable to extract the gender from " + taxCode , cause));
    }

    private Either<UnmarshallingError, CharSeq> unmarshalBirthPlace(CharSeq taxCode) {
        var tryExtractBirthPlace = unmarshaller.unmarshalBirthPlace(taxCode);

        return tryExtractBirthPlace
                .toEither()
                .mapLeft(cause -> UnmarshallingError.because("Unable to extract the birth place from " + taxCode , cause));

    }

    private Function1<CharSeq, Either<MarshallingError, CharSeq>> marshallingStep (
            PhysicalPerson personToMarshal,
            MarshallingComputationStep step) {
        return marshalFragment -> concatenateMarshalFragment(marshalFragment, step.apply(personToMarshal));
    }

    private Either<MarshallingError, CharSeq> concatenateMarshalFragment(CharSeq marshalFragment,
                                                                         Either<MarshallingError, CharSeq> newStep) {
        return newStep.map(marshalFragment::concat);
    }

    @FunctionalInterface
    interface MarshallingComputationStep
            extends Function1<PhysicalPerson, Either<MarshallingError, CharSeq>> {
    }

    private boolean isNotAValidTaxCodeFormat(CharSeq taxCode) {
        return not(taxCode.matches("[A-Z]{6}[0-9]{2}[ABCDEHLMPRST]{1}[0-9]{2}[a-zA-Z]{1}[0-9]{3}[A-Z]{1}"));
    }
}

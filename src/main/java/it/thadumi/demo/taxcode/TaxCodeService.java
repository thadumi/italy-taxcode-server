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
import static it.thadumi.demo.commons.StringUtils.isEmpty;

import java.time.LocalDate;
import java.time.Month;

@ApplicationScoped
public class TaxCodeService {
    @Inject
    TaxCodeMarshalling marshaller;

    @Inject
    TaxCodeUnmarshalling unmarshaller;


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
        var cleanTaxCode = removeControlChars(CharSeq(taxCode));

        return cleanTaxCode.flatMap(this::extractPersonInformationFrom);
    }

    private  Either<UnmarshallingError, PhysicalPerson> extractPersonInformationFrom(CharSeq taxCode) {
        var dob = unmarshalDateOfBirth(taxCode);
        var gender = unmarshalGender(taxCode);
        var birthPlace = unmarshalBirthPlace(taxCode);

        var extractedData = For(dob, birthPlace, gender)
                .yield(Tuple::of)
                .map(Tuple(CharSeq.empty(), CharSeq.empty())::concat);

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
        return Right(marshaller.marshalDateOfBirth(person.getDateOfBirth(), person.getGender()));
    }

    private Either<MarshallingError, CharSeq> birthPlaceMarshalling(PhysicalPerson person) {
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
        if(taxCode.length() != 16)
            return Left(UnmarshallingError.because("Length of tax-code expected to be 16 but was " + taxCode.length()));

        return Right(unmarshaller.removeControlChar(taxCode));
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
}

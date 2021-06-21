package it.thadumi.demo.taxcode;


import io.vavr.Function1;
import io.vavr.collection.CharSeq;
import io.vavr.control.Either;
import it.thadumi.demo.taxcode.errors.FragmentGenerationError;
import it.thadumi.demo.taxcode.errors.MarshalingError;
import it.thadumi.demo.taxcode.models.PhysicalPerson;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static io.vavr.API.Left;
import static io.vavr.API.Right;
import static it.thadumi.demo.commons.StringUtils.isEmpty;

@ApplicationScoped
public class TaxCodeService {
    @Inject
    TaxCodeMarshaling maxcodeMarshaller;

    public Either<MarshalingError, String> marshal(PhysicalPerson person) {
        return emptyMarshaling()
                .flatMap(marshalingStep(person, this::surnameMarshaling))
                .flatMap(marshalingStep(person, this::firstNameMarshaling))
                .flatMap(marshalingStep(person, this::birthDateMarshaling))
                .flatMap(marshalingStep(person, this::birthPlaceMarshaling))
                .flatMap(this::injectControlCodes)
                .map(CharSeq::toString);
    }

    public Either<MarshalingError, PhysicalPerson> unmarshal(String cf) {
        // assert length 16
        return Either.left(null);
    }

    private Either<MarshalingError, CharSeq> emptyMarshaling() {
        return Right(CharSeq.empty());
    }


    private Either<MarshalingError, CharSeq> surnameMarshaling(PhysicalPerson person) {
        if(isEmpty(person.getSurname()))
            return Left(FragmentGenerationError.because("A person without surname was provided"));

        return Right(maxcodeMarshaller.marshalSurname(person.getSurname()));
    }

    private Either<MarshalingError, CharSeq> firstNameMarshaling(PhysicalPerson person) {
        if(isEmpty(person.getSurname()))
            return Left(FragmentGenerationError.because("A person without firstname was provided"));

        return Right(maxcodeMarshaller.marshalFirstname(person.getFirstname()));
    }

    private Either<MarshalingError, CharSeq> birthDateMarshaling(PhysicalPerson person) {
        return Right(maxcodeMarshaller.marshalDateOfBirth(person.getDateOfBirth(), person.getGender()));
    }

    private Either<MarshalingError, CharSeq> birthPlaceMarshaling(PhysicalPerson person) {
        return maxcodeMarshaller
                .marshalBirthplace(person.getBirthplace())
                .toEither(() -> FragmentGenerationError.because("Unknown ISTAT code for the birth place " + person.getBirthplace()));
    }

    private Either<MarshalingError, CharSeq> injectControlCodes(CharSeq taxCode) {
        if (taxCode.length() < 15)
            return Left(FragmentGenerationError.because("Something went wrong generating the tax code. Tax code too short."));

        return Right(maxcodeMarshaller.applyControlChar(taxCode));
    }

    private Function1<CharSeq, Either<MarshalingError, CharSeq>> marshalingStep (
            PhysicalPerson personToMarshal,
            MarshalingComputationStep step) {
        return marshalFragment -> concatenateMarshalFragment(marshalFragment, step.apply(personToMarshal));
    }

    private Either<MarshalingError, CharSeq> concatenateMarshalFragment(CharSeq marshalFragment,
                                                                       Either<MarshalingError, CharSeq> newStep) {
        return newStep.map(marshalFragment::concat);
    }

    @FunctionalInterface
    interface MarshalingComputationStep
            extends Function1<PhysicalPerson, Either<MarshalingError, CharSeq>> {
    }
}

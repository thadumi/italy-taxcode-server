package it.thadumi.demo.taxcode;


import io.vavr.Function1;
import io.vavr.collection.CharSeq;
import io.vavr.control.Either;
import it.thadumi.demo.taxcode.errors.FragmentGenerationError;
import it.thadumi.demo.taxcode.errors.MarshallingError;
import it.thadumi.demo.taxcode.models.PhysicalPerson;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static io.vavr.API.Left;
import static io.vavr.API.Right;
import static it.thadumi.demo.commons.StringUtils.isEmpty;

@ApplicationScoped
public class TaxCodeService {
    @Inject
    TaxCodeMarshalling taxCodeMarshaller;

    public Either<MarshallingError, String> marshal(PhysicalPerson person) {
        return emptyMarshalling()
                .flatMap(marshallingStep(person, this::surnameMarshalling))
                .flatMap(marshallingStep(person, this::firstNameMarshalling))
                .flatMap(marshallingStep(person, this::birthDateMarshalling))
                .flatMap(marshallingStep(person, this::birthPlaceMarshalling))
                .flatMap(this::injectControlCodes)
                .map(CharSeq::toString);
    }

    public Either<MarshallingError, PhysicalPerson> unmarshal(String taxCode) {
        // assert length 16
        return Either.left(null);
    }

    private Either<MarshallingError, CharSeq> emptyMarshalling() {
        return Right(CharSeq.empty());
    }


    private Either<MarshallingError, CharSeq> surnameMarshalling(PhysicalPerson person) {
        if(isEmpty(person.getSurname()))
            return Left(FragmentGenerationError.because("A person without surname was provided"));

        return Right(taxCodeMarshaller.marshalSurname(person.getSurname()));
    }

    private Either<MarshallingError, CharSeq> firstNameMarshalling(PhysicalPerson person) {
        if(isEmpty(person.getSurname()))
            return Left(FragmentGenerationError.because("A person without firstname was provided"));

        return Right(taxCodeMarshaller.marshalFirstname(person.getFirstname()));
    }

    private Either<MarshallingError, CharSeq> birthDateMarshalling(PhysicalPerson person) {
        return Right(taxCodeMarshaller.marshalDateOfBirth(person.getDateOfBirth(), person.getGender()));
    }

    private Either<MarshallingError, CharSeq> birthPlaceMarshalling(PhysicalPerson person) {
        return taxCodeMarshaller
                .marshalBirthplace(person.getBirthplace())
                .toEither(() -> FragmentGenerationError.because("Unknown ISTAT code for the birth place " + person.getBirthplace()));
    }

    private Either<MarshallingError, CharSeq> injectControlCodes(CharSeq taxCode) {
        if (taxCode.length() < 15)
            return Left(FragmentGenerationError.because("Something went wrong generating the tax code. Tax code too short."));

        return Right(taxCodeMarshaller.applyControlChar(taxCode));
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

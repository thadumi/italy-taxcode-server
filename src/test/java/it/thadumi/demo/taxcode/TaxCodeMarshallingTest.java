package it.thadumi.demo.taxcode;

import io.vavr.collection.CharSeq;
import io.vavr.control.Option;
import it.thadumi.demo.taxcode.models.PhysicalPerson;
import it.thadumi.demo.taxcode.repo.MunicipalityRepo;
import it.thadumi.demo.taxcode.repo.NationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxCodeMarshallingTest {
    @Mock NationRepo nationRepo;
    @Mock MunicipalityRepo municipalityRepo;

    TaxCodeMarshalling underTest;

    @BeforeEach
    void init() {
        var nationService =
        underTest = new TaxCodeMarshalling(
                            new MonthService(),
                            new NationService(nationRepo),
                            new ItalyService(municipalityRepo),
                            new ControlCharService());

        lenient().when(nationRepo.exists("ROME CAPITALE")).thenReturn(false);
        lenient().when(nationRepo.getIstatID("ROMANIA")).thenReturn(Option.of("Z129"));
        lenient().when(nationRepo.getNationHavingIstatCode("Z129")).thenReturn(Option.of("ROMANIA"));


        lenient().when(municipalityRepo.exists("ROME CAPITALE")).thenReturn(true);
        lenient().when(municipalityRepo.getIstatID("ROMA CAPITALE")).thenReturn(Option.of("H501"));
        lenient().when(municipalityRepo.getMunicipalityHavingIstatCode("H501")).thenReturn(Option.of("ROMA CAPITALE"));
    }

    @Test
    void marshalingFirstnameShouldUseTheFstTrdAndFthConsonants() {
        var firstName = "Gianfranco";
        var expectedResult = "GFR";

        var result = underTest.marshalFirstname(firstName).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void marshalingFirstnameShouldUseVowelsWhenConsonantsAreNotEnough() {
        var firstName = "Mario";
        var expectedResult = "MRA";

        var result = underTest.marshalFirstname(firstName).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void marshalingFirstnameShouldFillWithXWhenConsonantsAndVowelsAreNotEnough() {
        var firstName = "Fo";
        var expectedResult = "FOX";

        var result = underTest.marshalFirstname(firstName).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void marshalingSurnameShouldUseTheFstSndAndTrdConsonants() {
        var firstName = "Rossi";
        var expectedResult = "RSS";

        var result = underTest.marshalSurname(firstName).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void marshalingSurnameShouldUseVowelsWhenConsonantsAreNotEnough() {
        var firstName = "Rosi";
        var expectedResult = "RSO";

        var result = underTest.marshalSurname(firstName).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void  marshalingSurnameShouldFillWithXWhenConsonantsAndVowelsAreNotEnough() {
        var firstName = "Fo";
        var expectedResult = "FOX";

        var result = underTest.marshalSurname(firstName).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void theMarshaledDayOfBirthShouldBeTheSameForAMale() {
        var dayOfBirth = 21;
        var gender = PhysicalPerson.Gender.MALE;
        var expectedResult = String.valueOf(21);

        var result = underTest.marshalDay(dayOfBirth, gender).toString();

        assertEquals(expectedResult, result);
    }

    @Test
    void theMarshaledDayOfBirthShouldBeIncreasedBy40ForAFemale() {
        var dayOfBirth = 21;
        var gender = PhysicalPerson.Gender.FEMALE;
        var expectedResult = String.valueOf(21 + 40);

        var result = underTest.marshalDay(dayOfBirth, gender).toString();

        assertEquals(expectedResult, result);
    }

    private PhysicalPerson.PhysicalPersonBuilder testingPersonSample() {
        return PhysicalPerson.builder()
                .surname("Rossi")
                .firstname("Mario")
                .birthplace("Roma")
                .dateOfBirth(LocalDate.of(1996, Month.JUNE, 21))
                .gender(PhysicalPerson.Gender.MALE);
    }
}

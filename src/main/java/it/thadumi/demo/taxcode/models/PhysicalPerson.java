package it.thadumi.demo.taxcode.models;

import io.vavr.Tuple5;
import io.vavr.collection.CharSeq;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhysicalPerson {
    String firstname;
    String surname;

    LocalDate dateOfBirth;
    String birthplace;

    Gender gender;


    public enum Gender {
        MALE,
        FEMALE;
    }

    public static PhysicalPerson fromTuple(Tuple5<CharSeq, CharSeq, LocalDate, CharSeq, Gender> personData) {
        return new PhysicalPerson(personData._1.toString(), personData._2.toString(),
                                  personData._3, personData._4.toString(), personData._5);
    }
}

package it.thadumi.demo.taxcode.models;

import lombok.*;

import java.time.LocalDate;

// lombok
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// quarkus native requirements
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
}

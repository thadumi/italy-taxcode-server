package it.thadumi.demo.taxcode.models;

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
}

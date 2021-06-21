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

    Sex sex;


    public enum Sex {
        MALE(),
        FEMALE();
    }
}

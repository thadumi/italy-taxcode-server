package it.thadumi.demo.taxcode.repo;

import io.vavr.control.Option;

public interface MunicipalityRepo {
    Option<String> getIstatID(String name);
    boolean exists(String name);

    Option<String> getMunicipalityHavingIstatCode(String code);
}

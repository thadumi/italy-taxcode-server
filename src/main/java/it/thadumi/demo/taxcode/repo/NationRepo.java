package it.thadumi.demo.taxcode.repo;

import io.vavr.control.Option;

public interface NationRepo {
    Option<String> getIstatID(String nationName);
    boolean exists(String nationName);
    Option<String> getNationHavingIstatCode(String code);
}

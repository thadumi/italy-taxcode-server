package it.thadumi.demo.taxcode.repo;

import io.vavr.collection.CharSeq;
import io.vavr.control.Option;

public interface MunicipalityRepo {
    Option<CharSeq> getIstatID(String id);
    boolean exists(String id);
}

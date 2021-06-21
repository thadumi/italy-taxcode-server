package it.thadumi.demo.taxcode;

import io.vavr.API;
import io.vavr.collection.CharSeq;
import io.vavr.control.Option;
import it.thadumi.demo.taxcode.repo.NationRepo;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class NationService {
    @Inject
    private NationRepo repo;

    public Option<CharSeq> istatCodeOf(String nation) {
        return repo.getIstatID(nation).map(API::CharSeq);
    }

    public Option<CharSeq> nationHavingIstatCode(String code) {
        return repo.getNationHavingIstatCode(code).map(API::CharSeq);
    }

    public boolean isaNation(String nationName) {
        return repo.exists(nationName);
    }

}

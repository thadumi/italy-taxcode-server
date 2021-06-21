package it.thadumi.demo.taxcode;

import io.vavr.collection.CharSeq;
import io.vavr.control.Option;
import it.thadumi.demo.taxcode.repo.MunicipalityRepo;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class ItalyService {
    @Inject
    private MunicipalityRepo repo;

    public Option<CharSeq> istatCodeOf(String place) {
        return repo.getIstatID(place);
    }

    public boolean isInItaly(String place) {
        return repo.exists(place);
    }
}

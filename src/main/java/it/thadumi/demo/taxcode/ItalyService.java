package it.thadumi.demo.taxcode;

import io.vavr.API;
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
        return repo.getIstatID(place).map(API::CharSeq);
    }

    public Option<CharSeq> istatCodeOf(CharSeq place) {
        return istatCodeOf(place.toString());
    }

    public boolean isInItaly(String place) {
        return repo.exists(place);
    }

    public Option<CharSeq> municipalityHavingIstatCode(String istatCode) {
        return repo.getMunicipalityHavingIstatCode(istatCode)
                   .map(API::CharSeq);
    }
    
    public Option<CharSeq> municipalityHavingIstatCode(CharSeq istatCode) {
        return municipalityHavingIstatCode(istatCode.toString());
    }
}

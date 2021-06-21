package it.thadumi.demo.taxcode;

import io.vavr.API;
import io.vavr.collection.CharSeq;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import java.time.Month;

import static io.vavr.API.Tuple;
import static java.time.Month.*;

@ApplicationScoped
class MonthService {
    private static final Map<Month, CharSeq> MONTH_ID_MAP = HashMap
            .ofEntries(
                    Tuple(JANUARY, API.CharSeq('A')),
                    Tuple(FEBRUARY, API.CharSeq('B')),
                    Tuple(MARCH, API.CharSeq('C')),
                    Tuple(APRIL, API.CharSeq('D')),
                    Tuple(MAY, API.CharSeq('E')),
                    Tuple(JUNE, API.CharSeq('H')),
                    Tuple(JULY, API.CharSeq('L')),
                    Tuple(AUGUST, API.CharSeq('M')),
                    Tuple(SEPTEMBER, API.CharSeq('P')),
                    Tuple(OCTOBER, API.CharSeq('R')),
                    Tuple(NOVEMBER, API.CharSeq('S')),
                    Tuple(DECEMBER, API.CharSeq('T'))
            );

    public CharSeq mapMonth(Month month) {
        return MONTH_ID_MAP.get(month).getOrElse(CharSeq.empty());
    }
}

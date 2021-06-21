package it.thadumi.demo.taxcode.repo.impl;

import io.vavr.API;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.CharSeq;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;
import it.thadumi.demo.commons.ResourceLoader;
import it.thadumi.demo.taxcode.repo.NationRepo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import static io.vavr.API.Tuple;

@ApplicationScoped
public class NationRepoImp implements NationRepo {

    private RepoFileBackend backend;

    @Inject
    public NationRepoImp(RepoFileBackend rb) {
        backend = rb;
    }

    @Override
    public Option<CharSeq> getIstatID(String id) {
        return backend.getTable()
                .get(id)
                .map(API::CharSeq);
    }

    @Override
    public boolean exists(String id) {
        return getIstatID(id).isDefined();
    }

    @Default
    @ApplicationScoped
    static class RepoFileBackend {
        @Inject
        @ConfigProperty(name = "repo.nation.dbPath")
        private String dbPath;

        private Map<String, String> nations;

        public Map<String, String> getTable() {
            return nations;
        }

        //@PostConstruct
        void loadDataOnStartup(@Observes @Initialized( ApplicationScoped.class ) Object init) {
            System.out.println("Loading the nations' db from " + dbPath);

            nations = retrieveData()
                        .onFailure(throwable -> {
                            throwable.printStackTrace();
                            System.exit(-1);
                        })
                        .onSuccess(data -> System.out.printf("Nations' db loaded. %d entries known.%n", data.length()))
                        .get();
        }

        private Try<Map<String, String>> retrieveData() {
            Function1<Seq<Tuple2<String, String>>, Map<String,String>> dataToMap =
                    rows -> rows.toMap(Function1.identity());

            return loadDBRows(dbPath)
                    .map(dataToMap);
        }

        private Try<Seq<Tuple2<String, String>>> loadDBRows(String dbPath)  {
            Function1<CharSeq, Seq<CharSeq>> toRows = db -> db.split("\n");

            Function1<Seq<CharSeq>, Seq<Tuple2<String, String>>> extractRowValues =
                    rows -> rows.map(row -> row.split(";"))
                                .map(row -> row.map(CharSeq::toString))
                                .map(row -> Tuple(row.get(0), row.get(1)));

            return ResourceLoader.getResourceFile(dbPath)
                    .map(toRows)
                    .map(extractRowValues);
        }

    }
}

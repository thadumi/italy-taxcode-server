package it.thadumi.demo.taxcode.repo.impl;

import com.bordercloud.sparql.SparqlClient;
import com.bordercloud.sparql.SparqlClientException;
import com.bordercloud.sparql.SparqlResultModel;
import io.vavr.API;
import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.CharSeq;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import it.thadumi.demo.commons.ResourceLoader;
import it.thadumi.demo.taxcode.repo.MunicipalityRepo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static io.vavr.API.*;

@ApplicationScoped
public class MunicipalityRepoImp implements MunicipalityRepo {

    @Inject
    private RepoBackend backend;

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
    static class RepoBackend {

        @Inject
        @ConfigProperty(name = "repo.municipality.endpoint")
        private String endpoint;

        @Inject
        @ConfigProperty(name = "repo.municipality.query")
        private String queryPath;

        private Map<String, String> municipalities;

        public Map<String, String> getTable() {
            return municipalities;
        }

        //@PostConstruct
        void loadDataOnStartup(@Observes @Initialized( ApplicationScoped.class ) Object init) {
            municipalities = retrieveData()
                                .onFailure(throwable -> {
                                    throwable.printStackTrace();
                                    System.exit(-1);
                                })
                                .onSuccess(data ->{
                                    System.out.println("Loaded the istat code of " + data.length() + " municipalities.");
                                })
                                .get();
        }

        private Try<Map<String, String>> retrieveData() {
            Function1< List<HashMap<String, Object>>, Stream<Tuple2<String, String>>> extractMunicipalitiesData =
                    rows -> rows.stream().map(row -> Tuple((String) row.get("itemLabel"), (String)row.get("ISTATID")));

            Function1< Stream<Tuple2<String, String>>, Map<String,String>> municipalitiesDataAsMap =
                    stream -> stream.reduce(Map(), Map::put, Map::merge);

            return getWikidataData()
                    .map(extractMunicipalitiesData)
                    .map(municipalitiesDataAsMap);
        }

        private Try<List<HashMap<String, Object>>> getWikidataData() {
            var client = createClient(endpoint);

            return loadQuery(queryPath)
                    .peek(query -> System.out.println("Using wikidata query: " + query))
                    .map(CharSeq::toString)
                    .mapTry(CheckedFunction(this::runQuery).apply(client))
                    .map(SparqlResultModel::getRows);
        }

        private Try<CharSeq> loadQuery(String queryPath)  {
            return ResourceLoader.getResourceFile(queryPath);
        }

        private SparqlClient createClient(String endpoint) {
            var sc = new SparqlClient(false);
            sc.setEndpointRead(URI.create(endpoint));

            return sc;
        }

        private SparqlResultModel runQuery(SparqlClient client, String query) throws SparqlClientException {
            var result = client.query(query);

            return result.getModel();
        }
    }
}

package it.thadumi.demo.infrastructure;

import it.thadumi.demo.taxcode.models.PhysicalPerson;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@ApplicationScoped
public class HealthChecks {

    @Produces
    @Liveness
    HealthCheck checkMemory() {
        return () -> {
            var memBean = ManagementFactory.getMemoryMXBean();
            var memUsed = memBean.getHeapMemoryUsage().getUsed();
            var memMax = memBean.getHeapMemoryUsage().getMax();

            return HealthCheckResponse
                    .named("Memory Liveness Check")
                    .withData("memory used", memUsed)
                    .withData("memory max", memMax)
                    .status(memUsed < memMax * 0.9).build();
        };
    }

    @Produces
    @Readiness
    HealthCheck endpointsReady() {
        var readinessCheck = "Endpoint Readiness Check";

        return () -> isMarshalingEndpointReachable()
                        ? HealthCheckResponse.up(readinessCheck)
                        : HealthCheckResponse.down(readinessCheck);
    }

    private boolean isMarshalingEndpointReachable() {
        var uri = getBaseUri() + "v1/taxcode/marshal";
        var payload = "{\n" +
                "  \"birthplace\": \"string\",\n" +
                "  \"dateOfBirth\": \"2021-06-21\",\n" +
                "  \"firstname\": \"string\",\n" +
                "  \"gender\": \"FEMALE\",\n" +
                "  \"surname\": \"string\"\n" +
                "}";

        var request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .uri(URI.create(uri))
                .build();

        return isEndpointReachable(request);
    }

    private boolean isEndpointReachable(HttpRequest request) {

        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String getBaseUri() {
        var port = System.getProperty("http.port");
        return "http://localhost:" + port + "/";
    }
}

package it.thadumi.demo.infrastructure;

import it.thadumi.demo.taxcode.TaxCodeService;
import it.thadumi.demo.taxcode.models.PhysicalPerson;
import lombok.*;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/taxcode")
public class TaxCodeEndpoint {
    @Inject
    private TaxCodeService taxCodeService;

    @POST
    @Path("/marshal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "500",
                            description = "Some error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseErrorModel.class))),
                    @APIResponse(
                            responseCode = "200",
                            description = "The tax code of the requested person.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TaxCodeModel.class)))
            }
    )
    public Response taxCodeOf(PhysicalPerson person) {

        return taxCodeService.marshal(person)
                             .fold(error -> Response.serverError()
                                                    .status(500)
                                                    .entity(ResponseErrorModel.of(error.getMessage())),
                                   taxCode -> Response.status(Response.Status.OK)
                                                      .entity(TaxCodeModel.of(taxCode)))
                .build();
    }

    @POST
    @Path("/unmarshal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "500",
                            description = "Some error",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseErrorModel.class))),
                    @APIResponse(
                            responseCode = "200",
                            description = "The person's information from the given tax code. " +
                                          "Note that the firstname and surname cannot be provided and therefore will be empty.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PhysicalPerson.class)))
            }
    )
    public Response personInformationFrom(TaxCodeModel taxCode) {
        return taxCodeService.unmarshal(taxCode.getTaxCode())
                            .fold(error -> Response.serverError()
                                                   .status(500)
                                                   .entity(ResponseErrorModel.of(error.getMessage())),
                                    person -> Response.status(Response.Status.OK)
                                                      .entity(person))
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxCodeModel {
        private String taxCode;

        public static TaxCodeModel of(String taxCode) {
            return new TaxCodeModel(taxCode);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseErrorModel {
        private String errorMessage;

        public static ResponseErrorModel of(String msg) {
            return new ResponseErrorModel(msg);
        }
    }
}

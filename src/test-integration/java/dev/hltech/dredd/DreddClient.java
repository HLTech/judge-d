package dev.hltech.dredd;

import dev.hltech.dredd.interfaces.rest.AggregatedValidationReportDto;
import dev.hltech.dredd.interfaces.rest.PactValidationForm;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public interface DreddClient {

    @POST
    @Path("/verification/pacts")
    AggregatedValidationReportDto validate(PactValidationForm form);

}

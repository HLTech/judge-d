package dev.hltech.dredd.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hltech.dredd.domain.environment.Environment;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.google.common.collect.Lists.newArrayList;

@RestController
public class ValidationController {

    @PostMapping(value = "verification/pacts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify pacts against providers from the environment", nickname = "Verify pats")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ValidationResultDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ValidationResultDto validatePacts(PactValidationForm pactValidationForm) {
        return createMockResponse();
    }

    @PostMapping(value = "verification/swagger", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify swagger against consumers from the environment", nickname = "Verify swagger")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ValidationResultDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ValidationResultDto validateSwagger(SwaggerValidationForm swaggerValidationForm) {
        return createMockResponse();
    }

    private ValidationResultDto createMockResponse() {
        return ValidationResultDto.builder()
            .verificationResult(newArrayList(
                ConsumerProviderValidationResultDto.builder()
                    .consumerName("consumerName")
                    .consumerVersion("1.0")
                    .providerName("providerName")
                    .providerVersion("1.0")
                    .interactions(newArrayList(
                        InteractionValidationResultDto.builder()
                            .name("some verification")
                            .verificationResult("OK")
                            .build(),
                        InteractionValidationResultDto.builder()
                            .name("some other verification")
                            .verificationResult("FAIL")
                            .errors(newArrayList("bad weather", "too cold"))
                            .build()
                    ))
                    .build()
            ))
            .build();
    }
}

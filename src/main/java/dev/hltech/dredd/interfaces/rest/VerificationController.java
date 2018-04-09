package dev.hltech.dredd.interfaces.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.google.common.collect.Lists.newArrayList;

@RestController
@RequiredArgsConstructor
public class VerificationController {

    @PostMapping(value = "verification/pacts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify pacts against providers from the environment", nickname = "Verify pats")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = VerificationResultDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public VerificationResultDto verifyPacts(PactVerificationForm pactVerificationForm) {
        return createMockResponse();
    }

    @PostMapping(value = "verification/swagger", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify swagger against consumers from the environment", nickname = "Verify swagger")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = VerificationResultDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public VerificationResultDto verifySwagger(SwaggerVerificationForm swaggerVerificationForm) {
        return createMockResponse();
    }

    private VerificationResultDto createMockResponse() {
        return VerificationResultDto.builder()
            .verificationResult(newArrayList(
                ConsumerProviderVerificationResultDto.builder()
                    .consumerName("consumerName")
                    .consumerVersion("1.0")
                    .providerName("providerName")
                    .providerVersion("1.0")
                    .interactions(newArrayList(
                        InteractionVerificationResultDto.builder()
                            .name("some verification")
                            .verificationResult("OK")
                            .build(),
                        InteractionVerificationResultDto.builder()
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

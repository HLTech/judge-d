package dev.hltech.dredd.interfaces.rest;

import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hltech.dredd.domain.InteractionValidationReport;
import dev.hltech.dredd.domain.PactValidationReport;
import dev.hltech.dredd.domain.PactValidator;
import dev.hltech.dredd.domain.ProviderNotAvailableException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static au.com.dius.pact.model.PactReader.loadPact;
import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.NO_SUCH_PROVIDER_ON_ENVIRONMENT;
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.OK;
import static java.util.stream.Collectors.toList;

@RestController
public class ValidationController {

    private final PactValidator pactValidator;
    private final ObjectMapper objectMapper;

    @Autowired
    public ValidationController(PactValidator pactValidator, ObjectMapper objectMapper) {
        this.pactValidator = pactValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "verification/pacts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify pacts against providers from the environment", nickname = "Verify pats")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationResultDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public AggregatedValidationResultDto validatePacts(@RequestBody PactValidationForm pactValidationForm)  {
            return new AggregatedValidationResultDto(
                pactValidationForm
                    .getPacts()
                    .stream()
                    .map(pact -> {
                        try {
                            return (RequestResponsePact) loadPact(objectMapper.writeValueAsString(pact));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("unable to parse pact", e);
                        }
                    })
                    .map(this::validatePact)
                    .flatMap(Collection::stream)
                    .collect(toList())
            );
    }

    private List<ContractValidationResultDto> validatePact(RequestResponsePact a) {
        try {
            return pactValidator.validate(a).stream().map(this::toDto).collect(toList());
        } catch (ProviderNotAvailableException e) {
            return newArrayList(ContractValidationResultDto.builder()
                    .consumerName(a.getConsumer().getName())
                    .providerName(a.getProvider().getName())
                    .validationStatus(NO_SUCH_PROVIDER_ON_ENVIRONMENT)
                    .build());
        }
    }

    private ContractValidationResultDto toDto(PactValidationReport input) {
        return ContractValidationResultDto.builder()
            .consumerName(input.getConsumerName())
            .providerName(input.getProviderName())
            .providerVersion(input.getProviderVersion())
            .validationStatus(OK)
            .interactions(input.getInteractionValidationReports()
                .stream()
                .map(this::toDto)
                .collect(toList())
            )
            .build();
    }

    private InteractionValidationResultDto toDto(InteractionValidationReport input) {
        return InteractionValidationResultDto.builder()
            .name(input.getName())
            .build();
    }

    @PostMapping(value = "verification/swagger", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify swagger against consumers from the environment", nickname = "Verify swagger")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationResultDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public AggregatedValidationResultDto validateSwagger(SwaggerValidationForm swaggerValidationForm) {
        return createMockResponse();
    }

    private AggregatedValidationResultDto createMockResponse() {
        return AggregatedValidationResultDto.builder()
            .validationResults(newArrayList(
                ContractValidationResultDto.builder()
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

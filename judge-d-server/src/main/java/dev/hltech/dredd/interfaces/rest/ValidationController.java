package dev.hltech.dredd.interfaces.rest;

import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hltech.dredd.domain.*;
import dev.hltech.dredd.domain.environment.KubernetesEnvironmentException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static au.com.dius.pact.model.PactReader.loadPact;
import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT;
import static dev.hltech.dredd.interfaces.rest.ContractValidationStatus.PERFORMED;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
public class ValidationController {

    private final PactValidator pactValidator;
    private final SwaggerValidator swaggerValidator;
    private final ObjectMapper objectMapper;

    @Autowired
    public ValidationController(PactValidator pactValidator, SwaggerValidator swaggerValidator, ObjectMapper objectMapper) {
        this.pactValidator = pactValidator;
        this.swaggerValidator = swaggerValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "verification/pacts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify pacts against providers from the environment", nickname = "Verify pats")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationReportDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public AggregatedValidationReportDto validatePacts(@RequestBody PactValidationForm pactValidationForm) {
        log.info("Received request {}", pactValidationForm);

        return new AggregatedValidationReportDto(
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

    @PostMapping(value = "verification/swagger", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify swagger against consumers from the environment", nickname = "Verify swagger")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationReportDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public AggregatedValidationReportDto validateSwagger(@RequestBody SwaggerValidationForm swaggerValidationForm) {
        log.info("Received request {}", swaggerValidationForm);

        return new AggregatedValidationReportDto(
            swaggerValidator.validate(swaggerValidationForm.getProviderName(), swaggerValidationForm.getSwagger())
                .stream()
                .map(ValidationController::toDto)
                .collect(toList())
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(KubernetesEnvironmentException.class)
    @ResponseBody
    public void unfinishableValidation() {
        log.error("Validation was not finished due to Kubernetes issues");
    }

    private List<ContractValidationReportDto> validatePact(RequestResponsePact a) {
        try {
            return pactValidator.validate(a).stream().map(ValidationController::toDto).collect(toList());
        } catch (ProviderNotAvailableException e) {
            return newArrayList(ContractValidationReportDto.builder()
                .consumerName(a.getConsumer().getName())
                .providerName(a.getProvider().getName())
                .validationStatus(FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT)
                .build());
        }
    }

    private static ContractValidationReportDto toDto(SwaggerValidationReport input) {
        return ContractValidationReportDto.builder()
            .consumerName(input.getConsumerName())
            .consumerVersion(input.getConsumerVersion())
            .providerName(input.getProviderName())
            .validationStatus(PERFORMED)
            .interactions(input.getInteractionValidationReports()
                .stream()
                .map(ValidationController::toDto)
                .collect(toList())
            )
            .build();
    }

    public static ContractValidationReportDto toDto(PactValidationReport input) {
        return ContractValidationReportDto.builder()
            .consumerName(input.getConsumerName())
            .providerName(input.getProviderName())
            .providerVersion(input.getProviderVersion())
            .validationStatus(PERFORMED)
            .interactions(input.getInteractionValidationReports()
                .stream()
                .map(ValidationController::toDto)
                .collect(toList())
            )
            .build();
    }

    public static InteractionValidationReportDto toDto(InteractionValidationReport input) {
        return InteractionValidationReportDto.builder()
            .interactionName(input.getName())
            .validationResult(input.getStatus())
            .errors(input.getErrors())
            .build();
    }

}

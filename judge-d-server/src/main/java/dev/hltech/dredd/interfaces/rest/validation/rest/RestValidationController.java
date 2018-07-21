package dev.hltech.dredd.interfaces.rest.validation.rest;

import au.com.dius.pact.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import dev.hltech.dredd.domain.validation.JudgeD;
import dev.hltech.dredd.domain.validation.ProviderNotAvailableException;
import dev.hltech.dredd.domain.validation.rest.RestContractValidator;
import dev.hltech.dredd.interfaces.rest.validation.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static dev.hltech.dredd.interfaces.rest.validation.ContractValidationStatus.FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT;
import static dev.hltech.dredd.interfaces.rest.validation.ContractValidationStatus.PERFORMED;
import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
public class RestValidationController {

    private final ObjectMapper objectMapper;
    private final JudgeD judgeD;
    private final RestContractValidator restCommunicationInterface;

    @Autowired
    public RestValidationController(
        ObjectMapper objectMapper,
        JudgeD judgeD,
        RestContractValidator restCommunicationInterface
    ) {
        this.objectMapper = objectMapper;
        this.judgeD = judgeD;
        this.restCommunicationInterface = restCommunicationInterface;
    }

    @PostMapping(value = "environments/{environment}/expectations-validation/rest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify pacts against providers from the environment", nickname = "Verify pats")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationReportDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public AggregatedValidationReportDto validatePacts(
        @PathVariable("environment") String environment,
        @RequestBody @Validated PactValidationForm pactValidationForm
    ) {
        log.info("Received request {}", pactValidationForm);
        List<ObjectNode> pacts = pactValidationForm.getPacts();

        List<ContractValidationReportDto> validationResults = pacts
            .stream()
            .map(pact -> {
                try {
                    return restCommunicationInterface.asExpectations(objectMapper.writeValueAsString(pact));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("unable to parse pact", e);
                }
            })
            .flatMap(pact -> validatePact(environment, pact).stream())
            .collect(toList());

        return new AggregatedValidationReportDto(validationResults);
    }

    private List<ContractValidationReportDto> validatePact(String environment, RequestResponsePact pact) {
        try {
            return judgeD
                .createContractValidator(environment, restCommunicationInterface)
                .validateExpectations(pact.getProvider().getName(), pact)
                .stream()
                .map(evr -> toDto(pact, evr))
                .collect(toList());
        } catch (ProviderNotAvailableException e) {
            return newArrayList(ContractValidationReportDto.builder()
                .consumerName(pact.getConsumer().getName())
                .providerName(pact.getProvider().getName())
                .validationStatus(FAILED_NO_SUCH_PROVIDER_ON_ENVIRONMENT)
                .build());
        }
    }

    @PostMapping(value = "environments/{environment}/capabilities-validation/rest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Verify swagger against consumers from the environment", nickname = "Verify swagger")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationReportDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public AggregatedValidationReportDto validateSwagger(
        @PathVariable("environment") String environment,
        @RequestBody @Validated SwaggerValidationForm swaggerValidationForm
    ) throws JsonProcessingException {
        log.info("Received request {}", swaggerValidationForm);
        String providerName = swaggerValidationForm.getProviderName();
        ObjectNode swagger = swaggerValidationForm.getSwagger();

        List<ContractValidationReportDto> collect = judgeD
            .createContractValidator(environment, restCommunicationInterface)
            .validateCapabilities(
                providerName,
                objectMapper.writeValueAsString(swagger)
            )
            .stream()
            .map(cvr -> toDto(providerName, cvr))
            .collect(toList());
        return new AggregatedValidationReportDto(collect);
    }


    private ContractValidationReportDto toDto(RequestResponsePact pact, InterfaceContractValidator.ExpectationValidationResult evr) {
        return new ContractValidationReportDto(
            pact.getConsumer().getName(),
            null,
            evr.getProviderName(),
            evr.getProviderVersion(),
            PERFORMED,
            evr.getInteractionValidationResults().stream().map(RestValidationController::toDto).collect(toList())
        );
    }

    private ContractValidationReportDto toDto(String providerName, InterfaceContractValidator.CapabilitiesValidationResult cvr) {
        return new ContractValidationReportDto(
            cvr.getConsumerName(),
            cvr.getConsumerVersion(),
            providerName,
            null,
            ContractValidationStatus.PERFORMED,
            cvr.getInteractionValidationResults().stream().map(RestValidationController::toDto).collect(toList())
        );
    }

    public static InteractionValidationReportDto toDto(InterfaceContractValidator.InteractionValidationResult input) {
        return InteractionValidationReportDto.builder()
            .interactionName(input.getName())
            .validationResult(input.getStatus())
            .errors(input.getErrors())
            .build();
    }

}

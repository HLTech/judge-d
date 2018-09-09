package dev.hltech.dredd.interfaces.rest.validation;

import dev.hltech.dredd.domain.JudgeD;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.validation.EnvironmentValidatorResult;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import dev.hltech.dredd.interfaces.rest.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static dev.hltech.dredd.interfaces.rest.validation.Converters.toDtos;

@RestController
public class ValidationController {

    private final JudgeD judgeD;
    private final ServiceContractsRepository serviceContractsRepository;
    private final List<InterfaceContractValidator<?, ?>> validators;

    @Autowired
    public ValidationController(
        JudgeD judgeD,
        ServiceContractsRepository serviceContractsRepository,
        List<InterfaceContractValidator<?, ?>> validators
    ) {
        this.judgeD = judgeD;
        this.serviceContractsRepository = serviceContractsRepository;
        this.validators = validators;
    }

    @GetMapping(value = "/environment-compatibility-report/{environment}/{serviceName}:{serviceVersion:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get validation report for contract between given service and given environment", nickname = "Validate against environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure"),
        @ApiResponse(code = 404, message = "Service not found"),})
    public List<ContractValidationReportDto> validate(
        @PathVariable("environment") String environment,
        @PathVariable("serviceName") String serviceName,
        @PathVariable("serviceVersion") String serviceVersion
    ) {
        ServiceContracts validatedServiceContracts = this.serviceContractsRepository.find(serviceName, serviceVersion)
            .orElseThrow(() -> new ResourceNotFoundException());

        List<EnvironmentValidatorResult> collect = this.validators.stream()
            .map(validator ->
                this.judgeD.validateServiceAgainstEnv(
                    validatedServiceContracts,
                    environment,
                    validator
                ))
            .collect(Collectors.toList());
        return toDtos(collect, serviceName, serviceVersion);
    }


}

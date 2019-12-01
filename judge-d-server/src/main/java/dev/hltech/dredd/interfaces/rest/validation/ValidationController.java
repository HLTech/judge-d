package dev.hltech.dredd.interfaces.rest.validation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.hltech.dredd.domain.JudgeD;
import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.domain.ServiceVersion;
import dev.hltech.dredd.domain.validation.EnvironmentValidatorResult;
import dev.hltech.dredd.domain.validation.InterfaceContractValidator;
import dev.hltech.dredd.interfaces.rest.RequestValidationException;
import dev.hltech.dredd.interfaces.rest.ResourceNotFoundException;
import dev.hltech.dredd.interfaces.rest.environment.ServiceDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

import static dev.hltech.dredd.interfaces.rest.validation.Converters.toDtos;
import static java.util.stream.Collectors.toList;

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

    @GetMapping(value = "/environment-compatibility-report", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get validation report for contract between set of services and given environment as if those services were first deployed", nickname = "Validate services against environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure"),
        @ApiResponse(code = 404, message = "Service not found")
    })
    public List<BatchValidationReportDto> validateAgainstEnvironments(
        @RequestParam("services") List<String> services,
        @RequestParam("environment") String environment
    ) {
        List<ServiceContracts> validatedServiceContracts = services.stream()
            .map(service -> {
                if (service.contains(":") && service.indexOf(":") == service.lastIndexOf(":")) {
                    String[] serviceNameAndVersion = service.split(":");
                    return new ServiceVersion(serviceNameAndVersion[0], serviceNameAndVersion[1]);
                } else {
                    throw new RequestValidationException();
                }
            })
            .map(serviceContractsRepository::findOne)
            .map(o -> o.orElseThrow(RequestValidationException::new))
            .collect(toList());

        Multimap<ServiceVersion, EnvironmentValidatorResult> validationResults = HashMultimap.create();
        this.validators.stream()
            .forEach(validator ->
                judgeD.validatedServicesAgainstEnvironment(
                    validatedServiceContracts,
                    environment,
                    validator
                )
                    .entrySet()
                    .stream()
                    .forEach(e -> validationResults.put(e.getKey(), e.getValue()))
            );

        return validationResults.asMap()
            .entrySet()
            .stream()
            .map(e -> {
                return BatchValidationReportDto.builder()
                    .service(ServiceDto.builder().name(e.getKey().getName()).version(e.getKey().getVersion()).build())
                    .validationReports(toDtos(e.getKey(), e.getValue()))
                    .build();
            })
            .collect(toList());
    }

    @GetMapping(value = "/environment-compatibility-report/{serviceName}:{serviceVersion:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get validation report for contract between given service and given environment", nickname = "Validate service against environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure"),
        @ApiResponse(code = 404, message = "Service not found")
    })
    public List<ContractValidationReportDto> validateAgainstEnvironments(
        @PathVariable("serviceName") String name,
        @PathVariable("serviceVersion") String version,
        @RequestParam("environment") List<String> environments
    ) {
        ServiceVersion serviceVersion = new ServiceVersion(name, version);
        ServiceContracts validatedServiceContracts = this.serviceContractsRepository.findOne(serviceVersion)
            .orElseThrow(ResourceNotFoundException::new);

        Collection<EnvironmentValidatorResult> collect = this.validators.stream()
            .map(validator ->
                this.judgeD.validateServiceAgainstEnvironments(
                    validatedServiceContracts,
                    environments,
                    validator
                ))
            .collect(toList());
        return toDtos(serviceVersion, collect);
    }
}

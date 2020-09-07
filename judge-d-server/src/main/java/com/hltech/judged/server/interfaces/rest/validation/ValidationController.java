package com.hltech.judged.server.interfaces.rest.validation;

import com.hltech.judged.server.domain.JudgeDApplicationService;
import com.hltech.judged.server.domain.ServiceId;
import com.hltech.judged.server.interfaces.rest.RequestValidationException;
import com.hltech.judged.server.interfaces.rest.environment.ServiceDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.hltech.judged.server.interfaces.rest.validation.Converters.toDtos;
import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class ValidationController {

    private final JudgeDApplicationService judgeD;

    @GetMapping(value = "/environment-compatibility-report", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get validation report for contract between set of services and given environment as if those services were first deployed", nickname = "Validate services against environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = BatchValidationReportDto.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure"),
        @ApiResponse(code = 404, message = "Service not found")
    })
    public List<BatchValidationReportDto> validateAgainstEnvironments(
        @RequestParam("services") List<String> services,
        @RequestParam("environment") String environment
    ) {
        List<ServiceId> serviceIds = services.stream()
            .map(service -> {
                if (service.contains(":") && service.indexOf(":") == service.lastIndexOf(":")) {
                    String[] serviceNameAndVersion = service.split(":");
                    return new ServiceId(serviceNameAndVersion[0], serviceNameAndVersion[1]);
                } else {
                    throw new RequestValidationException();
                }
            })
            .collect(Collectors.toUnmodifiableList());

        return judgeD.validatedServicesAgainstEnvironment(serviceIds, environment).asMap()
            .entrySet()
            .stream()
            .map(e -> BatchValidationReportDto.builder()
                .service(ServiceDto.builder().name(e.getKey().getName()).version(e.getKey().getVersion()).build())
                .validationReports(toDtos(e.getKey(), e.getValue()))
                .build())
            .collect(toList());
    }

    @GetMapping(value = "/environment-compatibility-report/{serviceName}:{serviceVersion:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get validation report for contract between given service and given environment", nickname = "Validate service against environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ContractValidationReportDto.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure"),
        @ApiResponse(code = 404, message = "Service not found")
    })
    public List<ContractValidationReportDto> validateAgainstEnvironments(
        @PathVariable("serviceName") String name,
        @PathVariable("serviceVersion") String version,
        @RequestParam("environment") List<String> environments
    ) {
        ServiceId serviceId = new ServiceId(name, version);

        return toDtos(serviceId, judgeD.validateServiceAgainstEnvironments(serviceId, environments));
    }
}

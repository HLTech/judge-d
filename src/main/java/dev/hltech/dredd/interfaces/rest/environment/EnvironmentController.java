package dev.hltech.dredd.interfaces.rest.environment;

import dev.hltech.dredd.domain.environment.Environment;
import dev.hltech.dredd.domain.environment.Service;
import dev.hltech.dredd.interfaces.rest.AggregatedValidationReportDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EnvironmentController {

    private final Environment environment;

    @Autowired
    public EnvironmentController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping(value = "environment/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get services from the environment", nickname = "Get Services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = AggregatedValidationReportDto.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<ServiceDto> getServices() {
        return environment.getAllServices()
            .stream()
            .map(EnvironmentController::toDto)
            .collect(Collectors.toList());
    }

    private static ServiceDto toDto(Service service) {
        return ServiceDto.builder()
            .name(service.getName())
            .version(service.getVersion())
            .build();
    }

}

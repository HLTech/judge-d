package dev.hltech.dredd.interfaces.rest.environment;

import dev.hltech.dredd.domain.environment.EnvironmentAggregate;
import dev.hltech.dredd.domain.environment.EnvironmentRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@RestController
public class EnvironmentController {

    private final EnvironmentRepository environmentRepository;

    @Autowired
    public EnvironmentController(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    @GetMapping(value = "environments", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get names of all persisted environments", nickname = "Get environments")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure")})
    public Set<String> getEnvironmentNames() {
        return environmentRepository.getNames();
    }

    @GetMapping(value = "environments/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get services from the environment", nickname = "Get Services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceDto.class, responseContainer = "list"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<ServiceDto> getEnvironment(@PathVariable("name") String name) {
        return environmentRepository.get(name).getAllServices()
            .stream()
            .map(sv -> new ServiceDto(sv.getName(), sv.getVersion()))
            .collect(toList());
    }

    @PutMapping(value = "environments/{name}")
    @ApiOperation(value = "Update the environment", nickname = "update environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Failure")})
    public void overwriteEnvironment(@PathVariable("name") String name, @RequestBody List<ServiceForm> services) {
        EnvironmentAggregate.EnvironmentAggregateBuilder builder = EnvironmentAggregate.builder(name);
        services.stream().forEach(sf -> builder.withServiceVersion(sf.getName(), sf.getVersion()));
        environmentRepository.persist(builder.build());
    }
}

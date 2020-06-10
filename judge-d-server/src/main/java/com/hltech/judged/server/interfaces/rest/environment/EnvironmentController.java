package com.hltech.judged.server.interfaces.rest.environment;

import com.google.common.collect.ImmutableSet;
import com.hltech.judged.server.domain.ServiceVersion;
import com.hltech.judged.server.domain.environment.EnvironmentAggregate;
import com.hltech.judged.server.domain.environment.EnvironmentAggregate.EnvironmentAggregateBuilder;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.hltech.judged.server.domain.environment.EnvironmentAggregate.DEFAULT_NAMESPACE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
    public void overwriteEnvironment(
        @PathVariable("name") String name,
        @RequestHeader(value = "X-JUDGE-D-AGENT-SPACE", defaultValue = DEFAULT_NAMESPACE, required = false) String agentSpace,
        @RequestBody Set<ServiceForm> services
    ) {
        agentSpace = firstNonNull(agentSpace, DEFAULT_NAMESPACE);
        EnvironmentAggregate environment = environmentRepository.get(name);
        Set<String> supportedSpaces = ImmutableSet.<String>builder().addAll(environment.getSpaceNames()).add(agentSpace).build();
        EnvironmentAggregateBuilder builder = EnvironmentAggregate.builder(name);
        for (String space : supportedSpaces) {
            if (agentSpace.equals(space)) {
                Set<ServiceVersion> collect = services.stream().map(sf -> new ServiceVersion(sf.getName(), sf.getVersion())).collect(toSet());

                builder.withServiceVersions(agentSpace, collect);
            } else {
                builder.withServiceVersions(space, environment.getServices(space));
            }
        }

        environmentRepository.persist(builder.build());
    }
}

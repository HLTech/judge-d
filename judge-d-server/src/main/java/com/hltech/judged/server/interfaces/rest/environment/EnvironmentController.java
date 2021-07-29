package com.hltech.judged.server.interfaces.rest.environment;

import com.hltech.judged.server.domain.JudgeDApplicationService;
import com.hltech.judged.server.domain.environment.Environment;
import com.hltech.judged.server.domain.environment.EnvironmentRepository;
import com.hltech.judged.server.domain.ServiceId;
import com.hltech.judged.server.domain.environment.Space;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

import static com.hltech.judged.server.domain.environment.Environment.DEFAULT_NAMESPACE;
import static java.util.stream.Collectors.toSet;

@RestController
@RequiredArgsConstructor
public class EnvironmentController {

    private final JudgeDApplicationService judgeD;
    private final EnvironmentRepository environmentRepository;

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
        @ApiResponse(code = 200, message = "Success", response = EnvironmentDto.SpaceDto.ServiceDto.class, responseContainer = "list"),
        @ApiResponse(code = 404, message = "Not found"),
        @ApiResponse(code = 500, message = "Failure")})
    public ResponseEntity<EnvironmentDto> getEnvironment(@PathVariable("name") String name) {
        return environmentRepository.find(name)
            .map(this::map)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(
        value = "environments/{name}",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Update the environment", nickname = "update environment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Failure")})
    public void overwriteEnvironment(
        @PathVariable("name") String name,
        @RequestHeader(value = "X-JUDGE-D-AGENT-SPACE", defaultValue = DEFAULT_NAMESPACE, required = false) String agentSpace,
        @RequestBody Set<ServiceForm> serviceForms
    ) {

        Set<ServiceId> serviceIds = serviceForms.stream()
            .map(sf -> new ServiceId(sf.getName(), sf.getVersion()))
            .collect(toSet());

        judgeD.overwriteEnvironment(name, agentSpace, serviceIds);
    }

    private EnvironmentDto map(Environment env) {
        return new EnvironmentDto(
            env.getName(),
            env.getSpaces().stream()
                .map(this::map)
                .collect(Collectors.toSet())
        );
    }

    private EnvironmentDto.SpaceDto map(Space space) {
        return new EnvironmentDto.SpaceDto(
            space.getName(),
            space.getServiceIds().stream()
                .map(this::map)
                .collect(Collectors.toSet())
        );
    }

    private EnvironmentDto.SpaceDto.ServiceDto map(ServiceId service) {
        return new EnvironmentDto.SpaceDto.ServiceDto(
            service.getName(),
            service.getVersion()
        );
    }
}

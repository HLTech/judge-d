package dev.hltech.dredd.interfaces.rest.contracts;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.interfaces.rest.validation.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class ContractsController {

    private ServiceContractsRepository serviceContractsRepository;

    @Autowired
    public ContractsController(ServiceContractsRepository serviceContractsRepository) {
        this.serviceContractsRepository = serviceContractsRepository;
    }

    @PostMapping(value = "/contracts/{provider}/{version}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register contracts for a version of a service", nickname = "register contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto create(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version, @RequestBody ServiceContractsForm form) {
        return toDto(serviceContractsRepository.persist(
            new ServiceContracts(
                provider,
                version,
                form.getCapabilities(),
                form.getExpectations()
            )
        ));
    }

    @GetMapping(value = "/contracts/{provider}/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register contracts for a version of a service", nickname = "register contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto create(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version) {
        return toDto(serviceContractsRepository.find(provider, version).orElseThrow(() -> new ResourceNotFoundException()));
    }

    private ServiceContractsDto toDto(ServiceContracts serviceContracts) {
        ServiceContractsDto dto = new ServiceContractsDto();
        dto.setName(serviceContracts.getName());
        dto.setVersion(serviceContracts.getVersion());

        dto.setCapabilities(serviceContracts.getCapabilities());
        dto.setExpectations(serviceContracts.getExpectations());
        return dto;
    }


}

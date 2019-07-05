package dev.hltech.dredd.interfaces.rest.contracts;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.interfaces.rest.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@Transactional
public class ContractsController {

    private ServiceContractsRepository serviceContractsRepository;
    private ContractsMapper mapper;

    @Autowired
    public ContractsController(ServiceContractsRepository serviceContractsRepository, ContractsMapper mapper) {
        this.serviceContractsRepository = serviceContractsRepository;
        this.mapper = mapper;
    }

    @PostMapping(value = "/contracts/{provider}/{version:.+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register contracts for a version of a service", nickname = "register contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto create(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version, @RequestBody ServiceContractsForm form) {
        return mapper.toDto(this.serviceContractsRepository.persist(
            new ServiceContracts(
                provider,
                version,
                mapper.mapCapabilitiesForm(form.getCapabilities()),
                mapper.mapExpectationsForm(form.getExpectations())
            )
        ));
    }

    @GetMapping(value = "/contracts/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get names of services with registered contracts", nickname = "get names of services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<String> getAvailableServiceNames() {
        return serviceContractsRepository.getServiceNames();
    }

    @GetMapping(value = "/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get contracts registered", nickname = "get names of services")
    @ApiResponses(value = {
        @ApiResponse(code = 302, message = "Found")
    })
    public RedirectView getServicesEndpointDescription()  {
        return new RedirectView("contracts/services", true);
    }


    @GetMapping(value = "/contracts/{serviceName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get versions of a service with registered contracts", nickname = "get versions of a service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<String> getServiceVersions(@PathVariable(name = "serviceName") String serviceName) {
        return serviceContractsRepository.find(serviceName)
            .stream()
            .map(ServiceContracts::getVersion).sorted()
            .collect(toList());
    }

    @GetMapping(value = "/contracts/{provider}/{version:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get contracts for a version of a service", nickname = "get contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto get(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version) {
        return mapper.toDto(this.serviceContractsRepository.find(provider, version).orElseThrow(ResourceNotFoundException::new));
    }

}

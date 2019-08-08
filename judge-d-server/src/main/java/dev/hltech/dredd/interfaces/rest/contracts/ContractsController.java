package dev.hltech.dredd.interfaces.rest.contracts;

import dev.hltech.dredd.domain.contracts.ServiceContracts;
import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import dev.hltech.dredd.interfaces.rest.ResourceNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.NoResultException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/contracts")
@Transactional
public class ContractsController {

    private ServiceContractsRepository serviceContractsRepository;
    private ContractsMapper mapper;

    @Autowired
    public ContractsController(ServiceContractsRepository serviceContractsRepository, ContractsMapper mapper) {
        this.serviceContractsRepository = serviceContractsRepository;
        this.mapper = mapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get registered contracts", nickname = "get names of services")
    @ApiResponses(value = {
        @ApiResponse(code = 302, message = "Found")
    })
    public RedirectView getServicesEndpointDescription()  {
        return new RedirectView("contracts/services", true);
    }

    @GetMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get names of services with registered contracts", nickname = "get names of services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<String> getAvailableServiceNames() {
        return serviceContractsRepository.getServiceNames();
    }

    /**
     * This endpoint is intended to server request checking if a service with given registered any contracts.
     * It will be extended to provide more detailed information on a service in the future.
     *
     * @param serviceName - name of the service that we are querying for
     * @return name of the service if contracts are present, NOT_FOUND (404) otherwise
     */
    @GetMapping(value = "/services/{serviceName}", produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Get details of a services with registered contracts", nickname = "get service details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 400, message = "Not found"),
        @ApiResponse(code = 500, message = "Failure")})
    public String getAvailableServiceNames(@PathVariable(name = "serviceName") String serviceName) {
        return serviceContractsRepository.getService(serviceName);
    }

    @ExceptionHandler(NoResultException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void notFound() { }

    @GetMapping(value = "/services/{serviceName}/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get versions of a service with registered contracts", nickname = "get versions of a service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<String> getAllServiceVersions(@PathVariable(name = "serviceName") String serviceName) {
        return serviceContractsRepository.find(serviceName)
            .stream()
            .map(ServiceContracts::getVersion).sorted()
            .collect(toList());
    }

    @GetMapping(value = "services/{serviceName}/versions/{version:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get contracts for a version of a service", nickname = "get contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto getContracts(@PathVariable(name = "serviceName") String serviceName, @PathVariable(name = "version") String version) {
        return mapper.toDto(this.serviceContractsRepository.find(serviceName, version).orElseThrow(ResourceNotFoundException::new));
    }

    @PostMapping(value = "services/{serviceName}/versions/{version:.+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register contracts for a version of a service", nickname = "register contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto registerContract(@PathVariable(name = "serviceName") String serviceName, @PathVariable(name = "version") String version, @RequestBody ServiceContractsForm form) {
        return mapper.toDto(this.serviceContractsRepository.persist(
            new ServiceContracts(
                serviceName,
                version,
                mapper.mapCapabilitiesForm(form.getCapabilities()),
                mapper.mapExpectationsForm(form.getExpectations())
            )
        ));
    }

    @PostMapping(value = "/{provider}/{version:.+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register contracts for a version of a service", nickname = "register contracts")
    @Deprecated
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

    @GetMapping(value = "/{serviceName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get versions of a service with registered contracts", nickname = "get versions of a service")
    @Deprecated
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

    @GetMapping(value = "/{provider}/{version:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get contracts for a version of a service", nickname = "get contracts")
    @Deprecated
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto get(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version) {
        return mapper.toDto(this.serviceContractsRepository.find(provider, version).orElseThrow(ResourceNotFoundException::new));
    }

}

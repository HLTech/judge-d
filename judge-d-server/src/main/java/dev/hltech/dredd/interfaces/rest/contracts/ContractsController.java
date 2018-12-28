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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;

@RestController
@Transactional
public class ContractsController {

    private ServiceContractsRepository serviceContractsRepository;

    @Autowired
    public ContractsController(ServiceContractsRepository serviceContractsRepository) {
        this.serviceContractsRepository = serviceContractsRepository;
    }

    @PostMapping(value = "/contracts/{provider}/{version:.+}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Register contracts for a version of a service", nickname = "register contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto create(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version, @RequestBody ServiceContractsForm form) {
        return toDto(this.serviceContractsRepository.persist(
            new ServiceContracts(
                provider,
                version,
                mapToEntity(form.getCapabilities()),
                form.getExpectations().entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapToEntity(entry.getValue())
                        )
                    )
            )
        ));
    }

    @GetMapping(value = "/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get names of services with registered contracts", nickname = "get names of services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<String> getAvailableServiceNames() {
        return serviceContractsRepository.getServiceNames();
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
            .map(sv -> sv.getVersion()).sorted()
            .collect(toList());
    }

    @GetMapping(value = "/contracts/{provider}/{version:.+}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get contracts for a version of a service", nickname = "get contracts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = ServiceContractsDto.class),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public ServiceContractsDto get(@PathVariable(name = "provider") String provider, @PathVariable(name = "version") String version) {
        return toDto(this.serviceContractsRepository.find(provider, version).orElseThrow(() -> new ResourceNotFoundException()));
    }

    private Map<String, ServiceContracts.Contract> mapToEntity(Map<String, ServiceContractsForm.ContractForm> protocolToContractForms) {
        return protocolToContractForms.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ServiceContracts.Contract(entry.getValue().getValue(), entry.getValue().getMimeType())
            ));
    }

    private Map<String, ServiceContractsDto.ContractDto> mapCapabilitiesToDto(Map<String, ServiceContracts.Contract> capabilities) {
        return capabilities.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ServiceContractsDto.ContractDto(entry.getValue().getValue(), entry.getValue().getMimeType())
            ));
    }

    private Map<String, Map<String, ServiceContractsDto.ContractDto>> mapExpectationsToDto(Map<ServiceContracts.ProviderProtocol, ServiceContracts.Contract> expectations) {
        HashMap<String, Map<String, ServiceContractsDto.ContractDto>> result = newHashMap();
        for (Map.Entry<ServiceContracts.ProviderProtocol, ServiceContracts.Contract> e : expectations.entrySet()) {
            ServiceContracts.ProviderProtocol pp = e.getKey();
            ServiceContracts.Contract contract = e.getValue();
            if (!result.containsKey(pp.getProvider())) {
                result.put(pp.getProvider(), newHashMap());
            }
            result.get(pp.getProvider()).put(pp.getProtocol(), new ServiceContractsDto.ContractDto(contract.getValue(), contract.getMimeType()));
        }
        return result;
    }

    private ServiceContractsDto toDto(ServiceContracts serviceContracts) {
        return new ServiceContractsDto(
            serviceContracts.getName(),
            serviceContracts.getVersion(),
            mapCapabilitiesToDto(serviceContracts.getCapabilitiesPerProtocol()),
            mapExpectationsToDto(serviceContracts.getExpectations())
        );
    }
}

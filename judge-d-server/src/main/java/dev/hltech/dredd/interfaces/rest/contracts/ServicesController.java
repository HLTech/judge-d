package dev.hltech.dredd.interfaces.rest.contracts;

import dev.hltech.dredd.domain.contracts.ServiceContractsRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class ServicesController {
    private ServiceContractsRepository serviceContractsRepository;

    @GetMapping(value = "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get names of services with registered contracts", nickname = "get names of services")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class, responseContainer = "list"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 500, message = "Failure")})
    public List<String> getAvailableServiceNames() {
        return serviceContractsRepository.getServiceNames();
    }

}

package dev.hltech.dredd.web;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GreetingController {

    @GetMapping(value = "greetings", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get greetings by message", nickname = "Get greetings by message")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "message", value = "Greeting's message", dataType = "string", paramType = "query")
    })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 500, message = "Failure")})
    public String get(
            @RequestParam(value = "message", required = false) String message) {
        return "Hello";
    }
}

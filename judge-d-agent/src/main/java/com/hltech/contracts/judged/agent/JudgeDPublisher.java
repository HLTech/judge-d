package com.hltech.contracts.judged.agent;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;

@FeignClient("judge-d-server")
public interface JudgeDPublisher {

    @PutMapping(path = "environments/{environment}", produces = "application/json")
    void publish(@PathVariable("environment") String environment, @RequestBody Set<ServiceForm> serviceForms);

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    class ServiceForm {

        private String name;
        private String version;

    }
}

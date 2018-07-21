package dev.hltech.dredd;

import dev.hltech.dredd.interfaces.rest.validation.AggregatedValidationReportDto;
import dev.hltech.dredd.interfaces.rest.validation.rest.PactValidationForm;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface DreddClient {

    @RequestMapping(value = "/verification/pacts", method = RequestMethod.GET)
    AggregatedValidationReportDto validate(PactValidationForm form);

}

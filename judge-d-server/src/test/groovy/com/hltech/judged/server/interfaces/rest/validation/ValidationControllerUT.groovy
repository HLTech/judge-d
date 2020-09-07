package com.hltech.judged.server.interfaces.rest.validation

import com.hltech.judged.server.domain.JudgeDApplicationService
import com.hltech.judged.server.interfaces.rest.RequestValidationException
import spock.lang.Specification

class ValidationControllerUT extends Specification {

    def judgeD = Mock(JudgeDApplicationService)

    def "should throw 400 when invalid format of service id" (){
        given:
            def controller = new ValidationController(judgeD)
        when:
            controller.validateAgainstEnvironments(["service.version", "service-without-version"] as List, "some-env")
        then:
            thrown RequestValidationException
    }
}

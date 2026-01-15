package connectro.rest

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class SearchExecutionControllerSpec extends Specification implements ControllerUnitTest<SearchExecutionController> {

     void "test index action"() {
        when:
        controller.index()

        then:
        status == 200

     }
}

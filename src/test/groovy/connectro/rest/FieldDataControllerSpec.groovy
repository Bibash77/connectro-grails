package connectro.rest

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class FieldDataControllerSpec extends Specification implements ControllerUnitTest<FieldDataController> {

     void "test index action"() {
        when:
        controller.index()

        then:
        status == 200

     }
}

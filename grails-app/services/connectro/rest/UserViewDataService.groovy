package connectro.rest

import com.fasterxml.jackson.databind.ObjectMapper
import dto.ApiResponse
import dto.UserSavedViewDto
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j;

@Transactional
@Slf4j
class UserViewDataService {

    ObjectMapper objectMapper = new ObjectMapper();

    def saveUserView(UserSavedViewDto userSavedViewDTO) {
        UserSavedView userSavedView = UserSavedView.fromDTO(userSavedViewDTO, objectMapper);
        if (!userSavedView.validate()) {
            log.error(userSavedView.errors as String);
            throw new RuntimeException("Validation failed")
        }
        userSavedView.save(flush: true, failOnError: true)
    }

    def updateUserView(Long id, UserSavedViewDto userSavedViewDTO) {
        def existingUserView = UserSavedView.get(id)
        if (!existingUserView) {
            log.error(existingUserView.errors as String);
            throw new RuntimeException("Validation failed")
        }
        try {
            UserSavedView userSavedView = UserSavedView.fromDTO(userSavedViewDTO, new ObjectMapper(), existingUserView)
            userSavedView.save('flush': true)
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to update user" + e.getMessage())
        }
    }

    List<UserSavedViewDto> getUserSavedViewList() {
        List<UserSavedView> userSavedViewDtoList = UserSavedView.findAll();
        def dtoList = userSavedViewDtoList.collect { UserSavedViewDto.fromEntity(it) }
        return dtoList;
    }

    def delete(Long id) {
        def view = UserSavedView.get(id)
        if (!view) {
            log.error(view.errors as String);
            throw new RuntimeException("Validation failed")
        }
        view.delete('flush': true)
        return id;
    }

}
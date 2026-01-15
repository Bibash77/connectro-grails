package connectro.rest

import dto.FieldDataDTO
import dto.UserSavedViewDto
import dto.ApiResponse
import grails.converters.JSON

class UserSavedViewController {
    UserViewDataService userViewDataService

    def findAll() {
        List<UserSavedViewDto> userSavedViewDtoList = userViewDataService.getUserSavedViewList()
        render new ApiResponse(userSavedViewDtoList, "Fetched all saved views") as JSON
    }

    def save() {
        try {
            UserSavedViewDto userSavedViewDTO = new UserSavedViewDto(request.JSON)
            userViewDataService.saveUserView(userSavedViewDTO)
            render new ApiResponse(null, "User view saved") as JSON
        } catch (Exception e) {
            render new ApiResponse("Error saving data: ${e.message}", false) as JSON
        }
    }

    def findById(Long id) {
        def view = UserSavedView.get(id)
        if (!view) {
            // only renders the element
            render new ApiResponse("UserSavedView not found with ID: $id", false) as JSON
            return
        }

        def dto = UserSavedViewDto.fromEntity(view)
        render new ApiResponse(dto, "Data fetched") as JSON
    }



    def dashboardInfo() {
        def view =
                UserSavedView.executeQuery("select usv.id, usv.viewName from UserSavedView usv where usv.showOneDashboard = true")
        if (!view) {
            // only renders the element
            render new ApiResponse("UserSavedView not found with ID: $id", false) as JSON
            return
        }


        def list = view.collect { [ id: it[0], viewName: it[1] ] }

        render new ApiResponse(list, "Data fetched") as JSON
    }

    def update(Long id) {
        try {
            UserSavedViewDto updatedDto = new UserSavedViewDto(request.JSON)
            userViewDataService.updateUserView(id, updatedDto)
            render new ApiResponse(null, "User view updated") as JSON
        } catch (Exception e) {
            render new ApiResponse("Error updating data: ${e.message}", false) as JSON
        }
    }

    def delete(Long id) {
        userViewDataService.delete(id);
        render new ApiResponse(null, "Deleted UserSavedView with ID: $id") as JSON
    }
}
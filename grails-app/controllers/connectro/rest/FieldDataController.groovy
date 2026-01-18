package connectro.rest

import dto.FieldDataDTO
import dto.ApiRenderResponse
import grails.converters.JSON

class FieldDataController {

    FieldDataService fieldDataService

    def findAll() {
        def list = fieldDataService.getFieldDataList()
        render new ApiRenderResponse(list, "Fetched all field data") as JSON
    }

    def save() {
        try {
            FieldDataDTO dto = new FieldDataDTO(request.JSON)
            fieldDataService.saveFieldData(dto)
            render new ApiRenderResponse(null, "Field data saved") as JSON
        } catch (Exception e) {
            render new ApiRenderResponse("Error saving data: ${e.message}", false) as JSON
        }
    }

    def findById(Long id) {
        def entity = fieldDataService.findById(id)
        if (!entity) {
            render new ApiRenderResponse("FieldData not found with ID: $id", false) as JSON
            return
        }
        def dto = FieldDataDTO.fromEntity(entity)
        render new ApiRenderResponse(dto, "Data fetched") as JSON
    }



    def update(Long id) {
        try {
            FieldDataDTO dto = new FieldDataDTO(request.JSON)
            fieldDataService.updateFieldData(id, dto)
            render new ApiRenderResponse(null, "Field data updated") as JSON
        } catch (Exception e) {
            render new ApiRenderResponse("Error updating data: ${e.message}", false) as JSON
        }
    }

    def delete(Long id) {
        try {
            fieldDataService.delete(id)
            render new ApiRenderResponse(null, "Deleted FieldData with ID: $id") as JSON
        } catch (Exception e) {
            render new ApiRenderResponse("Error deleting data: ${e.message}", false) as JSON
        }
    }
}

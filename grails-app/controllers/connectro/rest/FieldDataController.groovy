package connectro.rest

import dto.FieldDataDTO
import dto.ApiResponse
import grails.converters.JSON

class FieldDataController {

    FieldDataService fieldDataService

    def findAll() {
        def list = fieldDataService.getFieldDataList()
        render new ApiResponse(list, "Fetched all field data") as JSON
    }

    def save() {
        try {
            FieldDataDTO dto = new FieldDataDTO(request.JSON)
            fieldDataService.saveFieldData(dto)
            render new ApiResponse(null, "Field data saved") as JSON
        } catch (Exception e) {
            render new ApiResponse("Error saving data: ${e.message}", false) as JSON
        }
    }

    def findById(Long id) {
        def entity = fieldDataService.findById(id)
        if (!entity) {
            render new ApiResponse("FieldData not found with ID: $id", false) as JSON
            return
        }
        def dto = FieldDataDTO.fromEntity(entity)
        render new ApiResponse(dto, "Data fetched") as JSON
    }



    def update(Long id) {
        try {
            FieldDataDTO dto = new FieldDataDTO(request.JSON)
            fieldDataService.updateFieldData(id, dto)
            render new ApiResponse(null, "Field data updated") as JSON
        } catch (Exception e) {
            render new ApiResponse("Error updating data: ${e.message}", false) as JSON
        }
    }

    def delete(Long id) {
        try {
            fieldDataService.delete(id)
            render new ApiResponse(null, "Deleted FieldData with ID: $id") as JSON
        } catch (Exception e) {
            render new ApiResponse("Error deleting data: ${e.message}", false) as JSON
        }
    }
}

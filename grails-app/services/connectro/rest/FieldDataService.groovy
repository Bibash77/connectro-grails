package connectro.rest


import dto.FieldDataDTO
import groovy.util.logging.Slf4j
import grails.gorm.transactions.Transactional

@Transactional
@Slf4j
class FieldDataService {

    def saveFieldData(FieldDataDTO dto) {
        FieldData fieldData = dto.toEntity()
        if (!fieldData.validate()) {
            log.debug("Validation failed: ${fieldData.errors}")
            throw new RuntimeException("Validation failed")
        }
        fieldData.save(flush: true, failOnError: true)
    }

    def updateFieldData(Long id, FieldDataDTO dto) {
        def existing = FieldData.get(id)
        if (!existing) {
            log.error("FieldData not found with ID: $id")
            throw new RuntimeException("FieldData not found")
        }

        try {
            existing.properties = dto.toEntity().properties
            if (!existing.validate()) {
                log.debug("Validation failed: ${existing.errors}")
                throw new RuntimeException("Validation failed")
            }
            existing.save(flush: true, failOnError: true)
        } catch (Exception e) {
            log.error("Error updating FieldData: ${e.message}", e)
            throw new RuntimeException("Unable to update FieldData: ${e.message}")
        }
    }

    List<FieldDataDTO> getFieldDataList() {
        List<FieldData> list = FieldData.findAll()
        return list.collect { FieldDataDTO.fromEntity(it) }
    }

    def delete(Long id) {
        def fieldData = FieldData.get(id)
        if (!fieldData) {
            log.error("FieldData not found with ID: $id")
            throw new RuntimeException("FieldData not found")
        }
        fieldData.delete(flush: true)
        return id
    }

    FieldData findById(Long id) {
        return FieldData.get(id)
    }
}

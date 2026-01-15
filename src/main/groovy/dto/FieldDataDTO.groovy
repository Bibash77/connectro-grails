package dto

import connectro.rest.FieldData

class FieldDataDTO {
    String name
    String fieldName
    String type
    String displayName
    Boolean isFilterable
    Boolean isAggregatable
    Long id;

//    FieldDataDTO(Map json) {
//        this.name = json.name
//        this.fieldName = json.fieldName
//        this.type = json.type
//        this.displayName = json.displayName
//        this.isFilterable = json.isFilterable
//        this.isAggregatable = json.isAggregatable
//    }

    static FieldDataDTO fromEntity(FieldData entity) {
        return new FieldDataDTO([
                name: entity.name,
                fieldName: entity.fieldName,
                type: entity.type,
                displayName: entity.displayName,
                isFilterable: entity.isFilterable,
                isAggregatable: entity.isAggregatable,
                id: entity.id
        ])
    }

    FieldData toEntity() {
        return new FieldData(
                name: this.name,
                fieldName: this.fieldName,
                type: this.type,
                displayName: this.displayName,
                isFilterable: this.isFilterable,
                isAggregatable: this.isAggregatable
        )
    }
}


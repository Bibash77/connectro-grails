package dto

import groovy.json.JsonSlurper

class JsonUtils {
    static List safeParseList(String json, Class type) {
        try {
            return json ? (List) new JsonSlurper().parseText(json).collect { type.cast(it) } : []
        } catch (Exception e) {
            return []
        }
    }
}


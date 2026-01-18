package dto

class ApiRenderResponse<T> {
    T data
    String message
    boolean status

    ApiRenderResponse(T data, String message = "Success", boolean status = true) {
        this.data = data
        this.message = message
        this.status = status
    }

    ApiRenderResponse(String message, boolean status = false) {
        this(null, message, status)
    }
}

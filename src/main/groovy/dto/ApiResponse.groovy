package dto

class ApiResponse<T> {
    T data
    String message
    boolean status

    ApiResponse(T data, String message = "Success", boolean status = true) {
        this.data = data
        this.message = message
        this.status = status
    }

    ApiResponse(String message, boolean status = false) {
        this(null, message, status)
    }
}

package com.nano_electronics_cital.kheyr_backend.api;

public record ApiResponse<T>(
        String status,
        T data,
        String message
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message);
    }

    public static ApiResponse<Void> successMessage(String message) {
        return new ApiResponse<>("success", null, message);
    }

    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>("error", null, message);
    }
}

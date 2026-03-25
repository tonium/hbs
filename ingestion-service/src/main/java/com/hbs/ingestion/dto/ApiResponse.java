package com.hbs.ingestion.dto;

public record ApiResponse<T>(T data, ErrorDetail error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(null, new ErrorDetail(code, message));
    }

    public record ErrorDetail(String code, String message) {}
}

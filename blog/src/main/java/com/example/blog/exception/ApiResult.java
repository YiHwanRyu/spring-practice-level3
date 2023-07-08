package com.example.blog.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ApiResult {
    private String message;
    private String statusCode;

    public ApiResult(String message, String statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}

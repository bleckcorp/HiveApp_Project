package com.bctech.hive.exceptions;


import com.bctech.hive.dto.response.AppResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;

@RestControllerAdvice
public class AppExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(CustomException.class)
    public final ResponseEntity<AppResponse<ErrorDetails>> handleCustomException(CustomException ex, WebRequest request) {
        int statusCode =  HttpStatus.PRECONDITION_FAILED.value();
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message(ex.getMessage())
                .code(statusCode)
                .details(request.getDescription(true))
                .build();
        return ResponseEntity.status(statusCode).body(AppResponse.<ErrorDetails>builder()
                .message(errorDetails.getMessage())
                .error(errorDetails)
                .build());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<AppResponse<ErrorDetails>> handleAdminNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        int statusCode = ex.getStatus().value() != 0 ? ex.getStatus().value() : HttpStatus.PRECONDITION_FAILED.value();
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message(ex.getMessage())
                .code(statusCode)
                .details(request.getDescription(true))
                .build();
        return ResponseEntity.status(errorDetails.getCode()).body(AppResponse.<ErrorDetails>builder()
                .statusCode(HttpStatus.valueOf(statusCode).toString())
                .error(errorDetails)
                .build());
    }

    @ExceptionHandler(ResourceCreationException.class)
    public final ResponseEntity<AppResponse<ErrorDetails>> handleResourceCreationException(ResourceCreationException ex, WebRequest request) {
        int statusCode = ex.getStatus().value() != 0 ? ex.getStatus().value() : HttpStatus.PRECONDITION_FAILED.value();
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message(ex.getMessage())
                .code(statusCode)
                .details(request.getDescription(true))
                .build();
        return ResponseEntity.status(errorDetails.getCode()).body(AppResponse.<ErrorDetails>builder()
                .statusCode(HttpStatus.valueOf(statusCode).toString())
                .error(errorDetails)
                .build());
    }


    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<AppResponse<ErrorDetails>> handleBadRequestException(BadRequestException ex, WebRequest request) {
        int statusCode = ex.getStatus().value() != 0 ? ex.getStatus().value() : HttpStatus.PRECONDITION_FAILED.value();
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message(ex.getMessage())
                .code(statusCode)
                .details(request.getDescription(true))
                .build();
        return ResponseEntity.status(errorDetails.getCode()).body(AppResponse.<ErrorDetails>builder()
                .statusCode(HttpStatus.valueOf(statusCode).toString())
                .error(errorDetails)
                .build());
    }




}




package com.product.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(ApiException.class)
        protected ResponseEntity<ExceptionResponse> handleApiException(
                        ApiException exception,
                        WebRequest request) {

                ExceptionResponse response = new ExceptionResponse();

                response.setTimestamp(LocalDateTime.now());
                response.setStatus(exception.getStatus().value());
                response.setError(exception.getStatus());
                response.setMessage(exception.getMessage());
                response.setPath(
                                ((ServletWebRequest) request)
                                                .getRequest()
                                                .getRequestURI());

                return new ResponseEntity<>(
                                response,
                                exception.getStatus());
        }

        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException exception,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {

                ExceptionResponse response = new ExceptionResponse();

                response.setTimestamp(LocalDateTime.now());
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setError(HttpStatus.BAD_REQUEST);
                response.setMessage(
                                exception.getBindingResult()
                                                .getFieldError()
                                                .getDefaultMessage());
                response.setPath(
                                ((ServletWebRequest) request)
                                                .getRequest()
                                                .getRequestURI());

                return new ResponseEntity<>(
                                response,
                                HttpStatus.BAD_REQUEST);
        }
}
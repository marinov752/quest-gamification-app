package com.questgamification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        logger.error("Illegal argument exception: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(CustomApplicationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleCustomException(CustomApplicationException e, Model model) {
        logger.error("Custom application exception: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, Model model) {
        logger.error("Unexpected error occurred", e);
        model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        return "error";
    }
}


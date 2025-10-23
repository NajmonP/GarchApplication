package com.example.garchapplication.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(InvalidGarchParamsException.class)
    public String handleInvalidGarchParams(InvalidGarchParamsException ex,
                                           RedirectAttributes redirectAttributes) {
        String errorMessage = ex.getMessage() + " Součet vah: " + (ex.getLastVariance() + ex.getLastShock());
        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        return "redirect:/";
    }
}

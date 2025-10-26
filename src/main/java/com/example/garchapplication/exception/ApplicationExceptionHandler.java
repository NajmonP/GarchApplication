package com.example.garchapplication.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(MaxThresholdExceededException.class)
    public String MaxThresholdExceeded(MaxThresholdExceededException ex,
                                       RedirectAttributes redirectAttributes) {
        String errorMessage = ex.getMessage() + " Součet vah: " + ex.getSum();
        redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        return "redirect:/";
    }

    @ExceptionHandler(InvalidConstatVarianceException.class)
    public String InvalidConstatVarianceException(InvalidConstatVarianceException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(InvalidLastValueException.class)
    public String InvalidLastValueException(InvalidLastValueException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }
}

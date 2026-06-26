package com.ayush.ems.globlaexceptionhandler;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ayush.ems.helper.Message;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExists(UserAlreadyExistsException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", new Message("Registration failed: " + e.getMessage(), "alert-danger"));
        return "redirect:/signup";
    }

    @ExceptionHandler(InvalidCaptchaException.class)
    public String handleInvalidCaptcha(InvalidCaptchaException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", new Message("Captcha validation failed: " + e.getMessage(), "alert-danger"));
        return "redirect:/signup";
    }

    @ExceptionHandler(TermsNotAgreedException.class)
    public String handleTermsNotAgreed(TermsNotAgreedException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", new Message("Terms not agreed: " + e.getMessage(), "alert-danger"));
        return "redirect:/signup";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", new Message("An error occurred: " + e.getMessage(), "alert-danger"));
        return "redirect:/signup";
    }
}

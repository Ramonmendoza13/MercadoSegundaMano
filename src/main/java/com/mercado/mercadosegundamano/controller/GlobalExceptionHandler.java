package com.mercado.mercadosegundamano.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MultipartException.class,
            MaxUploadSizeExceededException.class,
            InvalidParameterException.class,
            IllegalArgumentException.class
    })
    public String handleUploadExceptions(HttpServletRequest request,
                                         RedirectAttributes redirectAttributes,
                                         Exception exception) {
        redirectAttributes.addFlashAttribute("error", buildUploadMessage(exception));
        return "redirect:" + resolveRedirectTarget(request);
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri == null) {
            return "/";
        }
        if (uri.startsWith("/profile/avatar")) {
            return "/profile";
        }
        if (uri.matches("^/my/products/\\d+/edit$")) {
            return uri;
        }
        if (uri.startsWith("/my/products")) {
            return "/my/products/new";
        }
        return "/";
    }

    private String buildUploadMessage(Exception exception) {
        String message = exception.getMessage();

        if (message != null && message.contains("FileCountLimitExceededException")) {
            return "Has superado el numero maximo de archivos permitidos en la subida.";
        }
        if (exception instanceof MaxUploadSizeExceededException) {
            return "Las imagenes superan el tamano maximo permitido.";
        }
        return "No se pudo completar la subida. Revisa el numero y el tamano de las imagenes.";
    }
}

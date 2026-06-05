package br.com.assinanet.security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class RequestErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "forward:/404";
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "forward:/index.html";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "forward:/500";
            }
        }
        return "error";
    }

    //@Override
    public String getErrorPath() {
        return "/error";
    }

}

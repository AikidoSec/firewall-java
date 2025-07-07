package com.example.demo;

import dev.aikido.agent_api.SetUser;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static dev.aikido.agent_api.SetUser.setUser;


@Component
@Order(0)
public class SetUserFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getHeader("user") != null) {
            // Useful for end2end tests:
            String id = httpRequest.getHeader("user");
            setUser(new SetUser.UserObject(id, "John Doe"));
        }
        chain.doFilter(request, response);
    }
}

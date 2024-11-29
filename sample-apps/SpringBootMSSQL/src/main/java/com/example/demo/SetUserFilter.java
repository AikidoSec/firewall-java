package com.example.demo;

import dev.aikido.agent_api.SetUser;
import dev.aikido.agent_api.ShouldBlockRequest;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

import static dev.aikido.agent_api.SetUser.setUser;

@Component
@Order(0)
public class SetUserFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        setUser(new SetUser.UserObject("123", "John Doe"));
        chain.doFilter(request, response);
    }
}

package com.example.demo;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;


@Component
@Order(0)
public class SetUserFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        //setUser(new SetUser.UserObject("123", "John Doe"));
        chain.doFilter(request, response);
    }
}

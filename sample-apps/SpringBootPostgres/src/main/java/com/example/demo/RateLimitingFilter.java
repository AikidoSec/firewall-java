package com.example.demo;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.PrintWriter;
import dev.aikido.agent_api.ShouldBlockRequest;

@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (ShouldBlockRequest.shouldBlockRequest()) {
            setResponse(response, "Rate Limited by Zen", 429);
            return;
        }
        chain.doFilter(request, response);
    }
    private static void setResponse(ServletResponse response, String text, int statusCode) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(statusCode);
        httpResponse.setContentType("text/plain");
        PrintWriter writer = httpResponse.getWriter();
        writer.write(text);
        writer.flush();
    }
}

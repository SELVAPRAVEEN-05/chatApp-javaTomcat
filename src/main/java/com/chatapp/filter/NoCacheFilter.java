package com.chatapp.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        // Prevent browser from caching pages
        httpResponse.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        httpResponse.setHeader(
                "Pragma",
                "no-cache"
        );

        httpResponse.setDateHeader(
                "Expires",
                0
        );

        // Continue the request
        chain.doFilter(request, response);
    }
}
package io.gigabyte.labs.deprecator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HeaderValidationFilter implements Filter {

    private EndpointConfiguration endpointConfig;

    @PostConstruct
    public void init() {
        this.endpointConfig = JsonUtil.readEndpointConfiguration();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        Optional<Endpoint> matchedEndpoint = endpointConfig.getEndpoints().stream()
          .filter(endpoint -> httpRequest.getServletPath().equals(endpoint.getPath()))
          .findFirst();

        if (matchedEndpoint.isPresent()) {
            Endpoint endpoint = matchedEndpoint.get();

            if (isBypassed(httpRequest, endpoint.getBypass())) {
                chain.doFilter(request, response);
                return;
            }

            // Validate against endpoint-specific headers or fall back to base_headers if endpoint headers don't exist
            if (endpoint.getHeaders() != null && !endpoint.getHeaders().isEmpty()) {
                if (!headersMatch(httpRequest, endpoint.getHeaders())) {
                    rejectRequest(response);
                    return;
                }
            } else {
                if (!headersMatch(httpRequest, endpointConfig.getBaseHeaders())) {
                    rejectRequest(response);
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    private boolean headersMatch(HttpServletRequest request, List<Header> headers) {
        if (headers == null || headers.isEmpty()) {
            return true;
        }

        return headers.stream().allMatch(header -> {
            String headerValue = Optional.ofNullable(request.getHeader(header.getName())).orElse("").toLowerCase();

            // ... (rest of the headersMatch function logic)

            Map<String, List<Header>> conditionalHeaders = header.getConditionalHeaders();
            if (conditionalHeaders != null && conditionalHeaders.containsKey(headerValue)) {
                return headersMatch(request, conditionalHeaders.get(headerValue));
            }

            return true;
        });
    }

    private boolean isBypassed(HttpServletRequest request, List<Header> bypassHeaders) {
        if (bypassHeaders == null) {
            return false;
        }
        return bypassHeaders.stream().anyMatch(header -> {
            String headerValue = request.getHeader(header.getName());
            return header.getValues().contains(headerValue);
        });
    }

    private void rejectRequest(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        httpResponse.getWriter().write("Request headers do not meet the required criteria.");
        httpResponse.flushBuffer();
    }

    // ... other methods and utility functions
}

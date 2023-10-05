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

    private EndpointConfiguration config; // Assume this is populated from the JSON.

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialize your filter here (like reading the configuration).
        // For the sake of example, let's assume EndpointConfiguration is loaded.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

        String requestPath = ((HttpServletRequest) request).getRequestURI();

        Optional<Endpoint> matchingEndpoint = config.getEndpoints().stream()
          .filter(endpoint -> requestPath.equals(endpoint.getPath()))
          .findFirst();

        if (matchingEndpoint.isPresent()) {
            Endpoint endpoint = matchingEndpoint.get();

            List<Header> effectiveHeaders = endpoint.isOverrideBaseHeaders() ?
              mergeHeaders(config.getBaseHeaders(), endpoint.getHeaders()) : config.getBaseHeaders();

            if (!headersMatch(request, effectiveHeaders)) {
                // Reject the request or redirect as necessary.
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean headersMatch(ServletRequest request, List<Header> headers) {
        return headers.stream().allMatch(header -> {
            String headerValue = ((HttpServletRequest) request).getHeader(header.getName());

            if ("reject".equals(header.getAction())) {
                return !header.getValues().contains(headerValue);
            } else if ("allow".equals(header.getAction())) {
                return header.getValues().contains(headerValue);
            }

            return true; // Default behavior.
        });
    }

    private List<Header> mergeHeaders(List<Header> baseHeaders, List<Header> endpointHeaders) {
        List<Header> mergedHeaders = new ArrayList<>(baseHeaders);

        for (Header endpointHeader : endpointHeaders) {
            mergedHeaders = mergedHeaders.stream()
              .filter(baseHeader -> !baseHeader.getAction().equals(endpointHeader.getAction()))
              .collect(Collectors.toList());
            mergedHeaders.add(endpointHeader);
        }

        return mergedHeaders;
    }

    @Override
    public void destroy() {
        // Cleanup resources (if any).
    }
}

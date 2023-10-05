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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        EndpointConfiguration endpointConfig = JsonUtil.readEndpointConfiguration();

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        Optional<Endpoint> matchedEndpoint = endpointConfig.getEndpoints().stream()
          .filter(endpoint -> httpRequest.getServletPath().equals(endpoint.getPath()))
          .findFirst();

        if (matchedEndpoint.isPresent()) {
            Endpoint endpoint = matchedEndpoint.get();

            // First, validate against base_headers
            if (!headersMatch(request, endpointConfig.getBaseHeaders())) {
                rejectRequest(response);
                return;
            }

            // Then, validate against endpoint-specific headers
            if (endpoint.getOverrideBaseHeaders() == null || !endpoint.getOverrideBaseHeaders()) {
                if (!headersMatch(request, endpoint.getHeaders())) {
                    rejectRequest(response);
                    return;
                }
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

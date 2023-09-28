package io.gigabyte.labs.deprecator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DeprecationInterceptor implements HandlerInterceptor {

    private List<Endpoint> endpoints; // Assume Endpoint is a POJO representing the structure in the JSON

    @PostConstruct
    public void init() {
        // Read and parse the JSON here using Jackson or any other method
        // Populate the endpoints list
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        Endpoint endpoint = endpoints.stream()
          .filter(e -> e.getPath().equals(path))
          .findFirst()
          .orElse(null);

        if (endpoint != null && endpoint.isDeprecated()) {
            if ("date".equals(endpoint.getCondition()) && LocalDate.now().isAfter(LocalDate.parse(endpoint.getConditionValue()))) {
                if (endpoint.getHeaders() == null || endpoint.getHeaders().isEmpty() || headersMatch(request, endpoint.getHeaders(), endpoint.getBypass())) {
                    response.setStatus(HttpServletResponse.SC_GONE); // HTTP 410 Gone
                    response.getWriter().write("This endpoint is deprecated.");
                    return false;
                }
            }
            // Add more conditions as needed
        }

        return true;
    }

    private boolean headersMatch(HttpServletRequest request, List<Header> requiredHeaders, List<Header> bypassHeaders) {

        // Check for bypass conditions first
        boolean isBypassed = bypassHeaders.stream()
          .anyMatch(bypassHeader -> bypassHeader.getValues()
            .contains(Optional.ofNullable(request.getHeader(bypassHeader.getName()))
              .orElse("")
              .toLowerCase())); // Convert to lowercase

        if (isBypassed) return false;

        // Then check for the regular conditions
        return requiredHeaders.stream().allMatch(header -> {
            String headerValue = Optional.ofNullable(request.getHeader(header.getName())).orElse("").toLowerCase(); // Convert to lowercase

            if ("reject".equals(header.getAction())) {
                return !header.getValues().stream().map(String::toLowerCase).collect(Collectors.toList()).contains(headerValue);
            } else if ("allow".equals(header.getAction())) {
                return header.getValues().stream().map(String::toLowerCase).collect(Collectors.toList()).contains(headerValue);
            }
            return true; // Default case
        });
    }

}

package utils.logging;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.filter.FilterableRequestSpecification;
import io.restassured.filter.FilterableResponseSpecification;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Attaches request/response to Allure only on failures (HTTP 4xx/5xx),
 * and emits a structured log (JSON string) to the console.
 */
public class AllureRequestResponseFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllureRequestResponseFilter.class);

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext ctx
    ) {
        long startNanos = System.nanoTime();
        Response response = ctx.next(requestSpec, responseSpec);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        int statusCode = response.getStatusCode();
        if (statusCode >= 400) {
            String correlationId = UUID.randomUUID().toString();
            String uri = resolveUri(requestSpec);
            String method = requestSpec.getMethod();

            Allure.addAttachment(
                    "Request",
                    method + " " + uri
            );
            Allure.addAttachment("Response (status=" + statusCode + ", ms=" + durationMs + ")", response.asString());

            logStructuredFailure(correlationId, method, uri, statusCode, durationMs);
        }

        return response;
    }

    private static String resolveUri(FilterableRequestSpecification requestSpec) {
        // Preferimos a URL final montada pelo Rest Assured.
        try {
            if (requestSpec.getURI() != null) {
                return requestSpec.getURI().toString();
            }
        } catch (Exception ignored) {
            // Fallback abaixo.
        }
        return "<unknown-uri>";
    }

    private static void logStructuredFailure(
            String correlationId,
            String method,
            String uri,
            int statusCode,
            long durationMs
    ) {
        String payload = toJsonString(Map.of(
                "event", "dog-api-failure",
                "correlationId", correlationId,
                "method", method,
                "uri", uri,
                "statusCode", statusCode,
                "durationMs", durationMs
        ));
        LOGGER.error(payload);
    }

    private static String toJsonString(Map<String, Object> map) {
        // Avoid extra library dependency for "structured logging".
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(escape(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append("\"").append(escape(String.valueOf(v))).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}


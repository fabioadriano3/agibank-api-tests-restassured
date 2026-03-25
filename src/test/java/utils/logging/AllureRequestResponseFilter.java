package utils.logging;

import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
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
            String method = safeInvokeToString(requestSpec, "getMethod", "UNKNOWN_METHOD");

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
        // Evita dependencia de metodos especificos do RestAssured entre versoes.
        String uri = safeInvokeToString(requestSpec, "getURI", null);
        if (uri != null && !uri.isBlank()) {
            return uri;
        }

        // Fallback razoavel: retorna a representacao do requestSpec.
        return requestSpec.toString();
    }

    private static String safeInvokeToString(Object target, String methodName, String defaultValue) {
        try {
            Method m = target.getClass().getMethod(methodName);
            Object value = m.invoke(target);
            if (value == null) return defaultValue;
            String s = String.valueOf(value);
            return s.isBlank() ? defaultValue : s;
        } catch (Exception ignored) {
            return defaultValue;
        }
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


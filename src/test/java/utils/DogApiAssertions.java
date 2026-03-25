package utils;

import models.DogApiResponse;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

public final class DogApiAssertions {
    private DogApiAssertions() {
    }

    public static void assertSuccessStatus(DogApiResponse<?> response) {
        Assertions.assertNotNull(response, "Response nula.");
        Assertions.assertNotNull(response.getStatus(), "Campo 'status' ausente.");
        Assertions.assertEquals("success", response.getStatus(), "Status inesperado.");
    }

    public static void assertError(DogApiResponse<?> response, int expectedCode) {
        Assertions.assertNotNull(response, "Response nula.");
        Assertions.assertNotNull(response.getStatus(), "Campo 'status' ausente.");
        Assertions.assertEquals("error", response.getStatus(), "Status de erro inesperado.");
        Assertions.assertNotNull(response.getMessage(), "Campo 'message' ausente.");
        Assertions.assertNotNull(response.getCode(), "Campo 'code' ausente.");
        Assertions.assertEquals(expectedCode, response.getCode(), "Code inesperado.");
    }

    public static void assertBreedsListAll(DogApiResponse<Map<String, List<String>>> response) {
        assertSuccessStatus(response);

        Map<String, List<String>> message = response.getMessage();
        Assertions.assertNotNull(message, "'message' nulo.");
        Assertions.assertFalse(message.isEmpty(), "A lista/mapa de breeds veio vazio.");

        // Amostra mínima para garantir consistência do tipo.
        int checked = 0;
        for (Map.Entry<String, List<String>> e : message.entrySet()) {
            Assertions.assertNotNull(e.getKey(), "Breed (key) nula.");
            Assertions.assertFalse(e.getKey().isBlank(), "Breed (key) vazia.");
            Assertions.assertNotNull(e.getValue(), "Sub-breeds (value) nulo para " + e.getKey());
            if (!e.getValue().isEmpty()) {
                Assertions.assertTrue(e.getValue().stream().allMatch(s -> s != null && !s.isBlank()),
                        "Sub-breeds contém strings vazias para " + e.getKey());
            }
            checked++;
            if (checked >= 5) break;
        }
    }

    public static void assertBreedImages(DogApiResponse<List<String>> response) {
        assertSuccessStatus(response);

        List<String> message = response.getMessage();
        Assertions.assertNotNull(message, "'message' nulo.");
        Assertions.assertFalse(message.isEmpty(), "Lista de imagens vazia.");

        // Checagem mínima do formato.
        int checked = 0;
        for (String url : message) {
            Assertions.assertNotNull(url, "URL nula.");
            Assertions.assertTrue(url.startsWith("http"), "URL inesperada: " + url);
            checked++;
            if (checked >= 3) break;
        }
    }

    public static void assertRandomImage(DogApiResponse<String> response) {
        assertSuccessStatus(response);

        String url = response.getMessage();
        Assertions.assertNotNull(url, "'message' nulo.");
        Assertions.assertFalse(url.isBlank(), "URL vazia.");
        Assertions.assertTrue(url.startsWith("http"), "URL inesperada: " + url);
    }
}


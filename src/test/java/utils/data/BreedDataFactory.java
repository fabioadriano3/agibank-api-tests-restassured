package utils.data;

import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Assertions;
import services.DogApiService;
import models.DogApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class BreedDataFactory {
    private BreedDataFactory() {
    }

    private static final Object LOCK = new Object();
    private static volatile List<String> cachedBreeds;

    public static List<String> getBreeds(DogApiService service) {
        if (cachedBreeds != null && !cachedBreeds.isEmpty()) {
            return cachedBreeds;
        }

        synchronized (LOCK) {
            if (cachedBreeds != null && !cachedBreeds.isEmpty()) {
                return cachedBreeds;
            }

            TypeRef<DogApiResponse<Map<String, List<String>>>> type =
                    new TypeRef<>() {
                    };

            try {
                DogApiResponse<Map<String, List<String>>> response =
                        service.getBreedsListAll().extract().as(type);
                Map<String, List<String>> message = response.getMessage();
                if (message == null || message.isEmpty()) {
                    cachedBreeds = Collections.emptyList();
                } else {
                    cachedBreeds = new ArrayList<>(message.keySet());
                }
            } catch (Exception e) {
                cachedBreeds = Collections.emptyList();
            }
        }

        return cachedBreeds == null ? Collections.emptyList() : cachedBreeds;
    }

    public static List<String> sampleValidBreeds(DogApiService service, int count, String fallbackBreed) {
        List<String> breeds = getBreeds(service);
        if (breeds.isEmpty()) {
            return List.of(fallbackBreed);
        }

        List<String> safe = breeds.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toList());
        Assertions.assertFalse(safe.isEmpty(), "Lista de breeds vazia, fallback não aplicável.");

        if (count <= 1) {
            return List.of(randomFrom(safe));
        }

        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<String> result = new ArrayList<>(count);
        while (result.size() < Math.min(count, safe.size())) {
            String candidate = safe.get(rnd.nextInt(safe.size()));
            if (!result.contains(candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    public static Stream<String> invalidBreeds(int count) {
        // Garante que não existe como breed (tamanho e prefixo "invalidbreed_").
        return IntStream.range(0, count)
                .mapToObj(i -> "invalidbreed_" + UUID.randomUUID());
    }

    private static String randomFrom(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }
}


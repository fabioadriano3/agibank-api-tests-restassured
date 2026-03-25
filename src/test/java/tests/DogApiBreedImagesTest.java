package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.ValidatableResponse;
import models.DogApiResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import utils.BaseTest;
import utils.DogApiAssertions;
import utils.data.BreedDataFactory;
import utils.schema.JsonSchemaAssertions;

import java.util.List;
import java.util.stream.Stream;

@Epic("Dog API")
@Feature("Breed Images")
@Tag("api")
public class DogApiBreedImagesTest extends BaseTest {

    @ParameterizedTest(name = "Breed {0} deve retornar success + imagens")
    @Story("GET /breed/{breed}/images - contrato + consistencia")
    @DisplayName("Breed images: 200 + schema + consistencia")
    @MethodSource("validBreedsProvider")
    void breedImages_happyPath_contractAndConsistency(String breed) throws Exception {
        ValidatableResponse response = dogApiService.getBreedImages(breed)
                .statusCode(200)
                .time(Matchers.lessThan((long) environment.responseTimeMaxMillis()));

        JsonSchemaAssertions.assertBreedImages(response);
        String body = response.extract().asString();

        TypeReference<DogApiResponse<List<String>>> type = new TypeReference<>() {
        };
        DogApiResponse<List<String>> dto = objectMapper.readValue(body, type);
        DogApiAssertions.assertBreedImages(dto);
    }

    private Stream<String> validBreedsProvider() {
        ensureInitialized();
        List<String> breeds = BreedDataFactory.sampleValidBreeds(
                dogApiService,
                environment.breedSampleCount(),
                environment.breedSample()
        );
        return breeds.stream();
    }

    @ParameterizedTest(name = "Breed invalido {0} deve retornar erro")
    @Story("GET /breed/{breed}/images - breed invalido")
    @DisplayName("Breed images: invalid breed -> 404 + error contract")
    @MethodSource("invalidBreedsProvider")
    void breedImages_negative_invalidBreed_returnsErrorContract(String invalidBreed) throws Exception {
        ValidatableResponse response = dogApiService.getBreedImages(invalidBreed)
                .statusCode(404)
                .time(Matchers.lessThan((long) environment.responseTimeMaxMillis()));

        JsonSchemaAssertions.assertErrorResponse(response);
        String body = response.extract().asString();

        TypeReference<DogApiResponse<String>> type = new TypeReference<>() {
        };
        DogApiResponse<String> dto = objectMapper.readValue(body, type);
        DogApiAssertions.assertError(dto, 404);
    }

    private Stream<String> invalidBreedsProvider() {
        ensureInitialized();
        // quantidade similar ao sampleCount para manter execução leve.
        int count = Math.max(3, environment.breedSampleCount());
        return BreedDataFactory.invalidBreeds(count);
    }

    @Test
    @Story("GET /breed/{breed}/images - endpoint incorreto")
    @DisplayName("Endpoint incorreto: 404 + error schema")
    void breedImages_negative_incorrectEndpoint_returnsErrorContract() throws Exception {
        // Typos no path
        String incorrectPath = "/breed/" + environment.breedSample() + "/imagess";

        ValidatableResponse response = dogApiService.getIncorrectEndpoint(incorrectPath)
                .statusCode(404)
                .time(Matchers.lessThan((long) environment.responseTimeMaxMillis()));

        JsonSchemaAssertions.assertErrorResponse(response);
        String body = response.extract().asString();

        TypeReference<DogApiResponse<String>> type = new TypeReference<>() {
        };
        DogApiResponse<String> dto = objectMapper.readValue(body, type);
        DogApiAssertions.assertError(dto, 404);
    }
}


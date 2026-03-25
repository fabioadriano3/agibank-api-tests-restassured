package tests;

import models.DogApiResponse;
import org.junit.jupiter.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.ValidatableResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import utils.BaseTest;
import utils.DogApiAssertions;
import utils.schema.JsonSchemaAssertions;

import java.util.List;
import java.util.Map;

@Epic("Dog API")
@Feature("Breeds")
@Tag("api")
public class DogApiBreedsListTest extends BaseTest {

    @Test
    @Story("GET /breeds/list/all")
    @DisplayName("Breeds list all: status 200 + schema + consistencia")
    void breedsListAll_happyPath_contractAndConsistency() {
        ValidatableResponse response = dogApiService.getBreedsListAll()
                .statusCode(200)
                .time(Matchers.lessThan((long) environment.responseTimeMaxMillis()));

        JsonSchemaAssertions.assertBreedsListAll(response);

        String body = response.extract().asString();
        TypeReference<DogApiResponse<Map<String, List<String>>>> type =
                new TypeReference<>() {
                };

        try {
            DogApiResponse<Map<String, List<String>>> dto = objectMapper.readValue(body, type);
            DogApiAssertions.assertBreedsListAll(dto);
        } catch (Exception e) {
            Assertions.fail("Falha ao desserializar response: " + e.getMessage());
        }
    }

    @Test
    @Story("GET /breeds/list/all - endpoint incorreto")
    @DisplayName("Endpoint incorreto: 404 + error schema")
    void breedsListAll_incorrectEndpoint_returnsErrorContract() {
        String incorrectPath = "/breeds/list/alll";

        ValidatableResponse response = dogApiService.getIncorrectEndpoint(incorrectPath)
                .statusCode(404)
                .time(Matchers.lessThan((long) environment.responseTimeMaxMillis()));

        JsonSchemaAssertions.assertErrorResponse(response);

        String body = response.extract().asString();
        TypeReference<DogApiResponse<String>> type = new TypeReference<>() {
        };

        try {
            DogApiResponse<String> dto = objectMapper.readValue(body, type);
            DogApiAssertions.assertError(dto, 404);
        } catch (Exception e) {
            Assertions.fail("Falha ao desserializar response: " + e.getMessage());
        }
    }
}


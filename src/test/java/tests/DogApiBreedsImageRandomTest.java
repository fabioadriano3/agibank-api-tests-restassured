package tests;

import com.fasterxml.jackson.core.type.TypeReference;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.ValidatableResponse;
import models.DogApiResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import utils.BaseTest;
import utils.DogApiAssertions;
import utils.schema.JsonSchemaAssertions;

@Epic("Dog API")
@Feature("Random Image")
@Tag("api")
public class DogApiBreedsImageRandomTest extends BaseTest {

    @Test
    @Story("GET /breeds/image/random - contrato + consistencia")
    @DisplayName("Random image: 200 + schema + consistencia")
    void breedsImageRandom_happyPath_contractAndConsistency() throws Exception {
        ValidatableResponse response = dogApiService.getBreedsImageRandom()
                .statusCode(200)
                .time(Matchers.lessThan((long) environment.responseTimeMaxMillis()));

        JsonSchemaAssertions.assertRandomImage(response);

        String body = response.extract().asString();
        TypeReference<DogApiResponse<String>> type = new TypeReference<>() {
        };
        DogApiResponse<String> dto = objectMapper.readValue(body, type);
        DogApiAssertions.assertRandomImage(dto);
    }

    @Test
    @Story("GET /breeds/image/random - endpoint incorreto")
    @DisplayName("Endpoint incorreto: 404 + error contract")
    void breedsImageRandom_negative_incorrectEndpoint_returnsErrorContract() throws Exception {
        String incorrectPath = "/breeds/image/randomx";

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


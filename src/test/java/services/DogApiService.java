package services;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class DogApiService {
    private final RequestSpecification requestSpec;

    public DogApiService(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    public ValidatableResponse getBreedsListAll() {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .get("/breeds/list/all")
                .then();
    }

    public ValidatableResponse getBreedImages(String breed) {
        return RestAssured.given()
                .spec(requestSpec)
                .pathParam("breed", breed)
                .when()
                .get("/breed/{breed}/images")
                .then();
    }

    public ValidatableResponse getBreedsImageRandom() {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .get("/breeds/image/random")
                .then();
    }

    public ValidatableResponse getIncorrectEndpoint(String relativePath) {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(relativePath)
                .then();
    }
}


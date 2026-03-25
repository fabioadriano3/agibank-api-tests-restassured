package utils.schema;

import io.restassured.response.ValidatableResponse;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public final class JsonSchemaAssertions {
    private JsonSchemaAssertions() {
    }

    public static void assertBreedsListAll(ValidatableResponse response) {
        response.body(matchesJsonSchemaInClasspath("schemas/breeds_list_all_response.schema.json"));
    }

    public static void assertBreedImages(ValidatableResponse response) {
        response.body(matchesJsonSchemaInClasspath("schemas/breed_images_response.schema.json"));
    }

    public static void assertRandomImage(ValidatableResponse response) {
        response.body(matchesJsonSchemaInClasspath("schemas/random_image_response.schema.json"));
    }

    public static void assertErrorResponse(ValidatableResponse response) {
        response.body(matchesJsonSchemaInClasspath("schemas/error_response.schema.json"));
    }
}


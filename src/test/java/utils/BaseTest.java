package utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import services.DogApiService;
import utils.config.EnvironmentConfigLoader;
import utils.config.TestEnvironment;
import utils.rest.RequestSpecificationFactory;

@ExtendWith(AllureJunit5.class)
public abstract class BaseTest {
    protected static TestEnvironment environment;
    protected static DogApiService dogApiService;
    protected static ObjectMapper objectMapper;

    @BeforeAll
    static void globalSetUp() {
        environment = EnvironmentConfigLoader.load();
        dogApiService = new DogApiService(RequestSpecificationFactory.create(environment));
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}


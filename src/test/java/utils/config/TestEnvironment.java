package utils.config;

public record TestEnvironment(
        String envName,
        String baseUri,
        int timeoutConnectMillis,
        int timeoutReadMillis,
        int responseTimeMaxMillis,
        String breedSample,
        int breedSampleCount
) {
}


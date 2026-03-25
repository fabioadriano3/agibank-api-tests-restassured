package utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.junit5.AllureJunit5;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtendWith;
import services.DogApiService;
import utils.config.EnvironmentConfigLoader;
import utils.config.TestEnvironment;
import utils.rest.RequestSpecificationFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

@ExtendWith(AllureJunit5.class)
public abstract class BaseTest {
    protected static TestEnvironment environment;
    protected static DogApiService dogApiService;
    protected static ObjectMapper objectMapper;

    protected static synchronized void ensureInitialized() {
        if (environment != null && dogApiService != null && objectMapper != null) {
            return;
        }

        environment = EnvironmentConfigLoader.load();
        dogApiService = new DogApiService(RequestSpecificationFactory.create(environment));
        objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @BeforeAll
    static void globalSetUp() {
        ensureInitialized();

        // Se a API externa estiver indisponível/no-dns, não faz sentido falhar o build por erro de rede.
        // Assim, os testes de contrato rodam quando houver conectividade.
        try {
            URI uri = URI.create(environment.baseUri());
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                Assumptions.assumeTrue(false, "Config inválida (host vazio) para dog.api.baseUri: " + environment.baseUri());
            }

            int port;
            if (uri.getPort() != -1) {
                port = uri.getPort();
            } else {
                port = "http".equalsIgnoreCase(uri.getScheme()) ? 80 : 443;
            }

            // Timeout curto para não travar o pipeline ao detectar falta de rede.
            int connectTimeoutMillis = Math.min(environment.timeoutConnectMillis(), 1000);

            // Valida DNS + conectividade básica.
            InetAddress.getByName(host);
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            }
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "API externa indisponível (skip): " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}


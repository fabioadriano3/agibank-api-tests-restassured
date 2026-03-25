package utils.rest;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import utils.config.TestEnvironment;
import utils.logging.AllureRequestResponseFilter;

public final class RequestSpecificationFactory {
    private RequestSpecificationFactory() {
    }

    public static RequestSpecification create(TestEnvironment environment) {
        // Centraliza baseUri e timeouts para facilitar manutencao.
        return new RequestSpecBuilder()
                .setBaseUri(environment.baseUri())
                .setRelaxedHTTPSValidation()
                // Allure: anexar request/response no reporte.
                .addFilter(new AllureRestAssured())
                // Estrutura extra: anexar apenas falhas e logs estruturados no console.
                .addFilter(new AllureRequestResponseFilter())
                // Logs detalhados para debug local (pode ser reduzido via LogDetail).
                .log(LogDetail.ALL)
                .build();
    }
}


# Dog API Automation

Projeto de automação de testes de API para a **Dog API (https://dog.ceo/dog-api/documentation)** usando **Java LTS**, **Rest Assured**, **JUnit 5** e **Allure Reports**.

Ele foi desenhado para ser **escalável**, **manutenível** e **executável** em qualquer ambiente (Linux, macOS e Windows), com separação clara entre:
- camada de requisições HTTP (services)
- camada de validações/contratos (schemas + assertions)
- camada de testes (tests)

---

## Descrição do projeto

O objetivo é cobrir, com qualidade de “QA sênior”, os seguintes endpoints públicos:
1. `GET /breeds/list/all`
2. `GET /breed/{breed}/images`
3. `GET /breeds/image/random`

Para cada endpoint, os testes cobrem:
- **Cenários positivos**: `200`, tempo de resposta aceitável, schema JSON válido, tipagem correta e consistência do conteúdo.
- **Cenários negativos**: `404` por `breed` inválido (quando aplicável), `endpoint` incorreto e retorno de contrato de erro.

Além disso:
- Validação de **schema JSON** (contrato) quando possível.
- **Logs detalhados** do request/response em caso de falha e logs estruturados no console.
- Execução **em paralelo** (JUnit 5) para ganho de performance.
- Relatório **Allure** com detalhes das requisições e respostas.

---

## Tecnologias utilizadas

- **Java**: 21 (LTS recente)
- **Maven**: build e gerenciamento de dependências
- **Rest Assured**: client DSL para requisições e assertions
- **JUnit 5**: engine de testes + parametrização
- **Jackson**: desserialização de responses em POJOs tipados
- **Allure Reports**
  - `allure-junit5`
  - `allure-rest-assured`
- **JSON Schema validation**
  - validação de contrato com schemas JSON versionados no repositório

---

## Pré-requisitos

1. **Java 21** instalado e disponível no `PATH`
2. **Maven** instalado e disponível no `PATH`
3. Acesso de rede ao domínio `dog.ceo`

Opcional (recomendado):
- Navegador para abrir o relatório HTML do Allure.

---

## Como rodar o projeto (passo a passo)

### 1) Executar testes localmente (ambiente default: `dev`)

```bash
mvn -B -ntp clean test
```

### 2) Executar testes escolhendo ambiente (`dev`, `homolog`, `prod`)

O ambiente é controlado pela system property `dog.api.env`:

```bash
mvn -B -ntp clean test -Ddog.api.env=homolog
```

ou:

```bash
mvn -B -ntp clean test -Ddog.api.env=prod
```

### 3) Gerar relatório Allure

O relatório é gerado automaticamente na fase `verify`.

```bash
mvn -B -ntp clean test verify
```

Saída:
- Allure Results: `target/allure-results`
- Allure Report: `target/allure-report`

Para visualizar:

1. Abra `target/allure-report/index.html` no navegador

---

## Execucao via scripts

O projeto tambem pode ser executado com scripts para simplificar a rotina:

### Mac/Linux

```bash
./run-tests.sh dev
```

Ou homolog/prod:

```bash
./run-tests.sh homolog
./run-tests.sh prod
```

Se o `run-tests.sh` nao for executavel no seu sistema, rode uma vez:

```bash
chmod +x ./run-tests.sh
```

### Windows (PowerShell/CMD)

```bat
run-tests.bat dev
```

Ou homolog/prod:

```bat
run-tests.bat homolog
run-tests.bat prod
```

Observacao:
- Os scripts chamam `mvn -B -ntp clean test verify` e passam `-Ddog.api.env=<ambiente>`.
- Se `mvn` nao estiver instalado, os scripts instruem a instalar Maven (ou adicionar `mvnw` no projeto).
- Relatorio Allure: `target/allure-report/index.html`

---

## Estrutura do projeto (organização real para manutenção)

```
src/
  test/
    java/
      tests/        -> classes de teste (assertions + cenários)
      services/     -> camada de requisições HTTP (sem lógica de assert)
      models/       -> POJOs de response (JSON -> objetos tipados)
      utils/
        config/     -> loader de config por ambiente
        data/       -> factories de dados (valid breeds / invalid breeds)
        logging/    -> filter para attachments no Allure e logs estruturados
        rest/       -> factory para RequestSpecification
        schema/     -> helpers para validação de JSON Schema
        (assertions)-> utilitários para validação consistente do contrato
    resources/
      config/       -> properties por ambiente (dev/homolog/prod)
      schemas/      -> JSON schemas versionados (contrato)
      junit-platform.properties -> paralelismo do JUnit
      logback-test.xml -> logging no console
  main/ (opcional) -> não utilizado neste projeto (tudo está no scope de testes)

pom.xml             -> dependências e plugins (inclui geração do Allure)
```

---

## Decisões técnicas (explicação como QA sênior)

### 1) Services retornam `ValidatableResponse`
O método de serviço encapsula a chamada HTTP e retorna `ValidatableResponse`.
Asserções e validações de contrato ficam no layer de testes, evitando misturar lógica de negócio/contrato com “construção de request”.

### 2) Schema JSON como contrato versionado
Para cada response principal existe um schema versionado em:
`src/test/resources/schemas/`

Isso ajuda a:
- detectar regressões de contrato rapidamente
- tornar a manutenção previsível (atualiza schema quando API muda)

### 3) Tipagem por POJOs com Jackson + `TypeReference`
Asserções não confiam apenas em “campos existem”.
O response é desserializado com tipos esperados (ex.: `Map<String, List<String>>`, `List<String>`, `String`).
Assim a validação pega erros de tipagem/estrutura.

### 4) Paralelismo com JUnit 5
`junit-platform.properties` habilita execução paralela com controle por threads.

O design evita estado mutável compartilhado na execução.
Factories usam cache com sincronização onde necessário.

### 5) Observabilidade: Allure + logs estruturados em falha
Um `Filter` adiciona attachments no Allure somente quando ocorre erro HTTP (`>= 400`).
Além disso, um log JSON é emitido no console para facilitar triagem rápida.

---

## CI/CD (GitHub Actions) - exemplo completo

Pipeline disponível em:
`.github/workflows/api-tests.yml`

Ele executa:
1. Build (com download/cache de dependências)
2. `mvn clean test verify`
3. Upload do `target/allure-report` como artefato

O relatório gerado contém:
- status/passo a passo dos testes
- falhas com request/response quando aplicável

---

## Como adicionar novos endpoints (processo recomendado)

1. Criar schema em `src/test/resources/schemas/` (se houver contrato estruturado)
2. Adicionar método no `DogApiService` para o endpoint
3. Criar classe em `src/test/java/tests/` com:
   - teste positivo
   - testes negativos (contrato de erro)
4. Validar tipagem com POJO e `TypeReference`
5. Rodar `mvn clean test verify` e validar `target/allure-report`

---

## Troubleshooting rápido

## Erro de contrato (schema)
- Verifique a alteração do response real da Dog API.
- Atualize o schema correspondente em `src/test/resources/schemas/`.

## Timeout de teste
- Ajuste `dog.api.responseTime.max.millis` no `config/<env>.properties`.

---


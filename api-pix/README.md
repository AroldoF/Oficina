# API REST de Transferências PIX

API Spring Boot 3 para criar contas e transferir valores entre elas por chave PIX. O projeto usa Java 17, JPA/Hibernate, H2, JUnit 5, Mockito, MockMvc, JaCoCo e Springdoc OpenAPI.

## Executar

```bash
mvn spring-boot:run
```

O banco H2 fica em memória. Para desenvolvimento, o console está disponível em `http://localhost:8080/h2-console`, usando a URL JDBC configurada em `application.yml`.

## Documentação da API

Com a aplicação em execução, o Swagger UI fica disponível em `http://localhost:8080/swagger-ui/index.html`. O contrato OpenAPI em JSON pode ser consultado em `http://localhost:8080/v3/api-docs`.

## Criar uma conta

`POST /api/v1/contas`

```json
{
  "titular": "Alice Silva",
  "chavePix": "alice@email.com",
  "saldoInicial": 100.00
}
```

O campo `saldoInicial` é opcional e assume `0.00` se não for enviado. Uma conta nova é criada ativa e retorna `201 Created`, com o cabeçalho `Location` apontando para a conta criada. A chave PIX deve ser única.

## Endpoint

`POST /api/v1/pix/transferir`

```json
{
  "contaOrigemId": 1,
  "chavePixDestino": "bob@email.com",
  "valor": 40.00
}
```

Resposta de sucesso (`200 OK`):

```json
{
  "transacaoId": 1,
  "contaOrigemId": 1,
  "contaDestinoId": 2,
  "valor": 40.00,
  "dataHora": "2026-10-13T10:00:00",
  "status": "CONCLUIDO"
}
```

## Regras aplicadas

- As duas contas precisam estar ativas.
- A origem deve ter saldo suficiente.
- A chave PIX de destino deve existir.
- Não é permitida transferência para a mesma conta.
- O limite por operação é R$ 5.000,00.
- Débito, crédito e gravação da transação são atômicos via `@Transactional`.

## Erros da API

| Situação | HTTP | Mensagem |
| --- | --- | --- |
| Saldo insuficiente | 400 | Saldo insuficiente para realizar a transferência |
| Chave PIX inexistente | 404 | Chave PIX não encontrada |
| Mesma conta | 400 | Não é possível transferir para a mesma conta |
| Limite excedido | 422 | Valor excede o limite diário/transação |
| Chave PIX já cadastrada | 409 | Já existe uma conta cadastrada com esta chave PIX |

## Testes e cobertura

```bash
mvn verify
```

O relatório HTML de cobertura é criado em `target/site/jacoco/index.html`; a verificação do build exige no mínimo 70% de cobertura de linhas.

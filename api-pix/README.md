# API REST de Transferências PIX

API Spring Boot 3 para transferir valores entre contas por chave PIX. O projeto usa Java 17, JPA/Hibernate, H2, JUnit 5, Mockito, MockMvc, JaCoCo e GitHub Actions.

## Executar

```bash
mvn spring-boot:run
```

O banco H2 fica em memória. Para desenvolvimento, o console está disponível em `http://localhost:8080/h2-console`, usando a URL JDBC configurada em `application.yml`.

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

## Testes e cobertura

```bash
mvn verify
```

O relatório HTML de cobertura é criado em `target/site/jacoco/index.html`; a verificação do build exige no mínimo 70% de cobertura de linhas.

---
author: Renata, Alessandra, Heitor e Aroldo
lang: pt-BR
subtitle: Testes automatizados em Java
theme: default
title: JUnit, Mockito e JaCoCo
---

# JUnit, Mockito e JaCoCo

## Testes automatizados em Java

**T01 --- Unitário / Integração**

Validação de regras de negócio em um microsserviço Java

Renata · Alessandra · Heitor · Aroldo

15/10

------------------------------------------------------------------------

# Objetivos da aula

Ao final, você deverá conseguir:

-   Diferenciar testes unitários e testes de integração.
-   Escrever testes com **JUnit 5**.
-   Isolar dependências com **Mockito**.
-   Medir cobertura com **JaCoCo**.
-   Avaliar o que os testes realmente comprovam.

------------------------------------------------------------------------

# O problema: como validar uma regra de negócio?

Considere uma regra de um microsserviço de pedidos:

> Um pedido só pode ser confirmado quando o pagamento estiver aprovado.

Perguntas para orientar o teste:

-   O pedido é confirmado quando o pagamento está aprovado?
-   O pedido continua pendente quando o pagamento é recusado?
-   O serviço trata corretamente uma falha na consulta do pagamento?

**Ideia central:** testar comportamentos observáveis, não apenas
executar linhas de código.

------------------------------------------------------------------------

# Três ferramentas, três responsabilidades

  Ferramenta    Responsabilidade
  ------------- ---------------------------------------------
  **JUnit 5**   Organizar, executar e verificar testes
  **Mockito**   Simular dependências e verificar interações
  **JaCoCo**    Medir cobertura de código durante os testes

Elas se complementam, mas não são intercambiáveis.

-   JUnit responde: *o teste passou?*
-   Mockito ajuda a responder: *como a unidade interagiu com suas
    dependências?*
-   JaCoCo responde: *quais partes do código foram executadas?*

------------------------------------------------------------------------

# Onde cada tipo de teste se encaixa?

## Teste unitário

Verifica uma unidade pequena --- normalmente uma classe ou método ---
com dependências externas isoladas.

-   Rápido e direcionado.
-   Fácil de executar com frequência.
-   Bom para regras de negócio e cenários de erro.

## Teste de integração

Verifica se componentes funcionam juntos, por exemplo:

-   Serviço + banco de dados.
-   Aplicação + API externa simulada.
-   Repositório + configuração de persistência.

**Atenção:** um teste que usa Mockito não é automaticamente um teste de
integração.

------------------------------------------------------------------------

# Exemplo de regra de negócio

Vamos testar um serviço que confirma pedidos apenas quando o pagamento
está aprovado.

``` java
public enum PaymentStatus {
    APPROVED, REJECTED, PENDING
}

public record Order(String id, boolean confirmed) {
    public Order confirm() {
        return new Order(id, true);
    }
}
```

O domínio representa os estados possíveis. O serviço aplica a regra e
coordena as dependências.

------------------------------------------------------------------------

# O serviço a ser testado

``` java
public interface PaymentGateway {
    PaymentStatus getStatus(String orderId);
}

public interface OrderRepository {
    Order findById(String orderId);
    void save(Order order);
}
```

``` java
public class OrderService {
    private final PaymentGateway payments;
    private final OrderRepository orders;

    public OrderService(PaymentGateway payments,
                        OrderRepository orders) {
        this.payments = payments;
        this.orders = orders;
    }

    public void confirmIfPaid(String orderId) {
        Order order = orders.findById(orderId);
        PaymentStatus status = payments.getStatus(orderId);

        if (status != PaymentStatus.APPROVED) {
            throw new IllegalStateException(
                "Pagamento não aprovado"
            );
        }

        orders.save(order.confirm());
    }
}
```

**Contrato esperado:** somente pagamentos aprovados permitem salvar o
pedido como confirmado.

------------------------------------------------------------------------

# JUnit 5: estrutura de um teste

``` java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExampleTest {
    @Test
    void shouldAddTwoNumbers() {
        int result = 2 + 3;

        assertEquals(5, result);
    }
}
```

-   `@Test`: identifica um método de teste.
-   `assertEquals`: compara o resultado esperado com o obtido.
-   O teste deve falhar quando o comportamento estiver incorreto.

Outras asserções comuns: `assertTrue`, `assertFalse`, `assertNull` e
`assertThrows`.

------------------------------------------------------------------------

# Padrão Arrange--Act--Assert

Uma estrutura simples para deixar os testes legíveis:

1.  **Arrange:** preparar dados e dependências.
2.  **Act:** executar a ação testada.
3.  **Assert:** verificar o resultado.

``` java
@Test
void shouldReturnExpectedValue() {
    // Arrange
    int a = 2;
    int b = 3;

    // Act
    int result = a + b;

    // Assert
    assertEquals(5, result);
}
```

O nome do teste deve descrever o cenário e o comportamento esperado.

------------------------------------------------------------------------

# Mockito: isolar dependências

Um teste unitário de `OrderService` não precisa chamar um gateway de
pagamento real nem acessar o banco.

Mockito permite criar objetos simulados (*mocks*):

``` java
PaymentGateway payments =
    mock(PaymentGateway.class);

OrderRepository orders =
    mock(OrderRepository.class);
```

Com mocks, podemos definir respostas para chamadas específicas e
verificar interações.

**Importante:** mocks são apropriados para dependências externas à
unidade; não devem substituir indiscriminadamente toda a lógica que se
quer testar.

------------------------------------------------------------------------

# Mockito: configurando o cenário

``` java
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@Test
void shouldConfirmOrderWhenPaymentIsApproved() {
    PaymentGateway payments = mock(PaymentGateway.class);
    OrderRepository orders = mock(OrderRepository.class);

    Order order = new Order("order-1", false);
    when(orders.findById("order-1")).thenReturn(order);
    when(payments.getStatus("order-1"))
        .thenReturn(PaymentStatus.APPROVED);

    OrderService service = new OrderService(payments, orders);

    service.confirmIfPaid("order-1");

    verify(orders).save(new Order("order-1", true));
}
```

-   `when(...).thenReturn(...)`: define a resposta do mock.
-   `verify(...)`: verifica se uma interação ocorreu.
-   `any(...)`: matcher para argumentos quando necessário.

------------------------------------------------------------------------

# Testando o cenário de falha

Quando o pagamento não está aprovado, o pedido não deve ser salvo como
confirmado.

``` java
@Test
void shouldNotSaveOrderWhenPaymentIsRejected() {
    PaymentGateway payments = mock(PaymentGateway.class);
    OrderRepository orders = mock(OrderRepository.class);

    when(orders.findById("order-1"))
        .thenReturn(new Order("order-1", false));
    when(payments.getStatus("order-1"))
        .thenReturn(PaymentStatus.REJECTED);

    OrderService service = new OrderService(payments, orders);

    assertThrows(
        IllegalStateException.class,
        () -> service.confirmIfPaid("order-1")
    );

    verify(orders, never()).save(any(Order.class));
}
```

Esse teste verifica duas propriedades:

-   A chamada lança a exceção esperada.
-   O repositório não salva o pedido.

------------------------------------------------------------------------

# O que testar além do caminho feliz?

Para a regra de confirmação, considere:

  Cenário                Resultado esperado
  ---------------------- --------------------------------------------
  Pagamento aprovado     Pedido confirmado e salvo
  Pagamento recusado     Exceção; pedido não salvo
  Pagamento pendente     Exceção; pedido não salvo
  Pedido inexistente     Erro apropriado, conforme o contrato
  Gateway indisponível   Falha tratada conforme a política definida

Não invente o comportamento de erro durante o teste: primeiro defina o
contrato esperado da aplicação.

------------------------------------------------------------------------

# JaCoCo: cobertura de código

JaCoCo instrumenta ou analisa o bytecode Java para calcular métricas
como:

-   **Cobertura de instruções:** instruções executadas.
-   **Cobertura de branches:** ramificações de decisões executadas,
    quando aplicável.
-   **Linhas e métodos:** métricas complementares.

A cobertura ajuda a encontrar áreas pouco exercitadas.

**Cobertura alta não garante testes bons.** Um teste pode executar uma
linha sem verificar o resultado correto.

------------------------------------------------------------------------

# JaCoCo com Maven

Adicione o plugin ao `pom.xml` do projeto Maven:

``` xml
<build>
  <plugins>
    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <version>0.8.13</version>
      <executions>
        <execution>
          <goals>
            <goal>prepare-agent</goal>
          </goals>
        </execution>
        <execution>
          <id>report</id>
          <phase>test</phase>
          <goals>
            <goal>report</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Execute:

``` bash
./mvnw test
```

O relatório HTML costuma ser gerado em:

``` text
target/site/jacoco/index.html
```

Verifique a versão do plugin compatível com a versão de Java usada no
projeto. A configuração acima é um exemplo de referência; ajuste-a
conforme o `pom.xml` e o build existentes.

------------------------------------------------------------------------

# Dependências de teste no Maven

Em um projeto Java com JUnit 5 e Mockito, uma configuração comum inclui:

``` xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-core</artifactId>
  <scope>test</scope>
</dependency>
```

Se o projeto usa Spring Boot, prefira avaliar o
`spring-boot-starter-test`, que normalmente já reúne JUnit Jupiter,
Mockito e outras ferramentas de teste.

**Boas práticas:** - Deixe versões gerenciadas pelo BOM do Spring Boot,
quando aplicável. - Confirme a configuração do Surefire e a versão do
Java. - Não adicione dependências redundantes sem necessidade.

------------------------------------------------------------------------

# Teste unitário versus integração

  -----------------------------------------------------------------------
  Aspecto                 Unitário                Integração
  ----------------------- ----------------------- -----------------------
  Escopo                  Uma unidade             Componentes combinados

  Banco real              Geralmente não          Pode usar

  Dependências            Frequentemente isoladas Parte delas é real

  Velocidade              Geralmente alta         Geralmente menor

  Falhas encontradas      Regra ou lógica local   Configuração e
                                                  contratos entre
                                                  componentes
  -----------------------------------------------------------------------

Um teste com Spring Boot e banco em memória pode ser útil, mas não
comprova necessariamente compatibilidade com o banco usado em produção.

Para maior confiança, use um ambiente de teste representativo, por
exemplo com Testcontainers, quando fizer sentido.

------------------------------------------------------------------------

# Um teste de integração: o que validar?

Imagine um endpoint `POST /orders/{id}/confirm`.

Um teste de integração pode validar:

1.  A aplicação sobe com a configuração de teste.
2.  A requisição chega ao controller.
3.  O serviço aplica a regra.
4.  O repositório persiste o resultado.
5.  A resposta HTTP corresponde ao contrato da API.

Dependendo do objetivo, use `@SpringBootTest`, testes de fatia como
`@WebMvcTest` ou testes de persistência específicos.

**Escolha o escopo mínimo que comprova o comportamento desejado.**

------------------------------------------------------------------------

# Erros comuns

-   Testar métodos privados diretamente em vez do comportamento público.
-   Verificar apenas chamadas, sem validar efeitos relevantes.
-   Usar mocks para tudo e nunca testar integrações reais.
-   Escrever testes dependentes da ordem de execução.
-   Testar implementação interna de forma excessiva.
-   Tratar cobertura como sinônimo de qualidade.
-   Não testar cenários de erro e limites.
-   Deixar testes passarem sem asserções significativas.

------------------------------------------------------------------------

# Live coding: desafio

Implemente e teste esta regra:

> Um pedido só pode ser confirmado quando o pagamento estiver aprovado.

Em duplas ou individualmente:

1.  Escreva o teste para pagamento aprovado.
2.  Escreva o teste para pagamento recusado.
3.  Use Mockito para simular o gateway e o repositório.
4.  Execute os testes.
5.  Gere o relatório JaCoCo.
6.  Identifique um cenário ainda não coberto e adicione um teste.

**Critério de conclusão:** os testes falham quando a regra é violada e
passam quando o comportamento está correto.

------------------------------------------------------------------------

# Discussão: como sabemos que o teste é bom?

Perguntas para a turma:

-   Um teste com 100% de cobertura pode estar errado?
-   Quando faz sentido usar um mock?
-   O que um teste unitário não consegue garantir?
-   Qual cenário de erro é mais importante neste serviço?
-   Como evitar que testes fiquem acoplados à implementação?

Não existe um percentual universal que garanta qualidade. Defina metas
de cobertura como indicadores, considerando risco, criticidade e custo
de manutenção.

------------------------------------------------------------------------

# Resumo

-   **JUnit 5:** estrutura e executa os testes.
-   **Mockito:** isola dependências e verifica interações.
-   **JaCoCo:** mede cobertura de código.
-   **Testes unitários:** validam comportamentos de unidades isoladas.
-   **Testes de integração:** validam componentes trabalhando em
    conjunto.
-   **Qualidade:** depende de cenários relevantes, boas asserções e
    testes confiáveis --- não apenas de cobertura.

------------------------------------------------------------------------

# Referências oficiais

-   JUnit 5 --- User Guide:
    https://junit.org/junit5/docs/current/user-guide/
-   Mockito --- documentação:
    https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html
-   JaCoCo --- documentação: https://www.jacoco.org/jacoco/trunk/doc/
-   Maven Surefire Plugin:
    https://maven.apache.org/surefire/maven-surefire-plugin/
-   Spring Boot --- Testing:
    https://docs.spring.io/spring-boot/reference/testing/

Consulte as versões compatíveis com o projeto usado na aula.

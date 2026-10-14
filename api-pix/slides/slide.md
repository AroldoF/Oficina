# Testes unitários em Java

## JUnit 5 · Mockito · JaCoCo

**Exemplo:** criação de conta Pix com Spring Boot

Apresentação para acompanhar no VS Code com a extensão `vscode-reveal`.

---

## O que vamos testar?

O serviço tem três comportamentos relevantes:

- Criar conta quando a chave Pix ainda não existe. {.fragment}
- Rejeitar uma chave Pix já cadastrada. {.fragment}
- Usar saldo zero quando o saldo inicial é `null`. {.fragment}

**Objetivo:** verificar comportamento, não apenas executar linhas.

---

## Três ferramentas, três responsabilidades

- **JUnit Jupiter:** organiza e executa os testes; oferece `@Test` e asserções. {.fragment}
- **Mockito:** substitui dependências por objetos simulados (*mocks*) e verifica interações. {.fragment}
- **JaCoCo:** mede cobertura de execução do código durante os testes. {.fragment}

JUnit e Mockito ajudam a testar. JaCoCo mede quais partes do código foram executadas.

---

## Estrutura do teste

```java
@Test
void deveCriarContaQuandoChavePixAindaNaoExiste() {
    // Arrange
    // Act
    // Assert
}
```

- **Arrange:** preparar dados e dependências. {.fragment}
- **Act:** executar o comportamento testado. {.fragment}
- **Assert:** verificar resultado e efeitos esperados. {.fragment}

Esse padrão é conhecido como AAA (*Arrange, Act, Assert*).

---

## `@ExtendWith(MockitoExtension.class)`

```java
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {
}
```

- `@ExtendWith` **vem do JUnit Jupiter**. {.fragment}
- `MockitoExtension` **vem do módulo Mockito para JUnit Jupiter**. {.fragment}
- A extensão inicializa os mocks e integra o ciclo de vida do Mockito aos testes. {.fragment}

Sem essa integração, anotações como `@Mock` não são inicializadas automaticamente por essa extensão.

---

## `@Mock`: criar uma dependência simulada

```java
@Mock
private ContaRepository contaRepository;
```

Um *mock* é um objeto controlado pelo teste. Ele substitui o repositório real, evitando acesso ao banco de dados neste teste unitário.

- Podemos programar o retorno de seus métodos. {.fragment}
- Podemos verificar se métodos foram chamados. {.fragment}
- Ele não persiste dados de verdade. {.fragment}

---

## `@InjectMocks`: montar a classe testada

```java
@InjectMocks
private ContaService contaService;
```

Mockito cria uma instância de `ContaService` e tenta injetar nela os mocks compatíveis.

Neste exemplo, `ContaService` tem um construtor que recebe `ContaRepository`. O mock é fornecido pelo Mockito, e o método `criar` executado é o método real do serviço.

**Não é necessário iniciar todo o Spring** para este teste unitário.

---

## `@Test`: marcar um caso de teste

```java
@Test
void deveCriarContaQuandoChavePixAindaNaoExiste() {
    // cenário e verificações
}
```

`@Test` é uma anotação do JUnit Jupiter. O mecanismo de testes descobre e executa o método.

Boas práticas para nomes:

- Indicar o comportamento esperado. {.fragment}
- Usar um cenário por teste. {.fragment}
- Evitar depender da ordem em que os testes são executados. {.fragment}

---

## Arrange: preparar a entrada

```java
CriarContaRequest request = new CriarContaRequest(
    "Maria Silva",
    "maria@example.com",
    new BigDecimal("100.00")
);
```

O objeto representa os dados enviados ao serviço: titular, chave Pix e saldo inicial.

Usamos `BigDecimal` para valores monetários, evitando os erros de precisão binária comuns em `float` e `double`.

> Ajuste os argumentos caso o construtor real de `CriarContaRequest` seja diferente.

---

## `when(...).thenReturn(...)`: configurar o mock

```java
when(contaRepository.existsByChavePix(request.chavePix()))
    .thenReturn(false);
```

Leitura: “quando o serviço consultar se esta chave Pix existe, o repositório simulado responderá `false`”.

- `when(...)` identifica a chamada simulada. {.fragment}
- `thenReturn(false)` define o retorno. {.fragment}
- Isso não executa uma consulta real ao banco. {.fragment}

O cenário de sucesso depende de o serviço receber `false` nessa verificação.

---

## Por que configurar `save`?

```java
when(contaRepository.save(any(Conta.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
```

Métodos de mocks retornam valores padrão quando não são configurados. Para um objeto, normalmente é `null`.

- `any(Conta.class)` combina com uma chamada de `save` que recebe uma conta. {.fragment}
- `thenAnswer(...)` permite calcular o retorno com base na chamada. {.fragment}
- `getArgument(0)` devolve o primeiro argumento: a própria entidade enviada ao `save`. {.fragment}

Isso é uma simulação simples — não representa uma persistência JPA completa nem gera um ID.

---

## Act: executar o método real

```java
ContaResponse response = contaService.criar(request);
```

O teste chama `criar` de verdade. Dentro dele, o serviço consulta e usa o repositório simulado.

O objetivo do teste unitário é verificar a lógica do `ContaService`, isolando a dependência externa, não testar o comportamento interno do Spring Data JPA.

---

## Assert: verificar o resultado

```java
assertNotNull(response);
```

`assertNotNull` é uma asserção do JUnit: falha se a resposta for `null`.

Outras asserções comuns:

- `assertEquals(esperado, obtido)` verifica igualdade. {.fragment}
- `assertTrue(condicao)` verifica uma condição verdadeira. {.fragment}
- `assertThrows(TipoException.class, executable)` verifica uma exceção. {.fragment}

Asserções são o que transforma uma execução em uma verificação automática.

---

## `verify`: verificar interações com o mock

```java
verify(contaRepository).existsByChavePix(request.chavePix());
verify(contaRepository).save(captor.capture());
```

`verify` faz parte do Mockito e verifica se uma chamada ocorreu.

- Sem modo adicional, `verify(mock).metodo()` espera uma chamada. {.fragment}
- `ArgumentCaptor` captura o argumento enviado ao método. {.fragment}
- Depois podemos verificar a entidade criada pelo serviço. {.fragment}

Use verificações de interação quando elas forem relevantes para o comportamento; não é necessário verificar cada chamada sem motivo.

---

## `ArgumentCaptor`: inspecionar a conta enviada

```java
ArgumentCaptor<Conta> captor = ArgumentCaptor.forClass(Conta.class);
verify(contaRepository).save(captor.capture());

Conta contaSalva = captor.getValue();
assertEquals("Maria Silva", contaSalva.getTitular());
assertEquals("maria@example.com", contaSalva.getChavePix());
assertEquals(0, contaSalva.getSaldo().compareTo(new BigDecimal("100.00")));
```

`ArgumentCaptor` pertence ao Mockito. Ele captura o argumento passado a `save`, para então inspecionar os dados.

Para `BigDecimal`, `compareTo` é usado aqui porque compara o valor numérico sem exigir escala igual (`100.0` e `100.00` representam o mesmo valor).

> Este exemplo pressupõe getters `getTitular()`, `getChavePix()` e `getSaldo()` na entidade `Conta`.

---

## Caso negativo: chave Pix duplicada

```java
when(contaRepository.existsByChavePix(request.chavePix()))
    .thenReturn(true);

assertThrows(
    ChavePixJaCadastradaException.class,
    () -> contaService.criar(request)
);

verify(contaRepository, never()).save(any(Conta.class));
```

- `assertThrows` é do JUnit. Confirma que a exceção esperada foi lançada. {.fragment}
- `never()` é do Mockito. Confirma que `save` não foi chamado. {.fragment}
- O teste protege tanto a regra de negócio quanto o efeito esperado no repositório. {.fragment}

---

## Outro caminho: saldo inicial nulo

No serviço, a regra é:

```java
BigDecimal saldoInicial = request.saldoInicial() == null
    ? BigDecimal.ZERO
    : request.saldoInicial();
```

O teste envia `null`, captura a entidade recebida pelo repositório e verifica que seu saldo é `BigDecimal.ZERO`.

Esse cenário cobre um caminho diferente do cadastro com saldo informado. Testes bons cobrem comportamentos relevantes, não apenas linhas distintas.

---

## Dependência de teste no Maven

Se o projeto já usa o starter de testes do Spring Boot, normalmente basta:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ele reúne JUnit Jupiter, Mockito e outras ferramentas comuns de teste. Não adicione versões manuais sem necessidade quando o Spring Boot já gerencia as dependências.

---

## JaCoCo: medir cobertura

JaCoCo é uma ferramenta de cobertura. Ela instrumenta a execução dos testes e gera um relatório sobre instruções, linhas e ramificações executadas.

Ele **não cria testes nem prova que as asserções estão corretas**.

No `pom.xml`, adicione o plugin ao bloco `<build><plugins>`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.15</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

A versão `0.8.15` tem suporte oficial a Java 26; portanto, cobre também o Java 25 usado neste exemplo.

---

## Executar testes e criar relatório

No terminal, na raiz do projeto Maven:

```bash
./mvnw clean verify
```

Ou, sem Maven Wrapper:

```bash
mvn clean verify
```

O relatório HTML costuma ser criado em:

`target/site/jacoco/index.html`

Abra o arquivo no navegador e navegue até `ContaService` para ver a cobertura da classe.

---

## Cobertura não é qualidade

- 100% de cobertura significa que as partes mensuradas foram executadas; não garante que todos os resultados foram verificados. {.fragment}
- Uma asserção fraca pode permitir que um bug passe mesmo com cobertura alta. {.fragment}
- Combine cenários de sucesso, erro e limites relevantes. {.fragment}

**Prioridade:** testes que verificam comportamentos e regras de negócio; cobertura como indicador auxiliar.

---

## Resumo

- **JUnit:** descreve, executa e verifica os casos de teste.
- **Mockito:** isola dependências e verifica interações.
- **JaCoCo:** informa o que foi executado.

Fluxo usado nesta aula:

**Arrange → Act → Assert → medir cobertura**

---

## Referências oficiais

- JUnit 5: https://junit.org/junit5/docs/current/user-guide/
- Mockito: https://javadoc.io/doc/org.mockito/mockito-junit-jupiter/latest/org/mockito/junit/jupiter/MockitoExtension.html
- Spring Boot — dependências de teste: https://docs.spring.io/spring-boot/reference/testing/test-scope-dependencies.html
- JaCoCo Maven Plugin: https://www.jacoco.org/jacoco/trunk/doc/maven.html
- Reveal.js — realce de código: https://revealjs.com/code/

# Entendendo um teste com JUnit e Mockito

## Cadastro de conta Pix com `ContaService`

**Objetivo:** entender o que cada anotação e chamada faz, não apenas copiar o teste.

<small>Material para vscode-reveal / Reveal.js</small>

---

# O que estamos testando?

O método `ContaService.criar(request)`:

- verifica se a chave Pix já existe;
- define o saldo inicial como zero se ele for `null`;
- salva uma entidade `Conta`;
- transforma a entidade em `ContaResponse`.

<small class="fragment">O teste unitário verifica a lógica do serviço sem precisar subir o Spring nem conectar a um banco real.</small>

---

# Antes de começar: JUnit e Mockito

**JUnit 5** organiza e executa os testes. Ele fornece `@Test`, `@BeforeEach` e asserções como `assertThrows`.

**Mockito** cria objetos simulados (*mocks*) e permite controlar o comportamento das dependências.

<small class="fragment">Eles são complementares: JUnit executa e verifica; Mockito isola o serviço das dependências externas.</small>

---

# `@Test` — qual método é um teste?

```java
@Test
void deveCriarContaComSucesso() {
    // cenário e verificações
}
```

- `@Test` vem do JUnit Jupiter.
- Informa ao mecanismo de testes que aquele método deve ser executado como teste.
- O nome do método deve descrever o comportamento esperado.

<small class="fragment">Um teste deve verificar um comportamento observável, e não simplesmente executar o método sem conferir nada.</small>

---

# `@ExtendWith(MockitoExtension.class)`

```java
@ExtendWith(MockitoExtension.class)
class ContaServiceTest {
}
```

- `@ExtendWith` é uma anotação do **JUnit 5** que registra uma extensão para participar do ciclo de vida dos testes.
- `MockitoExtension.class` é a extensão fornecida pelo **Mockito** para integrar seus recursos ao JUnit.
- Ela inicializa e gerencia as anotações Mockito, como `@Mock`, em cada teste.

<small class="fragment">Sem essa extensão, as anotações Mockito não são inicializadas automaticamente nesse estilo de teste — seria necessário inicializá-las de outra forma.</small>

---

# `@Mock` — criar uma dependência simulada

```java
@Mock
private ContaRepository contaRepository;
```

- `@Mock` vem do Mockito.
- Cria um objeto falso que implementa o comportamento da dependência, controlado pelo teste.
- Não executa as consultas reais do repositório nem acessa o banco.

<small class="fragment">Neste teste, nós decidimos se `existsByChavePix(...)` retorna `true` ou `false`.</small>

---

# `@InjectMocks` — montar a classe testada

```java
@InjectMocks
private ContaService contaService;
```

- `@InjectMocks` vem do Mockito.
- Mockito cria ou prepara a instância de `ContaService` e tenta injetar nela os mocks necessários.
- Como `ContaService` recebe `ContaRepository` no construtor, o mock pode ser injetado por esse construtor.

<small class="fragment">Isso não inicia o Spring. É uma instanciação gerenciada pelo Mockito, apropriada para um teste unitário simples.</small>

---

# `@Mock` + `@InjectMocks`: o desenho

```text
ContaServiceTest
    |
    | @InjectMocks
    v
ContaService  -------- usa --------> ContaRepository simulado
                                        (@Mock)
```

O serviço é real. O repositório é simulado.

<small class="fragment">Assim, o teste concentra-se nas decisões de `ContaService`, não no funcionamento do banco de dados.</small>

---

# `when(...).thenReturn(false)`

```java
when(contaRepository.existsByChavePix(request.chavePix()))
    .thenReturn(false);
```

- `when(...)` diz qual chamada simulada queremos configurar.
- `existsByChavePix(...)` é o método do repositório que o serviço consulta.
- `thenReturn(false)` define o resultado que o mock devolverá quando essa chamada ocorrer.

<small class="fragment">Neste cenário, o teste está dizendo: “finja que essa chave Pix ainda não está cadastrada”.</small>

---

# Por que configurar esse retorno?

O serviço contém uma decisão semelhante a esta:

```java
if (contaRepository.existsByChavePix(request.chavePix())) {
    throw new ChavePixJaCadastradaException();
}
```

- Se o mock retornar `true`, esperamos a exceção.
- Se retornar `false`, o fluxo pode continuar para salvar a conta.

<small class="fragment">O teste controla a entrada da dependência para verificar os diferentes caminhos da regra de negócio.</small>

---

# `any(Conta.class)` — aceitar qualquer Conta

```java
when(contaRepository.save(any(Conta.class)))
```

- `any(...)` é um *argument matcher* do Mockito.
- `any(Conta.class)` corresponde a um argumento do tipo `Conta`.
- Evita que o teste precise construir previamente exatamente a mesma instância que o serviço criará.

<small class="fragment">Atenção: matchers como `any()` são usados na configuração e verificação de mocks. Não são valores reais para passar ao código de produção.</small>

---

# `thenAnswer(...)` — calcular a resposta do mock

```java
when(contaRepository.save(any(Conta.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
```

- `thenAnswer` define uma resposta dinâmica para a chamada simulada.
- `invocation` representa a chamada feita ao mock.
- `getArgument(0)` recupera o primeiro argumento passado para `save`, neste caso a própria `Conta`.

<small class="fragment">O mock devolve a entidade recebida. É uma simulação simples: não grava no banco nem gera necessariamente um ID como um repositório JPA real faria.</small>

---

# `thenReturn` ou `thenAnswer`?

**`thenReturn(valor)`**

Use quando a resposta é conhecida e fixa, por exemplo, `false` para indicar que a chave não existe.

**`thenAnswer(invocation -> ...)`**

Use quando a resposta depende dos argumentos da chamada ou precisa ser calculada.

<small class="fragment">Para `save`, devolver o argumento recebido pode ser útil. Em outros testes, você pode devolver uma entidade preparada com ID para simular um resultado mais próximo do esperado.</small>

---

# `assertThrows` — confirmar uma exceção

```java
assertThrows(
    ChavePixJaCadastradaException.class,
    () -> contaService.criar(request)
);
```

- `assertThrows` vem do JUnit.
- Verifica se a ação executada lança uma exceção do tipo esperado.
- `() -> ...` é uma expressão lambda que adia a execução da chamada até o JUnit avaliá-la.

<small class="fragment">Se nenhuma exceção for lançada, ou se for lançada uma exceção incompatível, o teste falha.</small>

---

# `verify(...)` — verificar uma interação

```java
verify(contaRepository).save(any(Conta.class));
```

- `verify` vem do Mockito.
- Verifica se o mock recebeu a chamada especificada durante o teste.
- Por padrão, verifica que a chamada ocorreu uma vez.

<small class="fragment">É útil quando a interação é parte do comportamento esperado — por exemplo, garantir que o serviço solicitou o salvamento da conta.</small>

---

# `never()` — garantir que algo não ocorreu

```java
verify(contaRepository, never()).save(any(Conta.class));
```

- `never()` é um modo de verificação do Mockito.
- O teste falha se `save` tiver sido chamado pelo menos uma vez.
- No cenário de chave Pix duplicada, isso garante que a conta não seja enviada para persistência.

<small class="fragment">Não basta verificar que a exceção ocorreu: também podemos verificar que o serviço não executou um efeito colateral proibido.</small>

---

# `@BeforeEach` — preparar cada teste

```java
@BeforeEach
void setUp() {
    request = new CriarContaRequest(...);
}
```

- `@BeforeEach` vem do JUnit.
- Executa o método antes de cada método de teste.
- É adequado para preparar objetos e valores comuns aos cenários.

<small class="fragment">Cada teste deve continuar independente. Evite compartilhar estado mutável que faça um teste depender da ordem de execução.</small>

---

# `@SpringBootTest` — iniciar o contexto Spring

```java
@SpringBootTest
class ContaServiceIntegrationTest {
}
```

- Vem do **Spring Boot Test**, não do JUnit nem do Mockito.
- Carrega o contexto da aplicação Spring para o teste.
- Permite testar a integração entre beans configurados pela aplicação.

<small class="fragment">É mais pesado que um teste unitário com `@ExtendWith(MockitoExtension.class)`. Use quando precisar validar a configuração e a integração do Spring, não apenas a lógica isolada do serviço.</small>

---

# `@AutoConfigureMockMvc` — testar a camada HTTP

```java
@SpringBootTest
@AutoConfigureMockMvc
class ContaControllerTest {
    @Autowired
    MockMvc mockMvc;
}
```

- Vem do Spring Boot Test.
- Configura o `MockMvc` para enviar requisições simuladas à camada MVC do Spring.
- Permite testar rotas, status HTTP, JSON, validação e comportamento dos controllers sem abrir um servidor HTTP real.

<small class="fragment">Com `@SpringBootTest`, o contexto completo é carregado; `@AutoConfigureMockMvc` acrescenta a infraestrutura para fazer chamadas MVC simuladas.</small>

---

# Qual abordagem escolher?

| Abordagem | O que testa | Contexto Spring |
|---|---|---|
| JUnit + MockitoExtension | Regra de uma classe isolada | Não inicia |
| `@WebMvcTest` | Controller / camada MVC | Parcial, focado na web |
| `@SpringBootTest` | Integração da aplicação | Completo |
| `@SpringBootTest` + `@AutoConfigureMockMvc` | Fluxo HTTP usando contexto completo | Completo + MockMvc |

<small class="fragment">Não existe uma anotação “melhor” em todos os casos: escolha a menor infraestrutura que valide o comportamento desejado.</small>

---

# JaCoCo — cobertura não é qualidade

JaCoCo instrumenta a execução e gera métricas como cobertura de instruções, linhas e ramificações.

- JUnit executa os testes.
- Mockito isola dependências ou verifica interações.
- JaCoCo mede partes do código que foram executadas.

<small class="fragment">Cobertura alta não prova que as asserções são boas nem que todos os comportamentos relevantes foram verificados.</small>

---

# Resumo: leia o teste como uma história

1. **Preparar:** `@Mock`, `@InjectMocks`, `@BeforeEach`.
2. **Definir o cenário:** `when(...).thenReturn(...)`.
3. **Executar:** chamar `contaService.criar(request)`.
4. **Verificar o resultado:** `assertNotNull`, `assertThrows`.
5. **Verificar interações:** `verify`, `never`.
6. **Medir cobertura:** JaCoCo.

<small class="fragment">Um bom teste documenta o comportamento esperado e falha quando esse comportamento é quebrado.</small>

---

# Documentação oficial

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito — documentação e exemplos](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot — Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Spring MockMvc](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html)
- [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/)

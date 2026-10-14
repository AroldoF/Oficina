---
theme: default
title: JUnit, Mockito e JaCoCo
author: Renata, Alessandra, Heitor e Aroldo
lang: pt-BR
transition: slide-left
---

# JUnit, Mockito e JaCoCo

## Testes automatizados em Java

**T01 · Unitário / Integração**

Validação de regras de negócio em um microsserviço Java

Renata · Alessandra · Heitor · Aroldo

15/10

---

# Motivação: e se a regra quebrar sem ninguém perceber?

Um sistema de transferência PIX tem a regra: **o saldo deve cobrir o valor**.

Alguém altera o código e troca `>=` por `>` sem querer.

<div class="grid grid-cols-2 gap-8 pt-4">
<div>

### Sem teste automático

<v-clicks>

- Ninguém percebe na hora
- O erro chega ao **cliente**: quem tem saldo exato não consegue transferir
- Corrigir em produção custa caro
- Medo de mexer no código

</v-clicks>

</div>
<div>

### Com teste automático

<v-clicks>

- O teste **falha em segundos**
- O erro é pego antes do commit
- A regra fica documentada em código
- Mudar o sistema com segurança

</v-clicks>

</div>
</div>

<!--
Esse é o motivo de fazer testes: proteger as regras de negócio contra regressões.
O mesmo projeto PIX será usado na parte prática.
-->

---

# Pirâmide de testes

<div class="flex items-center justify-center h-full">
  <img src="/imgs/piramideTeste.png" width="450" />
</div>

<!--
Base: muitos testes unitários (rápidos, baratos).
Meio: menos testes de integração.
Topo: poucos testes ponta a ponta (lentos, caros).
Hoje focamos na base e no meio: JUnit + Mockito (unitário) e Spring Boot Test (integração).
-->

---

# Teste unitário × teste de integração

<div class="grid grid-cols-2 gap-6">
<div>

### Unitário

Testa **uma unidade** (classe ou método), com as dependências **simuladas**.

*Exemplo PIX:* `TransferenciaService` com o repository em mock. Sem banco e sem HTTP.

</div>
<div>

### Integração

Testa **componentes juntos**: controller, service e banco.

*Exemplo PIX:* `POST /api/v1/pix/transferir` com banco H2 e conferindo o status HTTP.

</div>
</div>

<v-click>

> **Atenção:** usar Mockito não torna um teste de integração. Se há mock, ainda é unitário.

</v-click>

<!--
Os colegas mostram os dois na parte prática:
unitário (TransferenciaServiceTest) e integração (TransferenciaControllerIT).
-->

---

# De onde saem os testes?

**Da regra de negócio.** Primeiro se define o comportamento esperado, depois se escreve o teste.

```mermaid {scale: 0.8}
flowchart LR
    A[Regra de negócio] --> B[Cenários<br/>feliz, erro, limite]
    B --> C[Casos de teste]
    C --> D[JUnit + Mockito]
```

<v-clicks>

- A regra diz **o que** o sistema deve fazer
- Cada regra gera **mais de um cenário**: o caminho feliz, os erros e os limites
- Cada cenário vira **um teste**

</v-clicks>

<!--
Mensagem: não se testa "o código", testa-se o comportamento que a regra exige.
Se a regra não estiver clara, o teste não tem o que comprovar.
-->

---

# Exemplo: uma regra, três testes

> **RN02:** o saldo da conta de origem deve ser maior ou igual ao valor da transferência.

| Cenário | Entrada | Resultado esperado |
|---|---|---|
| Caminho feliz | saldo R$ 100, transfere R$ 40 | Transferência concluída |
| Caso de erro | saldo R$ 30, transfere R$ 50 | `SaldoInsuficienteException` |
| Limite | saldo R$ 50, transfere R$ 50 | Transferência concluída |

<v-click>

O cenário de **limite** é o que mais pega erro (`>` no lugar de `>=`), o mesmo erro da motivação.

</v-click>

<!--
Fechamos o ciclo: o teste de limite teria pego o bug do começo.
Os colegas vão escrever esses testes ao vivo na parte prática.
-->

---

# Três ferramentas, três perguntas

| Ferramenta | Responde | Faz o quê |
|---|---|---|
| **JUnit 5** | *O teste passou?* | Organiza, executa e verifica os testes |
| **Mockito** | *Como a classe usou suas dependências?* | Cria dependências simuladas (mocks) |
| **JaCoCo** | *Que partes do código foram executadas?* | Mede a cobertura |

<br>

<v-click>

Elas **se complementam**, mas não substituem uma à outra.

</v-click>

---

# JUnit 5

**Framework** para escrever e executar testes em Java.

```java
@Test
void deveSomarDoisNumeros() {
    int resultado = 2 + 3;

    assertEquals(5, resultado);
}
```

<v-clicks>

- `@Test` marca o método de teste
- `assertEquals`, `assertTrue`, `assertThrows` comparam esperado × obtido
- O teste **falha** quando o comportamento está errado
- Roda pelo Maven (`./mvnw test`) e pelo IntelliJ

</v-clicks>

---

# Padrão AAA: Arrange, Act, Assert

<div class="grid grid-cols-2 gap-6">
<div>

**Método testado**

```java
boolean temSaldoSuficiente(
    double saldo, double valor) {
  return saldo >= valor;
}
```

<v-clicks>

- **Arrange:** define os dados do cenário
- **Act:** chama **uma** ação
- **Assert:** confere o resultado esperado

</v-clicks>

</div>
<div>

**O teste**

```java
@Test
void deveAceitarQuandoSaldoCobre() {
  // Arrange
  double saldo = 100;
  double valor = 40;

  // Act
  boolean resultado =
      temSaldoSuficiente(saldo, valor);

  // Assert
  assertTrue(resultado);
}
```

</div>
</div>

<!--
Exemplo propositalmente simples, sem banco e sem mock, só para mostrar a estrutura.
O nome do teste descreve o cenário e o resultado esperado.
Um teste, uma ação, uma verificação principal.
Na parte prática os colegas aplicam o mesmo padrão ao TransferenciaService.
-->

---

# Mockito

**Biblioteca** que simula as dependências de uma classe.

```java
ContaRepository repo = mock(ContaRepository.class);

when(repo.findById(1L)).thenReturn(Optional.of(conta));

service.transferir(...);

verify(repo).save(conta);
```

<v-clicks>

- `when(...).thenReturn(...)`: define a resposta do mock
- `verify(...)`: confere se a chamada aconteceu
- Testa a **regra de negócio** sem banco e sem HTTP
- Mock não é, por si só, teste de integração

</v-clicks>

---

# JaCoCo: cobertura de código

JaCoCo instrumenta ou analisa o bytecode Java para calcular métricas como:

- **Cobertura de instruções:** instruções executadas
- **Cobertura de branches:** ramificações de decisões executadas, quando aplicável
- **Linhas e métodos:** métricas complementares

A cobertura ajuda a encontrar áreas pouco exercitadas.

<v-click>

> **Cobertura alta não garante testes bons.** Um teste pode executar uma linha sem verificar o resultado correto.

</v-click>

---

# JaCoCo com Maven

Adicione o plugin ao `pom.xml`:

```xml
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

Execute `./mvnw test`. O relatório HTML fica em:

```text
target/site/jacoco/index.html
```

<!--
Verifique a versão do plugin compatível com a versão de Java do projeto.
A configuração é um exemplo de referência; ajuste conforme o pom.xml existente.
-->

---

# JUnit × pytest

<div class="grid grid-cols-2 gap-4">
<div>

**JUnit 5 (Java)**

```java
@Test
void deveLancarErro() {
    assertEquals(5, soma(2, 3));
    assertThrows(
        IllegalArgumentException.class,
        () -> dividir(1, 0));
}
```

</div>
<div>

**pytest (Python)**

```python
def test_deve_lancar_erro():
    assert soma(2, 3) == 5
    with pytest.raises(ValueError):
        dividir(1, 0)
```

</div>
</div>

| | JUnit 5 | pytest |
|---|---|---|
| Linguagem | Java (tipagem estática) | Python (tipagem dinâmica) |
| Estrutura | Classes e anotações (`@Test`) | Funções `test_*` |
| Asserções | Métodos (`assertEquals`) | `assert` simples |
| Preparação | `@BeforeEach` | *fixtures* |
| Vários casos | `@ParameterizedTest` | `@pytest.mark.parametrize` |
| Mocks | Mockito | `unittest.mock` / pytest-mock |
| Cobertura | JaCoCo | coverage.py / pytest-cov |

<!--
Ideia central: o objetivo é o mesmo (testar), muda o estilo.
JUnit é mais verboso e explícito; pytest é mais enxuto.
-->

---
layout: center
class: text-center
---

# Resumo

Testamos para **proteger as regras de negócio**

<br>

**JUnit** executa e verifica · **Mockito** isola · **JaCoCo** mede

<br>

**Unitário:** valida uma unidade isolada · **Integração:** valida componentes em conjunto

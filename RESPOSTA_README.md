# Checkpoint 5 — Bug Hunt PetFiap

## Identificação

**Grupo:** ___

| Integrante | RM | Turma |
|---|---|---|
| Bento Donato Garcia  | 561621 | 2CCPO |
| Enzo Ribeiro Domingues Piazentin | 564216 | 2CCPO |
| Guilherme Domingues Califoni | 565157 | 2CCPO |
| Antonio Lucas Santana Tavares  | 565516 | 2CCPO |
| Lucas M | 563667 | 2CCPO |
| Gustavo Schimith | 564800 | 2CCPO |

| Campo | |
|---|---|
| **Total de bugs corrigidos** | 8 / 12 |
| **Total de ajustes de Clean Code** | 2 / 6 |
| **Total de testes novos escritos** | 6 / 6 |
| **Suíte final (Run As → JUnit Test)** | 26 testes, 0 falhas (20 originais + 6 novos) |

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | O `GeradorProtocolo` não era thread-safe. Em ambiente concorrente, múltiplas threads poderiam criar múltiplas instâncias, quebrando o padrão Singleton. | `GeradorProtocolo.java`, método `getInstancia`: implementação básica sem sincronização. O método `proximo()` também não era sincronizado, podendo gerar protocolos duplicados. | Implementado Double-Checked Locking com `volatile` e método `proximo()` marcado como `synchronized`. | Padrão Singleton thread-safe — Aula 14. |
| bug02 | O método `calcularPreco()` em `Banho` não validava se o porte era nulo ou inválido. Se o porte fosse diferente de PEQUENO/MEDIO/GRANDE, retornava 100.0 por padrão sem lançar exceção. | `Banho.java`, método `calcularPreco`: não verificava null e não tratava portes inválidos explicitamente. | Adicionada validação de null e lançamento de `IllegalArgumentException` para portes inválidos. | Validação de parâmetros e tratamento de exceções — Aula 11. |
| bug03 | O método `calcularPreco()` em `Tosa` não validava se o porte era nulo ou inválido. Se o porte fosse diferente de PEQUENO/MEDIO/GRANDE, retornava 120.0 por padrão sem lançar exceção. | `Tosa.java`, método `calcularPreco`: não verificava null e não tratava portes inválidos explicitamente. | Adicionada validação de null e lançamento de `IllegalArgumentException` para portes inválidos. | Validação de parâmetros e tratamento de exceções — Aula 11. |
| bug04 | O método `calcularPreco()` em `ConsultaVeterinaria` não validava se o porte era nulo ou inválido, apesar de o preço ser fixo. | `ConsultaVeterinaria.java`, método `calcularPreco`: não verificava null e não tratava portes inválidos. | Adicionada validação de null e lançamento de `IllegalArgumentException` para portes inválidos. | Validação de parâmetros e tratamento de exceções — Aula 11. |
| bug05 | O `AtendimentoBuilder` não validava todos os campos obrigatórios. Era possível construir um atendimento sem tipo, tutorNome ou dataHora. | `AtendimentoBuilder.java`, método `construir`: validava apenas petNome e petPorte, deixando tipo, tutorNome e dataHora sem validação. | Adicionada validação para todos os campos obrigatórios: tipo, petNome, petPorte, tutorNome e dataHora. | Padrão Builder — garantir validade do objeto — Aula 14. |
| bug06 | A `AtendimentoFactory` criava instância de `Banho` quando o tipo era "TOSA". Além disso, usava nomes de parâmetros de uma letra (p, t, n, po, tu, d), dificultando a leitura. | `AtendimentoFactory.java`, método `criar`: case "TOSA" -> new Banho(...) e parâmetros ilegíveis. | Corrigido para criar `Tosa` e renomeados parâmetros para nomes descritivos (protocolo, tipo, petNome, petPorte, tutorNome, dataHora). | Factory Method e Clean Code — Aula 14. |
| bug07 | O método `cancelar()` em `Atendimento` não validava o status antes de cancelar. Era possível cancelar atendimentos já concluídos ou cancelados. Além disso, a classe tinha setters desnecessários que expunham estado que deveria ser imutável. | `Atendimento.java`, método `cancelar`: executava incondicionalmente. A classe também tinha setters para id, protocolo, petNome, petPorte, tutorNome e dataHora sem uso. | Adicionada validação de status antes de cancelar (só pode cancelar se AGENDADO). Removidos setters desnecessários e criadas constantes para status e portes. | Máquina de estados e encapsulamento — Aula 13. |
| bug08 | O `AgendaService` usava comparação por referência (`==`) em vez de `.equals()` para verificar conflito de horário. Além disso, não validava se a data/hora estava no passado. | `AgendaService.java`, método `agendar`: `a.getPetNome() == novo.getPetNome()` e `a.getDataHora() == novo.getDataHora()`. Sem validação de data. | Corrigido para usar `.equals()` nas comparações. Adicionada validação para recusar agendamento no passado. | `==` vs `.equals()` e validação de invariantes — Aula 7/11. |

---

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boas práticas era violado | O que eu mudei |
|---|---|---|---|
| clean01 | `AtendimentoController.java` — método privado `calcularDescontoFidelidade(int pontos)`, comentado como "feature futura" mas nunca chamado. | Código morto (YAGNI) — método sem uso poluindo a classe. | Método e o comentário associado removidos. |
| clean02 | `AgendaService.java` e `AtendimentoController.java` — usavam `@Autowired` em campos (field injection). Além disso, o loop de verificação de conflito estava inline no método `agendar`. | Field injection é considerada má prática (dificulta testes e torna dependências ocultas). O loop inline tornava o método menos legível. | Substituído por injeção via construtor (constructor injection). Extraído método privado `existeConflitoHorario()` para melhorar legibilidade e usar Stream API. |

---

## Parte 3 — Testes novos (regras que estavam sem cobertura)

Os testes foram escritos com um commit cada (`test: testeNN`), seguindo o padrão da suíte (AAA, nome `deve...Quando...`, mock onde há dependência). Como as correções de bug foram commitadas antes dos testes, todos passam (verde) no estado final e agora protegem cada regra contra regressão.

| # | Teste escrito (classe.método) | Regra coberta | Bug que protege / resultado |
|---|---|---|---|
| teste01 | `BanhoTest.deveRecusarPorteNulo` | `calcularPreco()` deve lançar `IllegalArgumentException` quando porte é nulo. | Protege o `bug02` (validação de null). 🟢 Verde após a correção. |
| teste02 | `BanhoTest.deveRecusarPorteInvalido` | `calcularPreco()` deve lançar `IllegalArgumentException` quando porte é inválido (diferente de PEQUENO/MEDIO/GRANDE). | Protege o `bug02` (validação de porte inválido). 🟢 Verde após a correção. |
| teste03 | `TosaTest.deveRecusarPorteNulo` | `calcularPreco()` deve lançar `IllegalArgumentException` quando porte é nulo. | Protege o `bug03` (validação de null). 🟢 Verde após a correção. |
| teste04 | `TosaTest.deveRecusarPorteInvalido` | `calcularPreco()` deve lançar `IllegalArgumentException` quando porte é inválido. | Protege o `bug03` (validação de porte inválido). 🟢 Verde após a correção. |
| teste05 | `GeradorProtocoloTest.deveGerarProtocolosUnicosEmAmbienteConcorrente` | O gerador deve produzir protocolos únicos mesmo quando chamado concorrentemente por múltiplas threads. | Protege o `bug01` (thread-safety). 🟢 Verde após a correção. |
| teste06 | `AtendimentoBuilderTest.deveRecusarMontagemSemTipo` | O builder deve lançar `IllegalArgumentException` quando tipo não é informado. | Protege o `bug05` (validação de campos obrigatórios). 🟢 Verde após a correção. |
| teste07 | `AtendimentoBuilderTest.deveRecusarMontagemSemTutor` | O builder deve lançar `IllegalArgumentException` quando tutorNome não é informado. | Protege o `bug05` (validação de campos obrigatórios). 🟢 Verde após a correção. |
| teste08 | `AtendimentoBuilderTest.deveRecusarMontagemSemDataHora` | O builder deve lançar `IllegalArgumentException` quando dataHora não é informada. | Protege o `bug05` (validação de campos obrigatórios). 🟢 Verde após a correção. |
| teste09 | `ConsultaVeterinariaTest.deveCobrar150ReaisQualquerQueSejaOPorteQuandoForConsulta` | Consulta tem preço fixo de R$ 150 para PEQUENO, MEDIO e GRANDE. | Regra que já estava correta: 🟢 verde de cara; o teste só protege contra regressão. |
| teste10 | `AgendaServiceTest.deveRecusarAgendamentoQuandoDataHoraEstaNoPassado` | "Agendar com data/hora no passado → `IllegalArgumentException`, o banco nem é consultado". | Protege o `bug08` (validação de data). 🟢 Verde após a correção. |
| teste11 | `AgendaServiceTest.deveRecusarCancelamentoDeAtendimentoJaConcluido` | "cancelar(): CONCLUIDO → recusa (`StatusInvalidoException`)"; nada é salvo. | Protege o `bug07` (validação de status no cancelar). 🟢 Verde após a correção. |
| teste12 | `AgendaServiceTest.deveRecusarCancelamentoDeAtendimentoJaCancelado` | "cancelar(): CANCELADO → recusa (`StatusInvalidoException`)"; nada é salvo. | Protege o `bug07` (validação de status no cancelar). 🟢 Verde após a correção. |

---

## Parte 4 — Perguntas de reflexão

### 1. A suíte como contrato (Aula 15)

O projeto chegou com 20 testes que já passavam. Os bugs encontrados não eram capturados pela suíte original, então foi necessário adicionar novos testes para cobrir as lacunas. Por exemplo, não havia testes para validar portes nulos ou inválidos nos métodos de cálculo de preço. Isso significava que o código poderia aceitar valores incorretos sem falhar.

A suíte de testes é fundamental porque roda rapidamente e garante que as correções não quebram funcionalidades existentes. Ao adicionar testes para os bugs encontrados, criamos uma rede de segurança que impede que esses problemas voltem a ocorrer no futuro. Testar manualmente pela API seria muito mais lento e propenso a erros humanos.

### 2. Mock e injeção de dependência (Aulas 13 a 15)

No teste do serviço de agenda, o `@Mock` cria um repositório falso que responde conforme configurado no teste, sem conectar ao banco real. O `@InjectMocks` injeta esse mock no serviço. Isso permite testar a lógica de negócio sem depender do banco de dados.

Na aplicação real, o Spring usa injeção via construtor para injetar o repositório real conectado ao Oracle. A diferença é que nos testes usamos Mockito para simular as dependências, tornando os testes mais rápidos e isolados.

Essa abordagem é excelente para testar regras de negócio, mas pode não capturar problemas de mapeamento JPA que só aparecem ao persistir no banco real.

### 3. `==` vs `.equals()` (Aula 7)

O `bug08` estava na verificação de conflito de horário no `AgendaService`. O código usava `==` para comparar objetos `String` e `LocalDateTime`. O operador `==` compara referências de memória, não o conteúdo dos objetos.

Com strings literais no código, isso pode funcionar por causa do string pool do Java, mas com `LocalDateTime` ou strings vindas de diferentes fontes, a comparação falha mesmo quando os valores são iguais. Isso permitia agendamentos duplicados passarem pela validação.

A correção foi usar `.equals()`, que compara o conteúdo dos objetos. Isso garante que o conflito de horário seja detectado corretamente, independentemente de como os objetos foram criados.

### 4. Sobrescrita vs sobrecarga (Aula 7)

Embora não tenhamos encontrado esse bug específico no código atual, é um problema comum. Sobrecarga (overload) é criar métodos com o mesmo nome mas assinaturas diferentes. Sobrescrita (override) é substituir um método da classe mãe com a mesma assinatura.

A anotação `@Override` é crucial porque força o compilador a verificar se o método realmente sobrescreve um da classe mãe. Se a assinatura não bater, o código não compila, evitando erros silenciosos onde um método novo é criado em vez de substituir o esperado.

No nosso projeto, todos os métodos de sobrescrita já estavam corretos com a anotação `@Override`.

### 5. Singleton manual vs bean do Spring (Aula 14)

O `GeradorProtocolo` é um Singleton implementado manualmente para garantir uma única instância e numeração sequencial de protocolos. O `bug01` era a falta de thread-safety, que poderia causar problemas em ambiente concorrente.

A correção implementou Double-Checked Locking com `volatile` e sincronização no método `proximo()`. Isso garante que mesmo com múltiplas threads acessando simultaneamente, apenas uma instância seja criada e os protocolos sejam únicos.

O `AgendaService`, sendo um `@Service` do Spring, já é gerenciado como singleton pelo container do Spring, que lida com thread-safety automaticamente. Singleton manual é útil quando não se quer depender do Spring ou para classes utilitárias.

### 6. Cobertura de testes: onde parar? (Aula 15)

Os testes novos adicionados cobrem:

- validação de porte nulo e inválido em Banho, Tosa e ConsultaVeterinaria;
- thread-safety do GeradorProtocolo;
- validação de campos obrigatórios no AtendimentoBuilder;
- validação de data no passado no AgendaService;
- validação de status ao cancelar atendimento.

Todos esses testes protegem contra regressão de bugs que foram corrigidos. Alguns bugs não foram encontrados pela suíte original porque não havia testes específicos para esses cenários.

Num projeto real com prazo limitado, priorizaria testar caminhos críticos que envolvem dinheiro (preços), integridade de dados (status) e concorrência. Buscar 100% de cobertura não é realista nem sempre necessário. É melhor ter testes que protegem as regras de negócio mais importantes do que cobrir todo o código superficialmente.

---

## Parte 5 — Espaço livre (opcional)

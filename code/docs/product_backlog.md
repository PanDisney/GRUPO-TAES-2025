# Product Backlog - Projeto Bisca

## Épico 1: Autenticação e Gestão de Conta

### PB-001: Sistema de Autenticação
**Descrição:** Implementar um sistema de autenticação seguro que permita aos utilizadores registados fazer login com as suas credenciais (email e senha).

**Critérios de Aceitação:**
- Os utilizadores podem fazer login com email e senha válidos
- A senha é armazenada de forma segura (com hash)
- Mensagens de erro claras para credenciais inválidas
- Redirecionamento para o dashboard após login bem-sucedido

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** Sistema de base de dados

---

### PB-002: Modo Anónimo
**Descrição:** Permitir que novos utilizadores acedam à aplicação sem criar uma conta, com acesso limitado a jogos de prática.

**Critérios de Aceitação:**
- Utilizadores podem ignorar o login
- Acesso limitado apenas a jogos de prática
- Sem acesso a moedas, histórico, personalizações ou classificações
- Função "Jogar como Anónimo" disponível no ecrã de login

**Prioridade:** Alta
**Estimativa:** 5 pontos
**Dependências:** PB-001

---

### PB-003: Recuperação de Senha
**Descrição:** Implementar funcionalidade para que utilizadores registados possam recuperar o acesso à sua conta caso esqueçam a senha.

**Critérios de Aceitação:**
- Link "Esqueci a Senha" no ecrã de login
- Email de recuperação com link seguro
- Formulário para redefinir a senha
- Confirmação após redefinição bem-sucedida

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-001, Sistema de email

---

### PB-004: Sincronização de Dados com CSS
**Descrição:** Sincronizar todos os dados do utilizador com o Centralized Support System (CSS) para persistência e acesso multiplataforma.

**Critérios de Aceitação:**
- Dados do utilizador sincronizados após cada ação
- Consistência entre dispositivos
- Autenticação segura com o CSS
- Tratamento de erros de sincronização

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** PB-001, Backend CSS

---

## Épico 2: Interface e Navegação Principal

### PB-005: Dashboard Principal
**Descrição:** Criar um painel de controlo principal que forneça acesso a todas as funcionalidades principais da aplicação.

**Critérios de Aceitação:**
- Exibição do avatar, nome de utilizador e saldo de moedas (utilizadores registados)
- Botões para: Iniciar Jogo, Ver Histórico, Ver Classificações, Personalizações
- Layout responsivo e intuitivo
- Navegação clara entre funcionalidades

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** PB-001 ou PB-002

---

### PB-006: Menu de Navegação
**Descrição:** Implementar um menu de navegação consistente para acesso rápido a diferentes secções da aplicação.

**Critérios de Aceitação:**
- Menu acessível em todas as páginas
- Ícones claros e descritivos
- Destaque da página atual
- Funcionalidade de logout

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-005

---

### PB-007: Notificações do Sistema
**Descrição:** Implementar um sistema de notificações para informar utilizadores sobre eventos importantes.

**Critérios de Aceitação:**
- Notificações para novo líder nas classificações
- Notificações para novas personalizações disponíveis
- Toque na notificação abre o ecrã relevante
- Histórico de notificações

**Prioridade:** Média
**Estimativa:** 6 pontos
**Dependências:** PB-005, CSS backend

---

## Épico 3: Lógica e Gameplay do Jogo Bisca

### PB-008: Iniciar Nova Partida
**Descrição:** Permitir que os utilizadores iniciem uma nova partida contra o bot.

**Critérios de Aceitação:**
- Botão "Iniciar Partida" no dashboard
- Seleção de tipo de jogo (prática ou partida com entrada)
- Distribuição de 9 cartas para o utilizador
- Cartas do bot ocultas
- Exibição do naipe de trunfo

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** PB-005, PB-009, PB-010

---

### PB-009: Lógica do Baralho e Distribuição de Cartas
**Descrição:** Implementar a lógica do baralho de 40 cartas (Bisca padrão) e a distribuição correta de cartas.

**Critérios de Aceitação:**
- Baralho de 40 cartas (sem 8, 9, 10)
- Distribuição inicial de 9 cartas
- Embaralhamento aleatório
- Seleção aleatória do naipe de trunfo
- Repescagem de cartas após cada rodada

**Prioridade:** Alta
**Estimativa:** 5 pontos
**Dependências:** Nenhuma

---

### PB-010: Gameplay - Jogar Cartas e Rodadas
**Descrição:** Implementar a mecânica principal de jogo: o utilizador e o bot jogam cartas alternadamente.

**Critérios de Aceitação:**
- Seleção de cartas pelo utilizador (clique/toque)
- Jogada automática do bot após o utilizador
- Exibição clara das cartas jogadas
- Determinação do vencedor da rodada
- Recolha das cartas pela rodada vencedora

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** PB-008, PB-009

---

### PB-011: Inteligência Artificial do Bot
**Descrição:** Implementar lógica de jogo para o bot simular um jogador realista.

**Critérios de Aceitação:**
- Bot segue regras de Bisca corretamente
- Bot toma decisões estratégicas
- Bot adapta a estratégia conforme o jogo progride
- Performance razoável do bot sem ser trivial

**Prioridade:** Alta
**Estimativa:** 13 pontos
**Dependências:** PB-009, PB-010

---

### PB-012: Cálculo de Pontos e Fim de Jogo
**Descrição:** Calcular corretamente os pontos após todas as cartas serem jogadas e determinar o vencedor do jogo.

**Critérios de Aceitação:**
- Cálculo automático de pontos (Ás=11, Sete=10, Rei=4, Valete=3, Dama=2)
- Identificação correta do vencedor (61+ pontos)
- Exibição clara do resultado
- Determinação de marcas (risca, capote, bandeira)

**Prioridade:** Alta
**Estimativa:** 5 pontos
**Dependências:** PB-010

---

### PB-013: Progressão de Partida (4 Jogos)
**Descrição:** Implementar o sistema de melhor de 4 jogos para determinar o vencedor da partida.

**Critérios de Aceitação:**
- Primeiro a ganhar 4 jogos vence a partida
- Marcas atribuídas conforme regras (1 risca, 2 capote, 3 bandeira)
- Exibição do progresso atual
- Automaticamente iniciar novo jogo se necessário

**Prioridade:** Alta
**Estimativa:** 5 pontos
**Dependências:** PB-012

---

### PB-014: Ecrã de Resultados da Partida
**Descrição:** Exibir resultados detalhados após a conclusão de uma partida.

**Critérios de Aceitação:**
- Resultado geral da partida (vitória/derrota)
- Detalhes de cada jogo jogado
- Conquistas alcançadas (Capote/Bandeira)
- Moedas ganhas/perdidas
- Opção para voltar ao dashboard ou jogar novamente

**Prioridade:** Alta
**Estimativa:** 6 pontos
**Dependências:** PB-013

---

## Épico 4: Sistema de Moedas e Transações

### PB-015: Gestão de Saldo de Moedas
**Descrição:** Manter e atualizar o saldo de moedas dos utilizadores registados.

**Critérios de Aceitação:**
- Exibição do saldo atual no dashboard
- Atualização em tempo real após transações
- Sincronização com CSS
- Histórico de transações

**Prioridade:** Alta
**Estimativa:** 6 pontos
**Dependências:** PB-004, PB-005

---

### PB-016: Entrada de Partida
**Descrição:** Implementar o sistema de pagamento da entrada de partida com moedas.

**Critérios de Aceitação:**
- Entrada de partida tem custo em moedas
- Validação de saldo suficiente antes de iniciar
- Deducção de moedas ao iniciar partida
- Mensagem de erro clara se saldo insuficiente

**Prioridade:** Alta
**Estimativa:** 5 pontos
**Dependências:** PB-015, PB-008

---

### PB-017: Recompensas de Moedas por Vitória
**Descrição:** Atribuir moedas ao utilizador por ganhar partidas.

**Critérios de Aceitação:**
- Moedas base por vitória
- Moedas bónus por conquistas (Capote/Bandeira)
- Cálculo automático de recompensas
- Adição ao saldo após conclusão da partida

**Prioridade:** Alta
**Estimativa:** 5 pontos
**Dependências:** PB-014, PB-015

---

### PB-018: Transações de Personalizações
**Descrição:** Permitir que utilizadores comprem personalizações com moedas.

**Critérios de Aceitação:**
- Seleção de personalização para compra
- Validação de saldo suficiente
- Deducção de moedas ao comprar
- Adição da personalização à coleção
- Mensagem de confirmação

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-015, PB-020

---

## Épico 5: Histórico e Classificações

### PB-019: Visualização do Histórico de Partidas
**Descrição:** Permitir que utilizadores registados vejam o histórico de todas as suas partidas anteriores.

**Critérios de Aceitação:**
- Lista de todas as partidas jogadas
- Informações: data, hora, duração, resultado, moedas ganhas
- Detalhamento rodada por rodada
- Ordenação por data (recente primeiro)

**Prioridade:** Média
**Estimativa:** 8 pontos
**Dependências:** PB-004, PB-005

---

### PB-020: Filtro e Busca no Histórico
**Descrição:** Permitir que utilizadores filtrem e procurem no seu histórico de partidas.

**Critérios de Aceitação:**
- Filtro por data (intervalo)
- Filtro por resultado (vitória/derrota)
- Filtro por conquistas (Capote/Bandeira)
- Busca por nome de oponente (bot)
- Atualização dinâmica da lista

**Prioridade:** Média
**Estimativa:** 6 pontos
**Dependências:** PB-019

---

### PB-021: Classificações Pessoais
**Descrição:** Exibir estatísticas pessoais e recordes do utilizador.

**Critérios de Aceitação:**
- Número total de partidas jogadas
- Número de vitórias
- Taxa de vitória (percentagem)
- Número de conquistas (Capote/Bandeira)
- Total de moedas ganhas

**Prioridade:** Média
**Estimativa:** 6 pontos
**Dependências:** PB-004, PB-005

---

### PB-022: Classificações Globais
**Descrição:** Exibir um ranking global com os melhores jogadores.

**Critérios de Aceitação:**
- Ranking por número de vitórias
- Ranking por moedas ganhas
- Ranking por conquistas
- Posição do utilizador atual
- Dados sincronizados com CSS

**Prioridade:** Média
**Estimativa:** 8 pontos
**Dependências:** PB-004, PB-021

---

## Épico 6: Personalizações

### PB-023: Catálogo de Personalizações
**Descrição:** Criar um catálogo de avatares e baralhos personalizados disponíveis para compra.

**Critérios de Aceitação:**
- Exibição de todos os avatares disponíveis
- Exibição de todos os baralhos disponíveis
- Indicação de items já possuídos
- Indicação de price em moedas
- Visualização prévia de cada item

**Prioridade:** Média
**Estimativa:** 6 pontos
**Dependências:** Nenhuma

---

### PB-024: Compra de Avatares
**Descrição:** Permitir que utilizadores comprem e selecionem avatares personalizados.

**Critérios de Aceitação:**
- Seleção de avatar para compra
- Validação de saldo
- Deducção de moedas
- Avatar adicionado à coleção
- Possibilidade de aplicar avatar

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-023, PB-015

---

### PB-025: Compra de Baralhos
**Descrição:** Permitir que utilizadores comprem e selecionem baralhos personalizados.

**Critérios de Aceitação:**
- Seleção de baralho para compra
- Validação de saldo
- Deducção de moedas
- Baralho adicionado à coleção
- Possibilidade de aplicar baralho

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-023, PB-015

---

### PB-026: Aplicação e Persistência de Avatares
**Descrição:** Permitir que utilizadores selecionem e apliquem um avatar personalizado.

**Critérios de Aceitação:**
- Seleção de avatar na tela de personalizações
- Avatar aparece no dashboard
- Avatar aparece durante o jogo
- Seleção persiste entre sessões
- Sincronização com CSS

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-024, PB-004

---

### PB-027: Aplicação e Persistência de Baralhos
**Descrição:** Permitir que utilizadores selecionem e apliquem um baralho personalizado.

**Critérios de Aceitação:**
- Seleção de baralho na tela de personalizações
- Baralho aplicado nas cartas durante partidas
- Seleção persiste entre sessões
- Sincronização com CSS

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-025, PB-004

---

## Épico 7: Interface de Utilizador e UX

### PB-028: Design Responsivo
**Descrição:** Garantir que a aplicação é funcional e visualmente agradável em diferentes tamanhos de ecrã.

**Critérios de Aceitação:**
- Layout funcional em smartphones (vertical e horizontal)
- Layout funcional em tablets
- Elementos clicáveis/tocáveis com tamanho apropriado
- Fonte legível

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** Todos os outros PBs de interface

---

### PB-029: Animações e Transições
**Descrição:** Melhorar a experiência visual com animações e transições suaves.

**Critérios de Aceitação:**
- Transições entre ecrãs suaves
- Animações de cartas sendo jogadas
- Animações de resultado de rodada
- Sem animações que distraiam ou atrasem

**Prioridade:** Baixa
**Estimativa:** 5 pontos
**Dependências:** PB-010

---

### PB-030: Acessibilidade
**Descrição:** Garantir que a aplicação é acessível para utilizadores com diferentes necessidades.

**Critérios de Aceitação:**
- Contraste suficiente entre texto e fundo
- Texto alternativo para imagens
- Navegação por teclado funcionando
- Suporte para leitores de ecrã

**Prioridade:** Média
**Estimativa:** 8 pontos
**Dependências:** Todos os outros PBs de interface

---

## Épico 8: Testes e Qualidade

### PB-031: Testes Unitários
**Descrição:** Implementar testes unitários para a lógica do jogo e funcionalidades críticas.

**Critérios de Aceitação:**
- Testes para cálculo de pontos
- Testes para determinação de vencedor
- Testes para lógica do baralho
- Cobertura mínima de 80%
- Todos os testes a passar

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** PB-009, PB-012

---

### PB-032: Testes de Integração
**Descrição:** Implementar testes de integração para funcionalidades que dependem de múltiplos componentes.

**Critérios de Aceitação:**
- Testes para fluxo de autenticação
- Testes para fluxo de jogo completo
- Testes para sincronização com CSS
- Testes para gestão de moedas

**Prioridade:** Alta
**Estimativa:** 8 pontos
**Dependências:** PB-031

---

### PB-033: Testes de Interface
**Descrição:** Implementar testes automatizados para a interface de utilizador.

**Critérios de Aceitação:**
- Testes para elementos interativos (botões, inputs)
- Testes para navegação
- Testes para validação de formulários
- Testes para responsividade

**Prioridade:** Média
**Estimativa:** 8 pontos
**Dependências:** PB-028

---

## Épico 9: Documentação e Processos

### PB-034: Documentação do Código
**Descrição:** Documentar o código fonte da aplicação para facilitar manutenção futura.

**Critérios de Aceitação:**
- Comentários em código crítico
- README com instruções de setup
- Documentação de APIs
- Guia de arquitetura

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** Nenhuma

---

### PB-035: Documentação de Testes
**Descrição:** Documentar a estratégia de testes e os testes implementados.

**Critérios de Aceitação:**
- Plano de testes
- Documentação dos casos de teste
- Relatório de cobertura
- Instruções para executar testes

**Prioridade:** Média
**Estimativa:** 5 pontos
**Dependências:** PB-031, PB-032, PB-033

---

### PB-036: Setup e Deployment
**Descrição:** Configurar o ambiente de desenvolvimento e processo de deployment.

**Critérios de Aceitação:**
- Ambiente de desenvolvimento funcionando
- Scripts de build automatizados
- Processo de deployment documentado
- CI/CD pipeline configurado (opcional)

**Prioridade:** Média
**Estimativa:** 8 pontos
**Dependências:** Nenhuma


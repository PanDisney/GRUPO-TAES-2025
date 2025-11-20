# User Stories(24) - Projeto Bisca

## US-01: Autenticação de Utilizador Registado
**Como** um utilizador registado  
**Quero** fazer login com as minhas credenciais  
**Para** aceder a todas as funcionalidades da aplicação (moedas, histórico, classificações e personalizações)

### Critérios de Aceitação
- Dado que estou no ecrã de login
- Quando introduzo o meu email e senha corretos
- Então devo ser redirecionado para o painel de controlo (dashboard)
- E devo ver o meu avatar, nome de utilizador e saldo de moedas

---

## US-02: Modo Anónimo ✅
**Como** um novo utilizador  
**Quero** poder ignorar o login e jogar em modo anónimo  
**Para** testar a aplicação sem criar uma conta

### Critérios de Aceitação
- Dado que estou no ecrã de login
- Quando clico em "Continuar como Anónimo"
- Então posso aceder apenas a jogos de prática
- E não posso jogar partidas, ganhar moedas, usar personalizações ou aceder a classificações e histórico ❌

---

## US-03: Visualização do Painel de Controlo ✅
**Como** um utilizador (registado ou anónimo)  
**Quero** ver um painel com acesso a todas as funcionalidades principais  
**Para** navegar facilmente pela aplicação

### Critérios de Aceitação
- Dado que fiz login ou entrei como anónimo
- Quando acesso o painel de controlo
- Então devo ver botões para: iniciar novo jogo, (ver histórico, ver classificações e aceder a personalizações) ❌

---

## US-04: Iniciar Nova Partida ✅
**Como** um utilizador  
**Quero** iniciar uma nova partida contra o bot  
**Para** jogar Bisca

### Critérios de Aceitação
- Dado que estou no painel de controlo
- Quando clico em "Iniciar Nova Partida"
- Então o jogo inicia e sou apresentado com 9 cartas
- E o bot apresenta as suas cartas (ocultas)

---

## US-05: Verificação de Saldo de Moedas
**Como** um utilizador registado  
**Quero** verificar o meu saldo de moedas antes de jogar  
**Para** garantir que tenho moedas suficientes para a entrada da partida

### Critérios de Aceitação
- Dado que vejo uma partida que requer uma entrada em moedas
- Quando verifico o meu saldo
- Então vejo o saldo atual no painel de controlo
- E se o saldo for insuficiente, a ação fica desativada com um aviso claro

---

## US-06: Compra de Entrada de Partida
**Como** um utilizador registado  
**Quero** pagar a entrada de uma partida com as minhas moedas  
**Para** jogar uma partida de verdade (não prática)

### Critérios de Aceitação
- Dado que tenho moedas suficientes
- Quando clico em "Jogar Partida"
- Então as moedas são deduzidas do meu saldo
- E a partida inicia

---

## US-07: Jogabilidade Básica - Jogar Carta ✅
**Como** um utilizador  
**Quero** jogar uma carta da minha mão durante o jogo  
**Para** avançar no jogo de Bisca

### Critérios de Aceitação
- Dado que estou numa rodada durante o jogo
- Quando clico numa carta da minha mão
- Então a carta é jogada
- E o bot joga a sua carta automaticamente

---

## US-08: Determinação do Vencedor da Rodada ✅
**Como** um utilizador  
**Quero** ver qual carta venceu a rodada  
**Para** compreender o jogo

### Critérios de Aceitação
- Dado que ambas as cartas foram jogadas
- Quando as cartas são comparadas
- Então a carta vencedora é destacada
- E o vencedor (eu ou bot) recebe as cartas da rodada
- E as cartas são desenadas do baralho automaticamente

---

## US-09: Cálculo de Pontos ✅
**Como** um utilizador  
**Quero** ver os pontos calculados corretamente após cada jogo  
**Para** saber como estou a ganhar

### Critérios de Aceitação
- Dado que todas as cartas foram jogadas
- Quando o jogo termina
- Então os pontos são calculados automaticamente
- E vejo o resultado (número de pontos para mim e para o bot)

---

## US-10: Progressão de Partida (4 Jogos)
**Como** um utilizador  
**Quero** jogar múltiplos jogos até alguém chegar a 4 jogos ganhos  
**Para** completar uma partida de Bisca

### Critérios de Aceitação
- Dado que terminei um jogo
- Quando vejo o resultado
- Então se ninguém alcançou 4 jogos, um novo jogo inicia
- E o primeiro a chegar a 4 jogos ganha a partida

---

## US-11: Ecrã de Resultados da Partida
**Como** um utilizador  
**Quero** ver os resultados detalhados após a partida  
**Para** compreender o desempenho e as recompensas

### Critérios de Aceitação
- Dado que a partida terminou
- Quando o ecrã de resultados é mostrado
- Então vejo: resultado da partida, resultado de cada jogo, conquistas (Capote/Bandeira), moedas ganhas

---

## US-12: Conquista Capote
**Como** um utilizador  
**Quero** ganhar a conquista Capote (2 marcas)  
**Para** obter pontos bónus e reconhecimento

### Critérios de Aceitação
- Dado que joguei um jogo
- Quando ganho entre 91 e 119 pontos
- Então recebo 2 marcas (Capote)
- E recebo moedas bónus

---

## US-13: Conquista Bandeira
**Como** um utilizador  
**Quero** ganhar a conquista Bandeira (3 marcas)  
**Para** obter o máximo de pontos bónus

### Critérios de Aceitação
- Dado que joguei um jogo
- Quando ganho todos os 120 pontos
- Então recebo 3 marcas (Bandeira)
- E recebo moedas bónus significativas

---

## US-14: Notificações do Sistema
**Como** um utilizador registado  
**Quero** receber notificações sobre eventos importantes  
**Para** estar informado sobre classificações e novas personalizações

### Critérios de Aceitação
- Dado que estou a usar a aplicação
- Quando há um novo líder nas classificações ou novas personalizações disponíveis
- Então recebo uma notificação do sistema
- E ao tocar na notificação, sou levado para o ecrã relevante

---

## US-15: Visualização do Histórico de Partidas
**Como** um utilizador registado  
**Quero** ver o histórico de todas as minhas partidas  
**Para** rastrear o meu desempenho ao longo do tempo

### Critérios de Aceitação
- Dado que estou no painel de controlo
- Quando clico em "Histórico"
- Então vejo uma lista de todas as minhas partidas com: data, duração, resultado, moedas ganhas
- E posso ver o detalhamento rodada a rodada

---

## US-16: Filtro e Ordenação do Histórico
**Como** um utilizador registado  
**Quero** filtrar e ordenar o meu histórico  
**Para** encontrar facilmente partidas específicas

### Critérios de Aceitação
- Dado que estou a visualizar o histórico
- Quando aplico filtros (data, resultado, conquistas)
- Então a lista é atualizada com os resultados filtrados
- E posso ordenar por data (mais recente ou mais antigo)

---

## US-17: Visualização das Classificações Pessoais
**Como** um utilizador registado  
**Quero** ver as minhas classificações pessoais  
**Para** compreender o meu desempenho

### Critérios de Aceitação
- Dado que estou no ecrã de classificações
- Quando seleciono "Minhas Classificações"
- Então vejo: número de partidas, número de vitórias, taxa de vitória, resultados Capote/Bandeira

---

## US-18: Visualização das Classificações Globais
**Como** um utilizador registado  
**Quero** ver as classificações globais  
**Para** comparar o meu desempenho com outros utilizadores

### Critérios de Aceitação
- Dado que estou no ecrã de classificações
- Quando seleciono "Classificações Globais"
- Então vejo um ranking com: utilizadores com mais vitórias, mais moedas ganhas, mais conquistas

---

## US-19: Visualização de Personalizações Disponíveis
**Como** um utilizador registado  
**Quero** ver as personalizações disponíveis (avatares e baralhos)  
**Para** personalizar a minha experiência de jogo

### Critérios de Aceitação
- Dado que estou no ecrã de personalizações
- Quando acedo à lista
- Então vejo todos os avatares e baralhos disponíveis
- E vejo quais já possuo e quais estão disponíveis para compra

---

## US-20: Compra de Personalizações
**Como** um utilizador registado  
**Quero** comprar avatares e baralhos com moedas  
**Para** personalizar a minha experiência de jogo

### Critérios de Aceitação
- Dado que vejo uma personalização disponível
- Quando clico em "Comprar"
- Então as moedas são deduzidas
- E a personalização é adicionada à minha coleção

---

## US-21: Seleção e Aplicação de Avatar
**Como** um utilizador registado  
**Quero** selecionar e aplicar um avatar personalizado  
**Para** mostrar a minha identidade na aplicação

### Critérios de Aceitação
- Dado que possuo um avatar personalizado
- Quando seleciono no ecrã de personalizações
- Então o avatar é aplicado
- E aparece no painel de controlo e durante as partidas
- E a seleção persiste entre sessões

---

## US-22: Seleção e Aplicação de Baralho Personalizado
**Como** um utilizador registado  
**Quero** selecionar um baralho personalizado  
**Para** ter uma experiência visual única durante as partidas

### Critérios de Aceitação
- Dado que possuo um baralho personalizado
- Quando seleciono no ecrã de personalizações
- Então o baralho é aplicado às cartas durante as partidas
- E a seleção persiste entre sessões

---

## US-23: Sincronização de Dados com Backend
**Como** um utilizador registado  
**Quero** que os meus dados sejam sincronizados com o servidor  
**Para** manter as minhas informações seguras e acessíveis

### Critérios de Aceitação
- Dado que realizo ações na aplicação (ganhar moedas, comprar personalizações, completar partidas)
- Quando finizo a ação
- Então os dados são sincronizados com o backend
- E se voltar a fazer login noutro dispositivo, vejo os dados atualizados

---

## US-24: Recuperação de Senha
**Como** um utilizador registado que esqueceu a senha  
**Quero** recuperar o acesso à minha conta  
**Para** voltar a usar a aplicação

### Critérios de Aceitação
- Dado que estou no ecrã de login
- Quando clico em "Esqueci a Senha"
- Então sou redirecionado para um formulário de recuperação
- E recebo um email com um link para redefinir a senha


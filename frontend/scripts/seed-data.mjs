// Dados de demonstração do VAPOR.
// Títulos e estúdios são reais, mas preços e datas são aproximados e as descrições são resumos próprios:
// servem só para a apresentação, não são uma fonte de informação sobre os jogos.

export const categorias = [
  { nome: 'Ação', descricao: 'Reflexos, combate e ritmo acelerado' },
  { nome: 'Aventura', descricao: 'Exploração e histórias para viver no seu ritmo' },
  { nome: 'RPG', descricao: 'Jogos de interpretação de papéis' },
  { nome: 'Indie', descricao: 'Produções de estúdios independentes' },
  { nome: 'Plataforma', descricao: 'Saltos precisos e fases desafiadoras' },
  { nome: 'Estratégia', descricao: 'Planejamento, gestão e decisões de longo prazo' },
  { nome: 'Simulação', descricao: 'Rotinas, construção e vida em miniatura' },
  { nome: 'Roguelike', descricao: 'Cada tentativa é diferente da anterior' },
  { nome: 'Puzzle', descricao: 'Quebra-cabeças e raciocínio' },
]

export const desenvolvedoras = [
  { nome: 'Valve', paisOrigem: 'Estados Unidos', site: 'https://www.valvesoftware.com' },
  { nome: 'CD Projekt Red', paisOrigem: 'Polônia', site: 'https://www.cdprojektred.com' },
  { nome: 'Supergiant Games', paisOrigem: 'Estados Unidos', site: 'https://www.supergiantgames.com' },
  { nome: 'ConcernedApe', paisOrigem: 'Estados Unidos', site: null },
  { nome: 'Maddy Makes Games', paisOrigem: 'Canadá', site: null },
  { nome: 'FromSoftware', paisOrigem: 'Japão', site: null },
  { nome: 'Team Cherry', paisOrigem: 'Austrália', site: null },
  { nome: 'Re-Logic', paisOrigem: 'Estados Unidos', site: null },
  { nome: 'Long Hat House', paisOrigem: 'Brasil', site: null },
  { nome: 'Paradox Interactive', paisOrigem: 'Suécia', site: 'https://www.paradoxinteractive.com' },
]

export const jogos = [
  {
    titulo: 'Half-Life 2',
    desenvolvedora: 'Valve',
    categorias: ['Ação', 'Aventura'],
    preco: 36.99,
    dataLancamento: '2004-11-16',
    descricao: 'Um físico vira símbolo da resistência contra uma ocupação alienígena, com combate e física que marcaram época.',
  },
  {
    titulo: 'Half-Life: Alyx',
    desenvolvedora: 'Valve',
    categorias: ['Ação', 'Aventura'],
    preco: 59.99,
    dataLancamento: '2020-03-23',
    descricao: 'Aventura em realidade virtual passada entre os dois primeiros jogos da série.',
  },
  {
    titulo: 'Portal 2',
    desenvolvedora: 'Valve',
    categorias: ['Puzzle', 'Aventura'],
    preco: 36.99,
    dataLancamento: '2011-04-19',
    descricao: 'Quebra-cabeças com portais, humor ácido e um laboratório que não perdoa distrações.',
  },
  {
    titulo: 'Counter-Strike 2',
    desenvolvedora: 'Valve',
    categorias: ['Ação'],
    preco: 0,
    dataLancamento: '2023-09-27',
    descricao: 'Tiro tático em equipes, rodadas curtas e economia de armas. Gratuito para jogar.',
  },
  {
    titulo: 'The Witcher 3: Wild Hunt',
    desenvolvedora: 'CD Projekt Red',
    categorias: ['RPG', 'Aventura'],
    preco: 79.99,
    dataLancamento: '2015-05-19',
    descricao: 'Um caçador de monstros atravessa um mundo aberto em busca de alguém muito importante.',
  },
  {
    titulo: 'Cyberpunk 2077',
    desenvolvedora: 'CD Projekt Red',
    categorias: ['RPG', 'Ação'],
    preco: 199.9,
    dataLancamento: '2020-12-10',
    descricao: 'RPG de ação numa metrópole futurista, em que cada escolha muda o rumo da sua história.',
  },
  {
    titulo: 'Hades',
    desenvolvedora: 'Supergiant Games',
    categorias: ['Roguelike', 'Ação', 'Indie'],
    preco: 47.99,
    dataLancamento: '2020-09-17',
    descricao: 'Fuja do submundo grego em combates rápidos, ganhando poderes dos deuses a cada tentativa.',
  },
  {
    titulo: 'Transistor',
    desenvolvedora: 'Supergiant Games',
    categorias: ['RPG', 'Indie'],
    preco: 36.99,
    dataLancamento: '2014-05-20',
    descricao: 'Combate que mistura tempo real e planejamento, numa cidade futurista e silenciosa.',
  },
  {
    titulo: 'Stardew Valley',
    desenvolvedora: 'ConcernedApe',
    categorias: ['Simulação', 'Indie', 'RPG'],
    preco: 24.99,
    dataLancamento: '2016-02-26',
    descricao: 'Herde uma fazenda, plante, pesque, faça amigos e reconstrua a vida no campo no seu tempo.',
  },
  {
    titulo: 'Celeste',
    desenvolvedora: 'Maddy Makes Games',
    categorias: ['Plataforma', 'Indie'],
    preco: 36.99,
    dataLancamento: '2018-01-25',
    descricao: 'Escalar uma montanha exige saltos precisos e, no caminho, enfrentar as próprias inseguranças.',
  },
  {
    titulo: 'Elden Ring',
    desenvolvedora: 'FromSoftware',
    categorias: ['RPG', 'Ação', 'Aventura'],
    preco: 229.9,
    dataLancamento: '2022-02-25',
    descricao: 'Mundo aberto sombrio e desafiador, criado em parceria com um dos grandes nomes da fantasia.',
  },
  {
    titulo: 'Dark Souls III',
    desenvolvedora: 'FromSoftware',
    categorias: ['RPG', 'Ação'],
    preco: 169.9,
    dataLancamento: '2016-04-12',
    descricao: 'Combate exigente, chefes memoráveis e um reino que desmorona a cada passo.',
  },
  {
    titulo: 'Hollow Knight',
    desenvolvedora: 'Team Cherry',
    categorias: ['Plataforma', 'Aventura', 'Indie'],
    preco: 27.99,
    dataLancamento: '2017-02-24',
    descricao: 'Explore um reino subterrâneo de insetos em um mapa vasto, feito à mão e cheio de segredos.',
  },
  {
    titulo: 'Terraria',
    desenvolvedora: 'Re-Logic',
    categorias: ['Aventura', 'Indie', 'Ação'],
    preco: 19.99,
    dataLancamento: '2011-05-16',
    descricao: 'Cave, construa e enfrente chefes em um mundo 2D que você molda como quiser.',
  },
  {
    titulo: 'Dandara',
    desenvolvedora: 'Long Hat House',
    categorias: ['Plataforma', 'Aventura', 'Indie'],
    preco: 29.99,
    dataLancamento: '2018-02-06',
    descricao: 'Plataforma brasileira em que a heroína salta entre superfícies de sal, ignorando a gravidade.',
  },
  {
    titulo: 'Crusader Kings III',
    desenvolvedora: 'Paradox Interactive',
    categorias: ['Estratégia', 'RPG'],
    preco: 139.99,
    dataLancamento: '2020-09-01',
    descricao: 'Comande uma dinastia medieval por gerações, entre casamentos, intrigas e guerras.',
  },
]

// Contas de demonstração (todas com a mesma senha) para mostrar o fluxo de usuário comum.
export const SENHA_DEMO = 'demo1234'

export const usuarios = [
  { nome: 'Ana Souza', email: 'ana.demo@vapor.com' },
  { nome: 'Bruno Lima', email: 'bruno.demo@vapor.com' },
  { nome: 'Carla Dias', email: 'carla.demo@vapor.com' },
  { nome: 'Diego Rocha', email: 'diego.demo@vapor.com' },
  { nome: 'Elisa Prado', email: 'elisa.demo@vapor.com' },
]

// [e-mail do usuário, título do jogo, nota, comentário]. Alguns jogos ficam sem avaliação de propósito.
export const avaliacoes = [
  ['ana.demo@vapor.com', 'Hades', 5, 'Viciante! Cada fuga ensina algo novo e a trilha sonora é impecável.'],
  ['bruno.demo@vapor.com', 'Hades', 5, 'Combate fluido e história que avança mesmo quando você perde.'],
  ['carla.demo@vapor.com', 'Hades', 5, null],
  ['diego.demo@vapor.com', 'Hades', 4, 'Muito bom, só enjoei um pouco da repetição depois de umas 40 horas.'],
  ['elisa.demo@vapor.com', 'Hades', 5, 'Melhor jogo do gênero que já joguei.'],

  ['carla.demo@vapor.com', 'The Witcher 3: Wild Hunt', 5, 'As missões secundárias valem mais que a principal de muito jogo por aí.'],
  ['diego.demo@vapor.com', 'The Witcher 3: Wild Hunt', 5, null],
  ['elisa.demo@vapor.com', 'The Witcher 3: Wild Hunt', 4, 'Mundo lindo, mas o inventário cansa.'],
  ['bruno.demo@vapor.com', 'The Witcher 3: Wild Hunt', 5, 'Obra-prima.'],

  ['ana.demo@vapor.com', 'Elden Ring', 5, 'Explorar sem mapa guiado é uma experiência única.'],
  ['bruno.demo@vapor.com', 'Elden Ring', 4, 'Difícil, injusto às vezes, e ainda assim não consegui largar.'],
  ['elisa.demo@vapor.com', 'Elden Ring', 5, null],

  ['ana.demo@vapor.com', 'Stardew Valley', 5, 'Meu jogo para relaxar depois do trabalho.'],
  ['carla.demo@vapor.com', 'Stardew Valley', 4, 'Aconchegante, só demora para engrenar.'],
  ['diego.demo@vapor.com', 'Stardew Valley', 5, 'Perdi o fim de semana inteiro na fazenda.'],

  ['bruno.demo@vapor.com', 'Celeste', 5, 'Difícil e emocionante, com uma mensagem que fica.'],
  ['elisa.demo@vapor.com', 'Celeste', 5, 'O modo de assistência torna tudo acessível sem tirar o mérito.'],

  ['ana.demo@vapor.com', 'Hollow Knight', 5, 'Atmosfera incrível e muito conteúdo pelo preço.'],
  ['diego.demo@vapor.com', 'Hollow Knight', 4, 'Me perdi no mapa várias vezes, mas é parte do charme.'],

  ['carla.demo@vapor.com', 'Portal 2', 5, 'Roteiro e quebra-cabeças em perfeita sintonia.'],
  ['bruno.demo@vapor.com', 'Portal 2', 5, null],

  ['diego.demo@vapor.com', 'Cyberpunk 2077', 3, 'A cidade é incrível, mas encontrei bugs demais no lançamento.'],
  ['ana.demo@vapor.com', 'Cyberpunk 2077', 4, 'Melhorou muito com as atualizações.'],

  ['elisa.demo@vapor.com', 'Dark Souls III', 4, 'Chefes inesquecíveis.'],
  ['carla.demo@vapor.com', 'Dandara', 4, 'Controles diferentes, mas viciam rápido. Ótimo ver um jogo brasileiro assim.'],
  ['bruno.demo@vapor.com', 'Counter-Strike 2', 3, 'Competitivo e divertido, mas a comunidade é pesada.'],
  ['elisa.demo@vapor.com', 'Terraria', 5, 'Horas e horas de construção e exploração.'],
  ['diego.demo@vapor.com', 'Half-Life 2', 5, 'Segue atual mesmo depois de tantos anos.'],
]

# NextHub — Mural de Oportunidades

[English](README.md) · **Português (Brasil)**

Eu criei o NextHub para reunir estágios, cursos, trabalhos freelancer e eventos em um mural pesquisável e personalizado para estudantes.

## Problema e solução

Percebi que estudantes encontram oportunidades espalhadas por redes sociais, grupos de conversa e murais físicos. Por isso, centralizei os anúncios no NextHub para ajudar cada estudante a encontrar opções relevantes, manter currículo e interesses no perfil, salvar oportunidades e acompanhar candidaturas. Também incluí um fluxo para empresas publicarem anúncios e gerenciarem candidatos.

## Funcionalidades do MVP

- Implementei cadastro, login e logout para estudantes e empresas.
- Criei o perfil do estudante com cursos, interesses, formação, formato desejado e disponibilidade.
- Adicionei upload de currículo PDF (máximo de 5 MB) e controle de acesso para o candidato e a empresa correspondente.
- Implementei listagem, busca textual e filtros por tipo, formato, assunto e área.
- Incluí detalhes de oportunidades, recomendações explicáveis, favoritos e candidaturas internas ou externas.
- Permiti que empresas criem, editem e encerrem oportunidades, analisem candidatos e atualizem o status das candidaturas. Coloquei o acesso ao currículo dentro do perfil do candidato e deixei a atualização de status junto à ação de perfil. Estudantes também podem retirar as próprias candidaturas.
- Configurei notificações derivadas das candidaturas para serem marcadas como lidas ao serem abertas; a bolinha aparece somente enquanto houver itens não lidos neste navegador.
- Desenvolvi uma interface responsiva com tema claro/escuro, navegação por perfil e barra mobile fixa que respeita a área segura do dispositivo. Mantive as colunas da grade de favoritos mesmo com poucos itens, animei o indicador das abas e centralizei os detalhes das oportunidades em um modal com rolagem interna.
- Preparei dados locais fictícios para a demonstração: 20 oportunidades, 10 usuários e 14 candidaturas.

No fluxo de candidatura interna, exijo um currículo. Para anúncios externos, direciono o estudante ao endereço de inscrição publicado. Calculo as recomendações de forma determinística usando interesses, formação e formato desejado; não chamo serviços externos de IA.

## Tecnologias

- **Front-end:** escolhi React 18, Vite 6, JavaScript e CSS.
- **Back-end:** escolhi Java 25, Spring Boot 3.5, Spring Data JPA, Hibernate 6 e Bean Validation.
- **Persistência:** uso SQLite e arquivos locais no desenvolvimento; para hospedagem, configurei PostgreSQL e Supabase Storage privado.
- **Testes e qualidade:** uso JUnit 5, Spring Boot Test, Maven, ESLint e GitHub Actions.

## Estrutura do repositório

```text
.
├── Back-end/
│   ├── src/main/java/br/com/nexthub/
│   ├── src/main/resources/application.properties
│   ├── data/nexthub.sqlite
│   ├── uploads/
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── public/
│   └── eslint.config.js
├── .github/workflows/ci.yml
├── .env.example
├── package.json              # Scripts da raiz
└── README.pt-BR.md
```

Organizei `frontend/` e `Back-end/` como a única aplicação ativa. Importo os dados fictícios de `backend/data/seed.json` somente na primeira inicialização e mantenho os dados de execução no SQLite. Antes de qualquer manutenção manual, faço cópias de segurança de `Back-end/data/nexthub.sqlite`.

## Pré-requisitos e instalação

- Java 25 ou superior.
- Node.js 22.13 ou superior e npm.
- Maven Wrapper incluído em `Back-end/`.

Para preparar o projeto, executo estes comandos na raiz do repositório:

```sh
npm ci
npm --prefix frontend ci
copy .env.example .env
```

No macOS/Linux, use `cp .env.example .env` no lugar de `copy`. O `.env` da raiz configura somente o proxy do Vite. Opcionalmente, o back-end carrega `Back-end/.env` ao iniciar; se o arquivo não existir, usa os padrões de `application.properties`. Variáveis já definidas no sistema operacional têm prioridade. Não defina `SPRING_PROFILES_ACTIVE=prod` para o desenvolvimento local: sem essa configuração, o SQLite e o armazenamento local continuam sendo usados.

## Executar localmente

Para executar o NextHub, abro dois terminais na raiz do repositório.

Terminal 1 — API (PowerShell):

```sh
cd Back-end
.\mvnw.cmd spring-boot:run
```

No Git Bash, macOS ou Linux:

```sh
cd Back-end
bash ./mvnw spring-boot:run
```

Terminal 2 — Vite:

```sh
npm run dev:frontend
```

Depois, acesso <http://localhost:5173>; o Vite encaminha minhas requisições `/api` para `http://localhost:8080`.

Configurei o back-end para criar ou atualizar `Back-end/data/nexthub.sqlite` com Hibernate (`ddl-auto=update`) sem apagar dados existentes. Também posso configurar a origem permitida pelo CORS com `NEXTHUB_CORS_ALLOWED_ORIGIN` (padrão: `http://localhost:5173`).

Para demonstrar os fluxos, uso estas contas fictícias, ambas com a senha `demo1234`:

- Estudante: `ana@nexthub.demo`
- Empresa: `talentos@nexthub.demo`

## Front-end

Organizei o front-end com React, JSX, Vite e React Router. Coloquei as páginas em `frontend/src/pages/`, os componentes compartilhados em `frontend/src/components/` e a comunicação com a API em `frontend/src/services/api.js`. Uso a URL após `#` na navegação do React Router (por exemplo, `http://localhost:5173/#/mural`). Mantive a identidade visual e os layouts responsivos em `frontend/public/css/style.css`.

## Endpoints da API

Na API Java, mantive os contratos usados pelo frontend:

- Autenticação: `POST /api/auth/register`, `/api/auth/login` e `/api/auth/logout`; `GET /api/me`.
- Perfil: `PATCH /api/profile`.
- Oportunidades: `GET` e `POST /api/opportunities`; `PATCH` e `DELETE /api/opportunities/{id}`; `GET /api/company/opportunities`.
- Favoritos: `GET /api/favorites` e `POST /api/favorites/{id}`.
- Candidaturas: `GET /api/applications`, `POST /api/applications/{id}`, `PATCH /api/applications/{id}` e `DELETE /api/applications/{id}` para o estudante dono retirar a própria candidatura.
- Currículos: `POST /api/resume` e `GET /api/resume/{id}`.

## Testes e lint

```sh
npm run lint:frontend
npm run build:frontend
npm --prefix frontend run test:e2e
```

Para validar a API, executo os testes Java em um terminal separado. No PowerShell:

```sh
cd Back-end
.\mvnw.cmd test
```

No Git Bash, macOS ou Linux:

```sh
cd Back-end
bash ./mvnw test
```

Com os testes de integração do back-end, valido cadastro, atualização do perfil com persistência dos cursos, upload de currículo, envio e retirada de candidatura e publicação de oportunidades pela empresa. Com os 14 testes de navegador, valido cadastro e login, candidatura, publicação pela empresa, leitura de notificações, currículo e perfil de candidatos, interações de cards, abas, modais e navegação mobile durante a rolagem. Para rodar os testes de navegador, instalo o Chromium uma vez e executo:

```sh
cd frontend
npm ci
npx playwright install chromium
npm run test:e2e
```

Configurei o Playwright para iniciar a API e o Vite automaticamente e usar um SQLite temporário, separado do meu banco local.

## CI

Configurei o GitHub Actions para executar em pull requests direcionados à `main` e em pushes na `main`. O workflow instala as dependências Node, executa lint e build do front-end, testes Maven do back-end e fluxos E2E com Playwright. Mantive essa configuração em `.github/workflows/ci.yml`. A última execução que verifiquei para `dev`, no commit `7be0a89`, terminou com sucesso ([execução 7](https://github.com/Levi-dev6638/NextHub/actions/runs/45915995752)).

## Persistência e segurança

Modelei as tabelas principais para usuários, oportunidades, candidaturas, favoritos e currículos; calculo as recomendações no front-end a partir do perfil. Localmente, uso Hibernate (`ddl-auto=update`) com SQLite e armazenamento de arquivos em `Back-end/uploads/`. No perfil `prod`, uso PostgreSQL com migrações Flyway e sessões HTTP persistidas no banco. Os currículos ficam em um bucket privado do Supabase Storage; somente a API usa a chave de serviço, após verificar a autorização do estudante ou da empresa. Importo `backend/data/seed.json` somente quando habilitado e o banco ainda não tem usuários. Protejo senhas com hash BCrypt e limito currículos a 5 MB.

## Preparação para deploy (não publicado)

Preparei a hospedagem com Vercel para o front-end, Render para a API Java e Supabase para PostgreSQL e currículos. Nenhum serviço foi criado ou publicado por este repositório. O fluxo recomendado é:

1. **Supabase:** crie um projeto e um bucket de Storage privado chamado `nexthub-resumes`. Restrinja o bucket a PDFs e 5 MB. No painel **Connect**, copie os dados do **Shared Pooler — Session mode**; esse modo atende ao Render em IPv4 e é compatível com sessões JDBC e migrações. Use os dados do pooler para preencher a URL JDBC, usuário e senha. A primeira inicialização da API aplica a migração Flyway.
2. **Render:** crie um Web Service Docker ligado ao repositório. Configure o diretório raiz como `.` (contexto de build na raiz), o Dockerfile como `Back-end/Dockerfile` e o health check como `/actuator/health`. Cadastre as variáveis abaixo no painel do serviço; não as coloque no código ou no front-end.
3. **Vercel:** importe o repositório com `frontend/` como diretório raiz. O build Vite gera `dist/`; o app usa caminhos relativos `/api`, então não precisa de `VITE_API_URL` no deploy. Depois que o Render fornecer a URL pública, substitua `https://your-nexthub-api.onrender.com` em `frontend/vercel.json` pela origem real da API. O rewrite encaminha `/api/*` pelo mesmo domínio do front-end para manter a sessão do navegador.

Variáveis do serviço Render:

| Variável | Valor |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | `jdbc:postgresql://<host-do-pooler>:5432/postgres?sslmode=require` |
| `DATABASE_USERNAME` | Usuário exato mostrado pelo Supabase para o pooler |
| `DATABASE_PASSWORD` | Senha do banco Supabase |
| `SUPABASE_URL` | `https://<project-ref>.supabase.co` |
| `SUPABASE_STORAGE_BUCKET` | `nexthub-resumes` |
| `SUPABASE_SERVICE_ROLE_KEY` | Chave de serviço do projeto; segredo exclusivo do back-end |
| `NEXTHUB_CORS_ALLOWED_ORIGIN` | URL HTTPS exata do front-end Vercel |
| `NEXTHUB_SEED_ENABLED` | `false` |
| `DATABASE_POOL_MAX` | `5` (opcional) |

No `.env.example`, `VITE_API_URL` configura somente o proxy local do Vite; os demais valores são referências e o Spring não carrega esse arquivo. Configure as variáveis de produção nos painéis dos provedores; não envie chaves, senhas ou arquivos `.env` ao Git. Não torne o bucket público e nunca configure a chave de serviço no Vercel. O seed de demonstração fica desabilitado por padrão em `prod`.

**Limites importantes do plano gratuito:** Render Free pode suspender a API após 15 minutos sem tráfego, demora para acordar e tem sistema de arquivos efêmero; o SQLite local e os arquivos locais não persistem nesse ambiente. Supabase Free atualmente inclui 500 MB de banco e 1 GB de arquivos, mas pode pausar projetos após uma semana sem atividade e não substitui uma estratégia de backup. Os limites podem mudar; confira os links oficiais antes de configurar. O próprio Render recomenda não usar instâncias Free para aplicações de produção. Portanto, esta configuração é adequada para demonstração e avaliação, não oferece SLA ou durabilidade de produção; use serviços pagos e backups verificados se os dados forem importantes. Como currículos contêm dados pessoais, teste com dados fictícios até estabelecer consentimento, retenção e requisitos de privacidade.

Referências oficiais: [planos Supabase](https://supabase.com/pricing), [conexões PostgreSQL do Supabase](https://supabase.com/docs/guides/database/connecting-to-postgres), [buckets privados](https://supabase.com/docs/guides/storage/buckets/fundamentals), [limites Render Free](https://render.com/docs/free) e [plano Hobby da Vercel](https://vercel.com/docs/plans/hobby).

Antes do deploy, valide localmente:

```sh
npm run lint:frontend
npm run build:frontend
npm test
cd Back-end
.\mvnw.cmd -Pproduction -DskipTests package
```

Também confirme no ambiente hospedado `/actuator/health`, login e persistência de sessão, cadastro/candidatura e upload/download de um PDF. Teste que um estudante e uma empresa sem autorização não conseguem baixar currículos alheios. O build Docker e a integração real com Supabase precisam ser verificados no provedor, pois dependem de Docker e credenciais/serviços externos.

## Equipe

- **Minha turma:** TDS — turmas mistas 6570, 6756 e 6863
- **Tech lead:** Levi (`Levi-dev6638`)
- **Responsável pela apresentação:** Lucas (`martinszvx`)
- **Integrante:** Giovana (`Giovana-Bernardes`)

## Links

- **Repositório:** <https://github.com/Levi-dev6638/mural-de-oportunidades>
- **Deploy:** Preparado, mas não publicado

# Decisões do projeto

[English](DECISIONS.md) · **Português (Brasil)**

Uso este registro para explicar as decisões que definem o MVP atual do NextHub. Organizo minhas escolhas por tema e marco as decisões anteriores como substituídas quando mudo de direção.

## 23 de novembro de 2026 — Referências para o mural de oportunidades

- **Minha decisão:** usei InfoJobs e Indeed como referência para padrões conhecidos de busca, filtros e cards de oportunidades, sem reproduzir nenhum dos produtos.
- **Alternativas que considerei:** eu poderia criar todas as interações do zero ou copiar o layout de um único portal.
- **Por que escolhi isso:** padrões reconhecíveis facilitam o uso do mural e deixam espaço para a identidade e o escopo próprios do NextHub.

## 24 de novembro de 2026 — Escopo do MVP

- **Minha decisão:** priorizei um fluxo demonstrável para estudantes e empresas: perfis, oportunidades, busca, recomendações explicáveis, favoritos, candidaturas e acesso ao currículo.
- **Alternativas que considerei:** eu poderia criar uma página estática de anúncios ou uma plataforma completa de recrutamento e seleção.
- **Por que escolhi isso:** um produto local de ponta a ponta demonstra o problema sem exigir integrações desnecessárias nem um processo completo de contratação.

## 25 de novembro de 2026 — Arquitetura oficial da aplicação (substituída em 26 de novembro)

- **Minha decisão naquele momento:** tratei `frontend/` (React 18 e Vite) e `backend/` (Node.js e Express) como a aplicação ativa. Considerei os scaffolds anteriores Java/Spring e React/Vite em `Back-end/` e `Front-end/` substituídos e os removi do projeto de trabalho.
- **Alternativas que considerei:** eu poderia manter as duas árvores da aplicação ou tornar o scaffold Java/Spring o back-end oficial.
- **Por que escolhi isso naquele momento:** a implementação Node/Express continha os fluxos de autenticação, oportunidades, perfil, candidaturas, favoritos e currículo, além dos dados de demonstração. Manter os starters Java/Vite deixaria aplicações concorrentes e não integradas, além de uma pipeline testando a fonte errada.

## 25 de novembro de 2026 — Persistência local em JSON

- **Minha decisão naquele momento:** preservei os dados existentes de demonstração e execução em `backend/data/` e armazenei os PDFs enviados no diretório ignorado `backend/uploads/`.
- **Alternativas que considerei:** eu poderia introduzir um banco SQL ou um serviço externo de dados.
- **Por que escolhi isso naquele momento:** os dados locais versionados tornavam a demonstração acadêmica reproduzível e disponível offline; considerei um banco desnecessário para a demonstração. Também exigi cópias de segurança antes de qualquer manutenção manual.

## 25 de novembro de 2026 — Sessões e proteção de currículos

- **Minha decisão naquele momento:** mantive senhas com hash bcrypt e sessões Express verificadas por perfil; limitei o acesso ao currículo ao estudante proprietário e às empresas com candidaturas para suas próprias oportunidades.
- **Alternativas que considerei:** eu poderia armazenar credenciais em texto, expor uploads como arquivos estáticos ou integrar um provedor externo de identidade.
- **Por que escolhi isso:** a abordagem atende ao MVP local e evita acesso público direto aos currículos. Um deploy de produção ainda exige sessões compartilhadas duráveis e armazenamento gerenciado de arquivos.

## 24 de novembro de 2026 — Recomendações explicáveis

- **Minha decisão:** calculo recomendações deterministicamente a partir de interesses, formação e formato desejado do estudante e exibo uma justificativa curta para cada correspondência.
- **Alternativas que considerei:** eu poderia usar ordenação aleatória, recomendações somente por palavra-chave ou ranking opaco por serviço externo de IA.
- **Por que escolhi isso:** o resultado é previsível e fácil de explicar na apresentação, sem API externa ou custo de serviço.

## 24 de novembro de 2026 — Navegação de salvos e candidaturas

- **Minha decisão:** apresento favoritos como uma aba dentro da área de Candidaturas do estudante, em vez de criar uma área de navegação separada.
- **Alternativas que considerei:** eu poderia separar Salvos e Candidaturas ou ocultar favoritos do acompanhamento.
- **Por que escolhi isso:** mantenho oportunidades salvas e enviadas no mesmo espaço sem misturar suas visualizações.

## 24 de novembro de 2026 — Navegação responsiva e tema

- **Minha decisão:** mantenho navegação específica por perfil, sidebar no desktop, barra inferior no celular e controles de tema claro/escuro em Configurações.
- **Alternativas que considerei:** eu poderia usar a mesma navegação para ambos os perfis ou colocar um controle permanente de tema na barra mobile.
- **Por que escolhi isso:** ações específicas por perfil reduzem a poluição visual e a barra mobile permanece focada na navegação.

## 24 de novembro de 2026 — Recursos fora do escopo

- **Minha decisão:** deixei gamificação, rankings, scraping automático da web, integrações institucionais externas e um processo seletivo completo das empresas fora deste MVP.
- **Alternativas que considerei:** eu poderia ampliar o produto além do fluxo demonstrável de uma semana.
- **Por que escolhi isso:** esses recursos aumentam a complexidade sem serem necessários para validar a proposta do mural de oportunidades.

## 26 de novembro de 2026 — Migração do back-end para Java e SQLite

- **Minha decisão:** substituí a API Node/Express pela API Java/Spring Boot oficial, escolhi Spring Data JPA/Hibernate para persistência e SQLite como banco local persistente.
- **Alternativas que considerei:** eu poderia manter o Node como API concorrente ou deixar os dados apenas em arquivos JSON.
- **Por que escolhi isso:** atendi à arquitetura definida para o projeto, centralizei a persistência em SQLite e mantive uma única API REST responsável pelos fluxos do NextHub. Esta decisão substitui minhas decisões de 25 de novembro sobre back-end e persistência.

## 26 de novembro de 2026 — Ajustes visuais e acesso aos currículos

- **Minha decisão:** registrei meus ajustes de estilo visual e defini que a área de candidatos da empresa deveria oferecer botões “Ver currículo”.
- **Alternativas que considerei:** eu poderia deixar o currículo acessível somente por outra tela ou não oferecer acesso direto na lista de candidatos.
- **Por que escolhi isso:** o acesso direto facilita a consulta do currículo durante a análise de candidatos.
- **Status:** registrei essa decisão de interface na época, mas não afirmei que a funcionalidade estivesse no código; eu havia revertido as alterações relacionadas.

## 27 de novembro de 2026 — Perfil, notificações e candidaturas

- **Minha decisão:** persisto os cursos do estudante junto ao perfil no SQLite e os disponibilizo pela API de perfil.
- **Alternativas que considerei:** eu poderia descartar cursos nas atualizações do perfil ou criar uma tabela separada.
- **Por que escolhi isso:** a lista de cursos é pequena e faz parte do perfil neste MVP; a API valida os dados e os armazena como JSON junto ao usuário.

- **Minha decisão:** derivo notificações dos registros de candidatura e salvo o estado de leitura por usuário no `localStorage`; exibo a bolinha somente quando houver notificações não lidas.
- **Alternativas que considerei:** eu poderia criar uma tabela de notificações no servidor ou manter a bolinha sempre visível.
- **Por que escolhi isso:** simplifico o MVP local. O estado de leitura fica no navegador e não é sincronizado entre dispositivos.

- **Minha decisão:** permito que o estudante retire a própria candidatura por `DELETE /api/applications/{id}`, sempre verificando a propriedade da candidatura.
- **Alternativas que considerei:** eu poderia permitir que empresas excluíssem candidaturas ou tornar os envios imutáveis.
- **Por que escolhi isso:** o estudante mantém controle sobre as próprias inscrições e a API aplica a autorização.

- **Minha decisão:** mantenho a barra de navegação mobile fixa durante a rolagem e incluo espaçamento para a área segura do dispositivo.
- **Alternativas que considerei:** eu poderia deixar a barra rolar com o conteúdo ou ignorar as áreas seguras do celular.
- **Por que escolhi isso:** mantenho a navegação acessível sem que a interface do sistema a cubra.

## 27 de novembro de 2026 — Ações de oportunidades e movimento

- **Minha decisão:** mantenho somente a ação “Ver” na lista de oportunidades da empresa e coloco “Editar” e “Encerrar oportunidade” nos detalhes.
- **Alternativas que considerei:** eu poderia manter todas as ações na linha da lista ou distribuir os botões entre lista e detalhes.
- **Por que escolhi isso:** simplifico a lista e evito que as ações fiquem apertadas ou quebrem em telas pequenas.

- **Minha decisão:** uso animações discretas para feedback e transições da interface, respeitando a preferência de movimento reduzido do sistema.
- **Alternativas que considerei:** eu poderia deixar a interface sem animações ou aplicar movimentos contínuos e intensos.
- **Por que escolhi isso:** ofereço retorno visual sem distrair nem desconsiderar as preferências de acessibilidade.

## 27 de novembro de 2026 — Layout de cards, modais, abas e análise de candidatos

- **Minha decisão:** mantenho as colunas dos favoritos consistentes mesmo com poucos resultados, animo o indicador da aba ativa e mantenho os detalhes da oportunidade centralizados e roláveis dentro da viewport.
- **Alternativas que considerei:** eu poderia deixar cards isolados se expandirem e se espalharem pela linha, manter o sublinhado da aba estático ou permitir que modais longos ultrapassassem a tela.
- **Por que escolhi isso:** uma grade consistente e a indicação clara da aba facilitam a navegação; o modal continua utilizável em telas menores.
- **Minha decisão:** coloco o status do candidato ao lado da ação de perfil, mantenho os dois controles do mesmo tamanho, alinho o status à direita e deixo o currículo acessível pelo perfil.
- **Alternativas que considerei:** eu poderia manter um botão de currículo separado na linha do candidato ou deixar o status em outra linha.
- **Por que escolhi isso:** organizo a linha do candidato sem remover o acesso ao currículo.

## Preparação de hospedagem em planos gratuitos

- **Minha decisão:** preservei SQLite e arquivos locais como padrão de desenvolvimento e adicionei um perfil `prod` com PostgreSQL/Flyway, sessões JDBC e currículos em bucket privado do Supabase Storage. Preparei o front-end para Vercel e a API Java para Render com Docker, sem publicar os serviços.
- **Alternativas que considerei:** eu poderia substituir a arquitetura local, usar armazenamento público de currículos ou publicar antes de validar serviços e credenciais.
- **Por que escolhi isso:** mantenho o fluxo local existente e documento um caminho de hospedagem reproduzível, mantendo segredos e documentos fora do front-end. Os planos gratuitos têm suspensão por inatividade, limites de recursos e ausência de garantias adequadas para produção; por isso, trato o deploy como demonstração e exijo serviços pagos e backups para uso confiável.

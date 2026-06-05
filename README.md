# Sistema de Gerenciamento de Portfólio de Projetos

API Spring Boot para controle de projetos, membros, fluxo de status, risco e relatórios do portfólio.

## Tecnologias
- Java 21
- Spring Boot 3.3.x
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Security (HTTP Basic)
- Swagger/OpenAPI
- JUnit 5 + Mockito

## Execução
### 1) Subir PostgreSQL com Docker

Use o arquivo `docker-compose.yml` na raiz do projeto.

Opcionalmente, copie o arquivo `.env.example` para `.env` e ajuste as variáveis locais.

```powershell
Copy-Item .env.example .env
docker compose up -d db
docker compose ps
```

Banco criado automaticamente:
- host: `localhost`
- porta: `5432`
- database: `portfolio_db`
- usuário: `postgres`
- senha: `postgres`

Para parar/remover o container:

```powershell
docker compose down
```

### 2) Rodar a API

Depois de subir o banco, rode a aplicação no IntelliJ usando a classe `com.portfolio.manager.PortfolioManagerApplication`.

Se quiser sobrescrever credenciais do banco, configure variáveis de ambiente:

- `DB_URL` = `jdbc:postgresql://localhost:5432/portfolio_db`
- `DB_USERNAME` = `postgres`
- `DB_PASSWORD` = `postgres`

Usuários padrão da API:
- `admin` / `admin`
- `user` / `user`

## Endpoints principais
### Membros mockados
- `POST /api/external/members`
- `GET /api/external/members`
- `GET /api/external/members/{id}`

### Projetos
- `POST /api/projects`
- `GET /api/projects/{id}`
- `GET /api/projects?page=0&size=10&name=...&status=...`
- `PUT /api/projects/{id}`
- `PATCH /api/projects/{id}/status`
- `PATCH /api/projects/{id}/members`
- `DELETE /api/projects/{id}`
- `GET /api/projects/report`

## Regras implementadas
- CRUD de projetos
- Classificação dinâmica de risco
- Fluxo de status com transição sequencial
- Exclusão bloqueada em estados críticos
- Associação de membros apenas com atribuição `funcionário`
- Limite de 1 a 10 membros por projeto
- Limite de até 3 projetos ativos por membro
- Relatório resumido do portfólio
- Tratamento global de exceções

## Swagger
A documentação fica disponível em:
- `/swagger-ui.html`
- `/v3/api-docs`

## Verificação rápida
Após iniciar a API, valide:

```powershell
Invoke-WebRequest http://localhost:8080/v3/api-docs | Select-Object StatusCode
Start-Process "http://localhost:8080/swagger-ui.html"
```


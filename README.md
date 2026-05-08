# Spring Auth Server
Servidor de autenticação e autorização baseado em **JWT**, desenvolvido com **Kotlin + Spring Boot**. Permite gerenciar usuários, perfis de acesso, eventos e participantes.
---
## 🛠️ Tecnologias
| Tecnologia | Versão |
|---|---|
| Kotlin | 2.2.21 |
| Spring Boot | 4.0.5 |
| Java | 21 |
| Spring Security | (gerenciado pelo Boot) |
| JJWT | 0.13.0 |
| Spring Data JPA | (gerenciado pelo Boot) |
| H2 Database | In-memory |
| SpringDoc OpenAPI (Swagger) | 3.0.2 |
---
## 🚀 Como executar
```bash
./gradlew bootRun
```
A aplicação sobe em `http://localhost:8080/api`.
> **Swagger UI:** `http://localhost:8080/api`
> **H2 Console:** `http://localhost:8080/api/h2-console` (usuário: `sa`, senha: `sa`)
---
## 🔐 Autenticação
A API utiliza **JWT Bearer Token**. Para acessar endpoints protegidos:
1. Faça login em `POST /api/users/login`
2. Use o token retornado no header: `Authorization: Bearer <token>`
| Perfil | Expiração do token |
|---|---|
| Usuário comum | 48 horas |
| ADMIN | 1 hora |
---
## 👤 Endpoints — Usuários (`/api/users`)
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `GET` | `/users` | Lista todos os usuários | Público |
| `GET` | `/users?role={role}` | Lista usuários por role | Público |
| `GET` | `/users?sortDir=ASC\|DESC` | Lista usuários ordenados por nome | Público |
| `GET` | `/users/{id}` | Busca usuário por ID | Público |
| `POST` | `/users` | Cria novo usuário | Público |
| `POST` | `/users/login` | Realiza login e retorna JWT | Público |
| `PATCH` | `/users/{id}` | Atualiza nome do usuário | Autenticado (próprio usuário ou ADMIN) |
| `DELETE` | `/users/{id}` | Remove usuário | ADMIN |
| `PUT` | `/users/{id}/roles/{role}` | Adiciona role ao usuário | ADMIN |
### Criar usuário — `POST /users`
```json
{
  "email": "usuario@email.com",
  "password": "Senha@123",
  "name": "Nome do Usuário"
}
```
### Login — `POST /users/login`
```json
{
  "email": "usuario@email.com",
  "password": "Senha@123"
}
```
**Resposta:**
```json
{
  "token": "<jwt>",
  "user": { "id": 1, "name": "Nome do Usuário", "email": "usuario@email.com" }
}
```
### Atualizar nome — `PATCH /users/{id}`
```json
{
  "name": "Novo Nome"
}
```
---
## 🏷️ Endpoints — Roles (`/api/roles`)
> Todos os endpoints de roles exigem autenticação com perfil **ADMIN**.
| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/roles` | Cria nova role |
| `GET` | `/roles` | Lista todas as roles |
### Criar role — `POST /roles`
```json
{
  "name": "PREMIUM",
  "description": "Usuário premium"
}
```
---
## 📅 Endpoints — Eventos (`/api/events`)

> Tabela no banco: `events`
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/events` | Cria novo evento | Autenticado |
| `GET` | `/events` | Lista eventos com filtros e ordenação | Público |
| `GET` | `/events/{id}` | Busca evento por ID | Público |
| `PATCH` | `/events/{id}` | Atualiza dados do evento | Autenticado |
| `DELETE` | `/events/{id}` | Remove evento | ADMIN |
| `POST` | `/events/{eventId}/participants/{participantId}` | Associa participante ao evento | Autenticado |
| `DELETE` | `/events/{eventId}/participants/{participantId}` | Remove participante do evento | ADMIN |
| `GET` | `/events/{eventId}/participants` | Lista participantes do evento | Público |
### Filtros e ordenação — `GET /events`
Todos os parâmetros são opcionais e combináveis:
| Parâmetro | Tipo | Descrição | Exemplo |
|---|---|---|---|
| `name` | string | Filtra por nome (parcial, case-insensitive) | `name=java` |
| `status` | enum | Filtra por status | `status=SCHEDULED` |
| `location` | string | Filtra por local (parcial, case-insensitive) | `location=curitiba` |
| `startDate` | ISO datetime | Eventos a partir desta data | `startDate=2026-01-01T00:00:00` |
| `endDate` | ISO datetime | Eventos até esta data | `endDate=2026-12-31T23:59:59` |
| `sortBy` | string | Campo de ordenação (`name`, `eventDate`, `createdAt`, `location`) | `sortBy=eventDate` |
| `direction` | string | Direção da ordenação (`ASC` ou `DESC`) | `direction=ASC` |
**Exemplo completo:**
```
GET /api/events?name=java&status=SCHEDULED&location=curitiba&sortBy=eventDate&direction=ASC
```
### Status do evento
| Status | Descrição |
|---|---|
| `SCHEDULED` | Evento agendado (padrão) |
| `CANCELLED` | Evento cancelado |
| `FINISHED` | Evento finalizado |
### Criar evento — `POST /events`
```json
{
  "name": "Java Summit 2026",
  "description": "Conferência sobre Java e JVM",
  "location": "Curitiba, PR",
  "eventDate": "2026-09-15T09:00:00",
  "status": "SCHEDULED"
}
```
### Atualizar evento — `PATCH /events/{id}`
Todos os campos são opcionais:
```json
{
  "name": "Novo Nome",
  "location": "São Paulo, SP",
  "status": "CANCELLED"
}
```
---
## 👥 Endpoints — Participantes (`/api/participants`)

> Tabela no banco: `participants`
| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/participants` | Cria novo participante | Autenticado |
| `GET` | `/participants` | Lista todos os participantes | Público |
| `GET` | `/participants?sortDir=ASC\|DESC` | Lista participantes ordenados por nome | Público |
| `GET` | `/participants/{id}` | Busca participante por ID | Público |
| `PATCH` | `/participants/{id}` | Atualiza dados do participante | Autenticado |
| `DELETE` | `/participants/{id}` | Remove participante | ADMIN |
### Criar participante — `POST /participants`
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "(41) 99999-0000"
}
```
### Atualizar participante — `PATCH /participants/{id}`
Todos os campos são opcionais:
```json
{
  "name": "João Santos",
  "phone": "(41) 98888-1111"
}
```
---
## 📋 Regras de Negócio
### Usuários
- **E-mail único:** não é possível cadastrar dois usuários com o mesmo e-mail.
- **Senha:** mínimo 8 caracteres, obrigatório conter letra, número e caractere especial (`@$!%*#?&`).
- **Atualização:** apenas o próprio usuário ou um ADMIN pode alterar o nome.
- **Remoção:** um ADMIN não pode ser deletado se for o único com essa role no sistema.
### Roles
- O nome da role é sempre convertido e armazenado em **maiúsculas**.
- Roles duplicadas não são permitidas.
- Somente ADMINs podem criar e listar roles.
### Eventos
- Um evento pode ter vários participantes (relação `1:N`).
- Um participante pode ser associado a apenas um evento por vez.
- Associar um participante já vinculado ao mesmo evento retorna `409 Conflict`.
- Desassociar um participante não vinculado ao evento retorna `400 Bad Request`.
- Buscar evento inexistente retorna `404 Not Found`.
### Participantes
- **E-mail único:** não é possível cadastrar dois participantes com o mesmo e-mail.
- Um participante pode existir sem estar vinculado a nenhum evento.
- Buscar participante inexistente retorna `404 Not Found`.
### Inicialização automática (Bootstrapper)
Ao subir a aplicação, são criados automaticamente:
| Recurso | Valor padrão |
|---|---|
| Role | `ADMIN` |
| Role | `PREMIUM` |
| Usuário admin | `admin@authserver.com` / `admin` |
> ⚠️ **Atenção:** altere as credenciais padrão antes de usar em produção.
---
## 🔒 Regras de Segurança
- Sessão **stateless** (sem cookies/sessão no servidor).
- **CORS** liberado para todas as origens (configurar restrições em produção).
- **CSRF** desabilitado.
- Endpoints `GET` são públicos; criação de usuário e login também são públicos.
- Ações de criação e atualização requerem autenticação JWT.
- Ações destrutivas (`DELETE`) requerem perfil **ADMIN**.
---
## 📁 Estrutura do Projeto
```
src/main/kotlin/br/pucpr/authserver/
├── AuthserverApplication.kt          # Entry point
├── Bootstrapper.kt                   # Dados iniciais (roles e admin padrão)
├── exceptions/                       # Exceções globais (400, 401, 403, 404)
├── roles/
│   ├── Role.kt                       # Entidade Role
│   ├── RoleController.kt             # Endpoints /roles
│   ├── RoleRepository.kt             # Repositório JPA
│   ├── RoleService.kt                # Regras de negócio de roles
│   ├── requests/CreateRoleRequest.kt
│   └── responses/RoleResponse.kt
├── security/
│   ├── JWT.kt                        # Geração e validação de tokens JWT
│   ├── JwtTokenFilter.kt             # Filtro de autenticação via JWT
│   ├── SecurityConfig.kt             # Configuração do Spring Security
│   └── UserToken.kt                  # Representação do usuário no token
├── users/
│   ├── User.kt                       # Entidade User
│   ├── UserController.kt             # Endpoints /users
│   ├── UserRepository.kt             # Repositório JPA
│   ├── UserService.kt                # Regras de negócio de usuários
│   ├── SortDir.kt                    # Enum de direção de ordenação
│   ├── requests/                     # DTOs de entrada
│   └── responses/                    # DTOs de saída
├── events/
│   ├── Event.kt                      # Entidade Event — tabela `events` (1:N com Participant)
│   ├── EventController.kt            # Endpoints /events e /events/{id}/participants
│   ├── EventRepository.kt            # Repositório JPA com query de filtros dinâmicos
│   ├── EventService.kt               # Regras de negócio com logs
│   ├── EventStatus.kt                # Enum: SCHEDULED, CANCELLED, FINISHED
│   ├── EventNotFoundException.kt     # Exceção 404 customizada
│   ├── requests/CreateEventRequest.kt
│   ├── requests/UpdateEventRequest.kt
│   └── responses/EventResponse.kt
└── participants/
    ├── Participant.kt                          # Entidade Participant — tabela `participants` (N:1 com Event)
    ├── ParticipantController.kt               # Endpoints /participants
    ├── ParticipantRepository.kt               # Repositório JPA
    ├── ParticipantService.kt                  # Regras de negócio com logs
    ├── ParticipantNotFoundException.kt        # Exceção 404 customizada
    ├── ParticipantAlreadyLinkedException.kt   # Exceção 409 customizada
    ├── ParticipantNotLinkedException.kt       # Exceção 400 customizada
    ├── requests/CreateParticipantRequest.kt
    ├── requests/UpdateParticipantRequest.kt
    └── responses/ParticipantResponse.kt
```
---
## ⚙️ Configuração (`application.yaml`)
```yaml
server:
  servlet:
    context-path: /api   # Todas as rotas prefixadas com /api
spring:
  datasource:
    url: jdbc:h2:mem:db  # Banco em memória (dados perdidos ao reiniciar)
```
> Para ambientes produtivos, substitua o H2 por um banco persistente (PostgreSQL, MySQL, etc.) e atualize as variáveis de conexão.

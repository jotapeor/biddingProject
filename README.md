# 🏛️ EditaisGOV — Backend

> ⚙️ API REST do Sistema de Licitações Governamentais, desenvolvida com Java 21 e Spring Boot. Responsável pelo gerenciamento de usuários, editais e processamento de lances com autenticação via JWT.

Este repositório é o **núcleo do sistema**. Para a interface web que consome esta API, consulte o repositório do **[Frontend →](https://github.com/jotapeor/bidding-frontend)**.

---

## 🚀 Tecnologias Utilizadas

| Tecnologia | Descrição |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.0.6 | Framework base |
| MySQL | Banco de dados relacional |
| JWT (jjwt) | Autenticação e autorização |
| Thymeleaf | Renderização de views |
| Maven | Gerenciador de dependências |

---

## ✨ Funcionalidades

- **Gerenciamento de Usuários:** Cadastro, autenticação e controle de perfis.
- **Gestão de Editais:** Criação e listagem de editais/leilões disponíveis.
- **Gerenciamento de Lances (Bids):** Submissão e histórico de lances (`MeuLanceDTO`).
- **Autenticação Segura:** Proteção de endpoints via tokens JWT.

---

## 🛠️ Pré-requisitos

- [JDK 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [Maven](https://maven.apache.org/)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/)

---

## ⚙️ Como Executar Localmente

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/jotapeor/biddingBackEnd.git
   cd biddingBackEnd
   ```

2. **Configure o banco de dados:**
   - Crie um banco no MySQL e importe o dump inicial:
     ```bash
     mysql -u seu_usuario -p nome_do_banco < dumpDB.sql
     ```
   - Atualize as credenciais em `src/main/resources/application.properties`.

3. **Inicie a aplicação:**
   ```bash
   # Com Maven Wrapper (Windows)
   mvnw spring-boot:run

   # Com Maven padrão
   mvn spring-boot:run
   ```

A API estará disponível em `http://localhost:8080`.

> ⚠️ O **[Frontend](https://github.com/jotapeor/bidding-frontend)** precisa que esta API esteja em execução para funcionar corretamente.

---

## 📂 Estrutura de Pacotes

```text
src/main/java/com/bidding/system/bidding/
├── controller/    # Endpoints da API REST (UserController, EditalController, LanceController)
├── model/         # Entidades e DTOs (ex: MeuLanceDTO)
├── repository/    # Acesso ao banco de dados via Spring Data JPA
└── service/       # Regras de negócio
```

---

## 🤝 Contribuindo

Sinta-se à vontade para abrir uma *issue* antes de enviar um *pull request*, especialmente para mudanças maiores.

---

## 📝 Licença

Este projeto tem fins educacionais como parte de um curso de Desenvolvimento Web com Java.

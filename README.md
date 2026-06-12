# Bidding System Backend (Sistema de Licitações/Leilões)

Este é o BackEnd de um Sistema de Licitações Governamentais, desenvolvido em **Java 21** e **Spring Boot 4.0**. A API é responsável pelo gerenciamento de usuários, criação e administração de editais, além do processamento e histórico de lances em tempo real.

O projeto utiliza uma arquitetura baseada em camadas (Controllers, Services e Repositories) e conta com autenticação e autorização via JWT (JSON Web Tokens).

## 🚀 Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.0.6
- **Banco de Dados:** MySQL
- **Segurança/Autenticação:** JWT (jjwt)
- **Views/Templates:** Thymeleaf
- **Gerenciador de Dependências:** Maven

## ✨ Principais Funcionalidades

- **Gerenciamento de Usuários:** Cadastro, autenticação e controle de perfis.
- **Gestão de Editais:** Criação e listagem de editais/leilões disponíveis.
- **Gerenciamento de Lances (Bids):** 
  - Submissão de lances para um determinado edital.
  - Listagem do histórico de lances (`MeuLanceDTO`).
- **Autenticação Segura:** Proteção de endpoints utilizando tokens JWT.

## 🛠️ Pré-requisitos

Antes de rodar o projeto, certifique-se de ter instalado em sua máquina:
- [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [Maven](https://maven.apache.org/) (O projeto já inclui o `mvnw` caso prefira usar o wrapper)
- [MySQL Server](https://dev.mysql.com/downloads/mysql/)

## ⚙️ Como executar o projeto localmente

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/SEU-USUARIO/biddingBackEnd.git
   cd biddingBackEnd
   ```

2. **Configuração do Banco de Dados:**
   - Crie um banco de dados no seu MySQL para o projeto.
   - O repositório inclui um arquivo chamado `dumpDB.sql` na raiz do projeto. Você pode importá-lo no seu banco para criar as tabelas e popular dados iniciais:
     ```bash
     mysql -u seu_usuario -p nome_do_banco < dumpDB.sql
     ```
   - Atualize as credenciais do banco de dados (URL, usuário e senha) no arquivo `src/main/resources/application.properties` (ou `application.yml`).

3. **Iniciando a Aplicação:**
   - Via Maven Wrapper (Windows):
     ```cmd
     mvnw spring-boot:run
     ```
   - Via Maven padrão:
     ```bash
     mvn spring-boot:run
     ```

A aplicação deverá iniciar e rodar localmente (por padrão em `http://localhost:8080`).

## 📂 Estrutura de Pacotes

A estrutura principal dos arquivos Java (em `src/main/java/com/bidding/system/bidding/`) segue o padrão:
- `/controller` - Endpoints da API REST (ex: `UserController`, `EditalController`, `LanceController`).
- `/model` - Entidades e DTOs (ex: `MeuLanceDTO`).
- `/repository` - Interfaces de acesso ao banco de dados via Spring Data JPA.
- `/service` - Regras de negócio da aplicação.

## 🤝 Contribuindo

Sinta-se à vontade para realizar um *fork* do projeto e enviar *pull requests*. Para mudanças maiores, por favor abra uma *issue* primeiro para discutirmos o que você gostaria de alterar.

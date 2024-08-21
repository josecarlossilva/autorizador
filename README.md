# Projeto Autorizador

## Tabela de Conteúdos

- [Descrição do Projeto](#descrição-do-projeto)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Configuração Inicial dos Dados](#configuração-inicial-dos-dados)
- [Regras de Autorização](#regras-de-autorização)
- [Como Executar a Aplicação](#como-executar-a-aplicação)
- [Exemplo de Chamada REST](#exemplo-de-chamada-rest)

## Descrição do Projeto

O projeto Autorizador é um sistema que processa e autoriza transações financeiras. O sistema utiliza um banco de dados em memória (H2) para armazenar informações sobre categorias de benefícios, transações e mapeamentos de comerciantes. As regras de autorização são aplicadas para garantir que as transações estejam dentro dos limites permitidos e que os dados associados sejam consistentes.

## Tecnologias Utilizadas

- **Java 17**: Linguagem de programação principal.
- **Spring Boot**: Framework para facilitação de desenvolvimento de aplicações.
- **Spring Data JPA**: Abstrai a implementação de repositórios baseados em JPA.
- **Spring MVC**: Para a construção de APIs RESTful.
- **Lombok**: Reduz o código boilerplate em classes Java.
- **H2 Database**: Banco de dados em memória para desenvolvimento e testes.
- **Maven/Gradle**: Ferramentas de gerenciamento de dependências e build.

## Configuração Inicial dos Dados

O projeto utiliza um banco de dados H2 em memória para facilitar o desenvolvimento e testes. O arquivo SQL `sql_script.sql` é utilizado para criar as tabelas e inserir dados iniciais automaticamente quando a aplicação é iniciada.

### Arquivo `sql_script.sql`

O arquivo `sql_script.sql` contém a definição das tabelas necessárias para a aplicação e insere dados iniciais nas tabelas:

```sql
-- Drop existing tables if they exist
DROP TABLE IF EXISTS benefit_category;
DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS merchant_mapping;

-- Create tables

-- Table `benefit_category`
CREATE TABLE benefit_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mcc VARCHAR(255),
    balance DOUBLE NOT NULL,
    category VARCHAR(255) NOT NULL
);

-- Table `transaction`
CREATE TABLE transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mcc VARCHAR(255),
    amount DOUBLE,
    merchant_name VARCHAR(255)
);

-- Table `merchant_mapping`
CREATE TABLE merchant_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant VARCHAR(255) NOT NULL,
    corrected_mcc VARCHAR(255)
);

-- Insert initial data into benefit_category
INSERT INTO benefit_category (mcc, balance, category) VALUES ('5411', 100.0, 'FOOD');
INSERT INTO benefit_category (mcc, balance, category) VALUES (NULL, 200.0, 'CASH');

-- Insert initial data into transaction table for example purposes
INSERT INTO transaction (mcc, amount, merchant_name) VALUES ('5411', 50.0, 'Grocery Store');
INSERT INTO transaction (mcc, amount, merchant_name) VALUES ('5812', 150.0, 'Restaurant');
INSERT INTO transaction (mcc, amount, merchant_name) VALUES ('9999', 200.0, 'Unknown Merchant');

-- Insert initial data into merchant_mapping
INSERT INTO merchant_mapping (merchant, corrected_mcc) VALUES ('Grocery Store', '5411');
INSERT INTO merchant_mapping (merchant, corrected_mcc) VALUES ('Restaurant', '5812');
INSERT INTO merchant_mapping (merchant, corrected_mcc) VALUES ('ATM', '6011');
```

Esses arquivos são automaticamente executados no início da aplicação para garantir que o banco de dados esteja preparado com os dados necessários.

## Regras de Autorização

As regras de autorização neste projeto incluem, mas não se limitam a:

1. **Verificação de Saldo**: Certificar-se de que o saldo da categoria de benefícios é suficiente para cobrir a transação antes de autorizá-la.
2. **Validação do MCC**: Verificar se o Merchant Category Code (MCC) está correto e presente no mapeamento de comerciantes.
3. **Classificação de Transações**: Classificar a transação de acordo com o MCC e aplicar as regras de negócio específicas para cada categoria de benefício.

## Como Executar a Aplicação

### Pré-requisitos

- Java 17 instalado no seu sistema.
- O artefato `autorizador.jar` deve estar presente no seu diretório de trabalho.

### Passos para Executar

1. Abra o terminal (prompt de comando).
2. Navegue até o diretório onde o `autorizador.jar` está localizado.
3. Execute o seguinte comando:

```sh
java -jar autorizador.jar
```

Este comando iniciará a aplicação Spring Boot, criando as tabelas e inserindo os dados iniciais no banco de dados em memória H2.

### Acessando o Console do H2

Para verificar os dados inseridos e executar consultas adicionais, acesse o console H2 em:
http://localhost:8080/h2-console

Esta URL é padrão para acessar o console do H2 quando ele está configurado para ser acessível na aplicação Spring Boot.

3. **Configurar a Conexão**: Você verá uma tela de login que solicita algumas informações para conectar ao banco de dados H2. Preencha os campos da seguinte maneira:

    - **JDBC URL**: `jdbc:h2:mem:autorizador`
    - **Username**: `sa`
    - **Password**: `password`

   ## Descrição:
A URL JDBC `jdbc:h2:mem:autorizador` indica que você está se conectando a um banco de dados H2 em memória chamado `autorizador`.
- `sa` é o nome de usuário padrão.
- `password` é a senha associada.

*Nota: Se você alterou essas configurações no `application.properties` ou `application.yml`, use os valores que você configurou.*

4. **Login**: Após preencher as informações, clique no botão `Connect`. Isso abrirá a interface do Console do H2, onde você poderá visualizar e interagir com o banco de dados.

### Exemplos de Uso do Console H2

- **Visualizar Tabelas**: No painel da esquerda, você verá uma lista das tabelas disponíveis no banco de dados. Clique em uma tabela para visualizar seus dados.

- **Executar Consultas SQL**: No painel principal, você pode digitar e executar consultas SQL para manipular ou consultar dados. Por exemplo, para ver todas as transações, você pode executar a seguinte consulta:

  ```sql
  SELECT * FROM transaction;
  ```

- **Inserir Dados**: Você também pode inserir novos dados através de comandos SQL. Por exemplo:

  ```sql
  INSERT INTO transaction (mcc, amount, merchant_name) VALUES ('1234', 200.0, 'Novo Comerciante');
  ```

### Conclusão

O Console do H2 é uma ferramenta poderosa para desenvolvimento e depuração, permitindo acesso direto e manipulação do banco de dados em memória. Certifique-se de fechar a conexão ao banco de dados quando terminar suas consultas para garantir a segurança dos dados e a performance da aplicação.

## Exemplo de Chamada REST

Para testar a funcionalidade de autorização de transações, utilize a seguinte chamada REST com `curl`:

```sh
curl --location 'http://localhost:8080/transactions/authorize' \
--header 'Content-Type: application/json' \
--data '{
  "account": "123",
  "totalAmount": 100.00,
  "mcc": "5811",
  "merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```

### Explicação dos Campos

- **account**: Identificador da conta.
- **totalAmount**: Total do valor da transação.
- **mcc**: Merchant Category Code (Código de Categoria do Comerciante).
- **merchant**: Nome do comerciante.

Isso garantirá que a chamada REST seja feita corretamente para a endpoint `/transactions/authorize` do projeto Autorizador.

## Conclusão

Este projeto demonstra um exemplo de sistema de autorização básico, utilizando tecnologias modernas e configurando um ambiente de desenvolvimento e teste com banco de dados em memória. Para qualquer dúvida ou contribuição, sinta-se à vontade para abrir uma issue ou um pull request no repositório do projeto.

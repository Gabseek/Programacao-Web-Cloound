# Ecommerce (Spring Boot) — Instruções de instalação e execução

Repositório backend: Spring Boot

## Requisitos
- Java 17+ (recomendado LTS) — versões mais recentes também funcionam.
- Maven 3.6+ ou wrapper Maven (`mvnw`) incluído no projeto.

## Estrutura relevante
- spring-backend/ — código fonte Spring Boot
- spring-backend/src/main/resources/data.sql — dados iniciais (produtos, cart, order)
- spring-backend/media/ — pasta onde colocar imagens dos produtos (servida em `/media/**`)

## Preparação (uma vez)
Certifique-se de ter Java e Maven instalados:

```bash
sudo apt update
# Atualiza a lista de pacotes disponíveis no sistema.

sudo apt install openjdk-17-jdk
# Instala o Java OpenJDK 17 (JDK + JRE).

java -version
# Verifica se o Java foi instalado corretamente e mostra a versão instalada.

sudo apt install maven
# Instala o Apache Maven a partir dos repositórios oficiais do Ubuntu.

mvn -v
# Verifica se o Maven foi instalado corretamente e mostra sua versão.

```
## Executando a aplicação
No diretório do projeto:

- Usando Maven:
  ```
  mvn -f spring-backend/pom.xml spring-boot:run
  ```

Por padrão a aplicação inicia em `http://localhost:8080`.

## Endpoints úteis
- Loja (páginas HTML): `http://localhost:8080/`
- Carrinho: `http://localhost:8080/cart/`
- Produto (detalhe): `http://localhost:8080/product/{id}/`


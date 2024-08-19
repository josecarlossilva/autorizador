# Use uma imagem base do OpenJDK
FROM openjdk:22-jdk

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR da sua aplicação para o contêiner
COPY target/*.jar app.jar

# Expõe a porta que sua aplicação irá usar
EXPOSE 8080

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
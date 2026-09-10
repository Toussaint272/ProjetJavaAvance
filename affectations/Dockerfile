# ============================================
# Étape 1 : Build avec Maven
# ============================================
FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /app

# Copier le pom.xml et télécharger les dépendances (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et compiler
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================
# Étape 2 : Image légère pour l'exécution
# ============================================
FROM eclipse-temurin:17-jre-slim

WORKDIR /app

# Copier le JAR généré depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port (Render injectera le port via la variable PORT)
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
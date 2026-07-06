# ---- Stage 1: build frontend ----
FROM node:20 AS frontend-build
WORKDIR /app/komga-webui
COPY komga-webui/package*.json ./
RUN npm install
COPY komga-webui/ ./
RUN npm run build

# ---- Stage 2: build backend (includes frontend via prepareThymeLeaf) ----
FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /app
COPY . .
COPY --from=frontend-build /app/komga-webui/dist ./komga-webui/dist
RUN ./gradlew prepareThymeLeaf :komga:bootJar --no-daemon

# ---- Stage 3: runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/komga/build/libs/komga-*.jar komga.jar
EXPOSE 25600
ENTRYPOINT ["java", "-jar", "komga.jar"]

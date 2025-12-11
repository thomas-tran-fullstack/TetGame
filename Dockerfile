# Build frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /workspace/frontend
COPY frontend/package*.json ./
# Use `npm ci` for reproducible installs; fall back to `npm install` if lockfile is out of sync
RUN npm ci || npm install --no-audit --prefer-offline --silent
COPY frontend/ .
RUN npm run build

# Build backend and include frontend static files
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
# Copy built frontend into backend resources so Spring Boot can serve it
COPY --from=frontend-builder /workspace/frontend/dist ./src/main/resources/static
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

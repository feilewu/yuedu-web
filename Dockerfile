FROM node:20-alpine AS frontend
WORKDIR /app
COPY frontend/package.json frontend/pnpm-workspace.yaml ./
RUN npm install -g pnpm && pnpm install --ignore-scripts --no-lockfile
COPY frontend/ .
RUN pnpm build-only

FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY settings.gradle.kts build.gradle.kts ./
COPY server/ server/
COPY modules/ modules/
COPY --from=frontend /app/dist/ web/vue/
RUN ./gradlew :server:jar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /app/server/build/libs/server-1.0.0.jar app.jar
COPY --from=backend /app/web/ web/
EXPOSE 1122
ENTRYPOINT ["java", "--add-opens", "java.base/java.time=ALL-UNNAMED", "-jar", "app.jar"]

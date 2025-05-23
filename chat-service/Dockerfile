# Использование официального образа OpenJDK в качестве базового образа
FROM openjdk:17-jdk-slim

# Установка рабочей директории внутри контейнера
WORKDIR /app

# Копирование собранного JAR-файла вашего приложения в контейнер
# Предполагается, что JAR-файл называется chat-service.jar и находится в директории 'target'
# Если у вас другое имя, обновите его здесь.
COPY target/chat-service-0.0.1-SNAPSHOT.jar chat-service.jar

# ОТКРЫТИЕ ПОРТОВ:
# EXPOSE 8060 - Порт для основного HTTP/REST трафика
EXPOSE 8060
# EXPOSE 8090 - Порт для Actuator
EXPOSE 8090

# Команда для запуска вашего Spring Boot приложения
ENTRYPOINT ["java", "-jar", "chat-service.jar"]
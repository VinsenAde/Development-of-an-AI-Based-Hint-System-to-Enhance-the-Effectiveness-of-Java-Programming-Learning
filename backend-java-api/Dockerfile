# FROM eclipse-temurin:21-jdk

# RUN useradd -m sandboxuser

# USER sandboxuser

# WORKDIR /sandbox

# CMD ["bash"]
FROM eclipse-temurin:21-jdk

RUN apt-get update && \
    apt-get install -y docker.io && \
    rm -rf /var/lib/apt/lists/*

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
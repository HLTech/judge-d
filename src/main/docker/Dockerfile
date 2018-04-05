FROM openjdk:8-alpine

ARG JAR_FILE
COPY ${JAR_FILE} contract-verifier.jar

COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

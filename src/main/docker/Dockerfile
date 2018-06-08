FROM openjdk:8

COPY judge-dredd.jar judge-dredd.jar

COPY entrypoint.sh entrypoint.sh
RUN chmod +x entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

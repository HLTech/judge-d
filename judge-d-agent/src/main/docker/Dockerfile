FROM adoptopenjdk/openjdk11:alpine-slim



WORKDIR /root
ADD ./judge-d-agent-0.1-SNAPSHOT.jar /root/judge-d-agent.jar
ADD ./entrypoint.sh /root/entrypoint.sh
RUN chmod +x /root/entrypoint.sh

CMD ["/root/entrypoint.sh"]

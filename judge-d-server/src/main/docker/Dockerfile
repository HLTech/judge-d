FROM openjdk:8-jdk

WORKDIR /root
ADD ./judge-d-server-0.1-SNAPSHOT.jar /root/judge-d.jar
ADD ./entrypoint.sh /root/entrypoint.sh
RUN chmod +x /root/entrypoint.sh

CMD ["/root/entrypoint.sh"]

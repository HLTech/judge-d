FROM adoptopenjdk/openjdk11:alpine-slim

WORKDIR /root
ADD judge-d* /root/judge-d.jar
ADD entrypoint.sh /root/entrypoint.sh
RUN chmod +x /root/entrypoint.sh

CMD ["/root/entrypoint.sh"]

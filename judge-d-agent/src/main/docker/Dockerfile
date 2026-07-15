FROM amazoncorretto:17-alpine

WORKDIR /root
ADD judge-d-agent* /root/judge-d-agent.jar
ADD entrypoint.sh /root/entrypoint.sh
RUN chmod +x /root/entrypoint.sh

CMD ["/root/entrypoint.sh"]

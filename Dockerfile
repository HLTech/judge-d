FROM openjdk:8-jdk-alpine


ARG MAVEN_VERSION=3.5.4
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

ADD src /sourcecode/src
ADD pom.xml /sourcecode/pom.xml

WORKDIR /sourcecode


RUN apk add --no-cache curl tar bash \
  && mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-$MAVEN_VERSION-bin.tar.gz\
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn \
  && mvn clean package -DskipTests=true -Dmaven.javadoc.skip=true -B -V \
  && cp ./target/judge-d-0.1-SNAPSHOT.jar /root/judge-d.jar \
  && rm -rf /sourcecode \
  && rm -rf $MAVEN_CONFIG


WORKDIR /root
ADD entrypoint.sh /root/entrypoint.sh
RUN chmod +x /root/entrypoint.sh

ENTRYPOINT ["/root/entrypoint.sh"]

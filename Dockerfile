FROM    java:8

MAINTAINER Sergey Skovorodnikov <sg.skov@gmail.com>

ENV     JAVA_HOME       /usr/lib/jvm/java-8-openjdk-amd64
ENV     GLASSFISH_HOME  /usr/local/glassfish4
ENV     PATH            $PATH:$JAVA_HOME/bin:$GLASSFISH_HOME/bin

# mongo repo setup

RUN     apt-get update && \
        apt-get install -y wget unzip && \
        rm -rf /var/lib/apt/lists/*

RUN     wget -P /tmp/ http://download.java.net/glassfish/4.1.1/release/glassfish-4.1.1.zip && \
        unzip /tmp/glassfish-4.1.1.zip -d /usr/local/ && \ 
        rm -f /tmp/glassfish-4.1.1.zip

RUN     git clone https://github.com/sergenikov/rest-service-java.git /usr/local/restclinic && \
        cp /usr/local/restclinic/restclinic.war /usr/local/glassfish4/glassfish/domains/domain1/autodeploy/restclinic.war

RUN     mkdir -p /data/db

EXPOSE  8080 4848 8181

WORKDIR /usr/local/glassfish4

CMD     /usr/local/glassfish4/bin/asadmin start-domain --verbose domain1

FROM ubuntu:18.04
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
		apt-get -y install apt-utils && \
		apt-get -y install software-properties-common debconf-utils wget && \
		apt-get clean && rm -rf /var/lib/apt/lists/*

RUN apt-get update && \
	apt-get install -y openjdk-8-jdk && \
	apt-get install -y ant && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

RUN apt-get update && \
	apt-get install -y ca-certificates-java && \
	apt-get clean && \
	update-ca-certificates -f && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

RUN wget https://dl.bintray.com/sbt/debian/sbt-1.3.8.deb && \
    wget http://downloads.lightbend.com/scala/2.12.8/scala-2.12.8.deb && \
    dpkg -i sbt-1.3.8.deb && \
    dpkg -i scala-2.12.8.deb && \
    rm *.deb

RUN apt-get update \
    && apt-get install -y \
        nmap \
        vim \
        git \
        unzip

RUN apt-get update \
    && apt-get install -y curl \
    && apt-get -y autoclean

ENV NVM_DIR /usr/local/nvm
ENV NODE_VERSION 12.16.1

RUN curl --silent -o- https://raw.githubusercontent.com/creationix/nvm/v0.31.2/install.sh | bash

RUN /bin/bash -c "source $NVM_DIR/nvm.sh \
    && nvm install $NODE_VERSION \
    && nvm alias default $NODE_VERSION \
    && nvm use default"

ENV NODE_PATH $NVM_DIR/v$NODE_VERSION/lib/node_modules
ENV PATH $NVM_DIR/versions/node/v$NODE_VERSION/bin:$PATH

RUN npm install -g npm@6.8.0

EXPOSE 8000
EXPOSE 9000
EXPOSE 5000
EXPOSE 8888
EXPOSE 443
EXPOSE 3000

VOLUME /home/zaneta/projekt/
ENV JAVA_OPTS="-Dhttp.proxyHost=localhost -Dhttp.proxyPort=443 -Dhttp.proxySet=true -Dhttps.proxyHost=localhost -Dhttps.proxyPort=443 -Dhttps.proxySet=true"

WORKDIR /home/zaneta/projekt
COPY . .

WORKDIR /home/zaneta/projekt/client
RUN npm install

WORKDIR /home/zaneta/projekt/
CMD /bin/bash ./run.sh
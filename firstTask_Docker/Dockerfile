FROM ubuntu:20.04

MAINTAINER Stanislav Shevchuk

ENV TZ=Europe/Warsaw

RUN apt-get update && apt-get install -y

#Java
RUN apt-get install openjdk-17-jre-headless -y
RUN apt-get install curl zip unzip git nano -y

#Kotlin
RUN curl -s https://get.sdkman.io | bash
RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && \
    sdk install kotlin && \
    sdk install gradle"

#Ktlint
RUN curl -sSLO https://github.com/pinterest/ktlint/releases/download/0.44.0/ktlint && \
	chmod a+x ktlint && \
	mv ktlint /usr/local/bin/
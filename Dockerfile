FROM frolvlad/alpine-java:jdk8-slim
COPY distributions/*.zip /wapy/wapy.zip
WORKDIR /wapy
RUN ls -la
RUN unzip wapy.zip
EXPOSE 8080
WORKDIR /wapy/wapy-backend-1.0/bin/
ARG JDBC_URL
ARG JDBC_USER
ARG JDBC_PASS
CMD ["./me.wapy.backend"]

ENV WAPY_JDBC_URL=$JDBC_URL
ENV WAPY_JDBC_USERNAME=$JDBC_USERNAME
ENV WAPY_JDBC_PASSWORD=$JDBC_PASSWORD
FROM frolvlad/alpine-java:jdk8-slim
COPY distributions/*.zip /wapy/wapy.zip
WORKDIR /wapy
RUN ls -la
RUN unzip wapy.zip
EXPOSE 8080
WORKDIR /wapy/wapy-backend-1.0/bin/
CMD ["./me.wapy.backend"]
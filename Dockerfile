FROM adoptopenjdk/openjdk8:jre
COPY distributions/*.zip /wapy/wapy.zip
WORKDIR /wapy
RUN unzip wapy.zip
EXPOSE 8080
WORKDIR /wapy/wapy-backend-1.0/bin/
CMD ["./me.wapy.backend"]
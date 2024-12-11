# ベースイメージとして軽量なJava 11のイメージを使用
FROM adoptopenjdk:11-jre-hotspot

ARG CONF="persistence_docker-compose.conf"
ENV CONF_PATH=${CONF}

# アプリケーションJARをコピー
COPY ./target/scala-3.4.3/til-akka-typed.jar /app/til-akka-typed.jar

# 下記を参考に ENTRYPOINT でシェル形式を指定しても警告が出ないように明示的に使用するシェルを指定
# https://docs.docker.com/reference/build-checks/json-args-recommended/#explicitly-specify-the-shell
SHELL ["/bin/bash", "-c"]

# アプリケーションをENTRYPOINTとして実行
ENTRYPOINT exec java -Dconfig.resource=/${CONF_PATH} -jar /app/til-akka-typed.jar
# ベースイメージとして軽量なJava 11のイメージを使用
FROM adoptopenjdk:11-jre-hotspot

# アプリケーションJARをコピー
COPY ./target/scala-3.4.3/til-akka-typed.jar /app/til-akka-typed.jar

# アプリケーションをENTRYPOINTとして実行
ENTRYPOINT ["java", "-Dconfig.resource=/persistence_docker-compose.conf", "-jar", "/app/til-akka-typed.jar"]
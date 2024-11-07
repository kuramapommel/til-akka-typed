# Akka Typed の学習用プロジェクト

## サーバの起動

### sbt を使用

1. `sbt run` コマンドでサーバを起動

### java -jar を使用

1. `sbt assembly` コマンドで FAT jar ファイルを作成
2. `java -jar ./target/scala-3.4.3/til-akka-typed.jar` コマンドでサーバを起動

### docker を使用

1. `sbt assembly` コマンドで FAT jar ファイルを作成
2. `docker build --platform linux/arm64 -t til-akka-typed .` コマンドで docker image を作成
   - Apple Silicon の場合 `--platform linux/arm64` を指定
3. `docker-compose up` コマンドでコンテナを起動

### minikube を使用

1. `sbt assembly` コマンドで FAT jar ファイルを作成
2. `docker build --platform linux/arm64 -t til-akka-typed .` コマンドで docker image を作成
   - Apple Silicon の場合 `--platform linux/arm64` を指定
3. `docker tag til-akka-typed:latest kuramapommel/til-akka-typed:latest` コマンドで docekr image にタグ付け
4. `docker push kuramapommel/til-akka-typed:latest` コマンドで Docker Hub に docker image を送信
5. `minikube start --driver=docker` コマンドで minikube クラスターを起動
6. `minikube mount ${PWD}/scripts:/scripts` コマンドで `./sample/script` を MinikubeVM の `/scripts` にマウントする
7. `kubectl apply -f kubernetes.yml` コマンドで deployments, services を起動
8. `kubectl apply -f minikube.yml` コマンドで services を起動
9. `minikube tunnel` コマンドで localhost からアクセスできるようにトンネルを開く

#### クラスターダッシュボードの確認

1. `minikube dashboard` コマンドでダッシュボードを表示

#### pods を削除 / minikube クラスターの停止

1. `kubectl get pods` コマンドで pods 情報を確認
2. `kubectl delete pod <pod-name>` コマンドで対象の pod を削除
3. `kubectl delete -f kubernetes.yml` で pods を削除
4. `minikube stop` で minikube クラスターを停止

## E2E

商品登録 API

```sh
curl -XPOST http://localhost:8080/product -d '{"name": "test", "imageUrl": "https://placehold.jp/111111/777777/150x150.png", "price": 100, "description": "test"}' -H "Content-Type:application/json"
```

商品編集 API

```sh
curl -XPOST http://localhost:8080/product/:productId -d '{"name": "test-product", "imageUrl": "https://placehold.jp/777777/111111/150x150.png", "price": 200, "description": "test-description"}' -H "Content-Type:application/json"
```

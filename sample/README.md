# Akka Typed の学習用プロジェクト

## サーバの起動

### sbt を使用

1. `sbt` コマンドで sbt を起動
2. `set javaOptions += "-DSEED_NODES.0=akka://til-akka-typed@0.0.0.0:2551"` コマンドで環境変数を設定
3. `set javaOptions += "-DSEED_HOST=0.0.0.0"` コマンドで環境変数を設定
4. `set javaOptions += "-DSEED_PORT=2551"` コマンドで環境変数を設定
5. `run` コマンドでサーバを起動

### java -jar を使用

1. `sbt assembly` コマンドで FAT jar ファイルを作成
2. `java -DSEED_NODES.0="akka://til-akka-typed@0.0.0.0:2551" -DSEED_HOST="0.0.0.0" -DSEED_PORT="2551" -jar ./target/scala-3.4.3/til-akka-typed.jar` コマンドでサーバを起動

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
6. `kubectl apply -f ./kubernetes/namespace.yml` コマンドで namespace を作成
7. `minikube mount ${PWD}/scripts:/scripts` コマンドで `./sample/scripts` を MinikubeVM の `/scripts` にマウントする
8. `kubectl apply -f ./kubernetes/dynamodb-local.yml` コマンドで dynamodb-local をセットアップ
9. `kubectl apply -f ./kubernetes/akka-cluster.yml` コマンドで akka-cluster server を起動
10. `minikube tunnel` コマンドで localhost からアクセスできるようにトンネルを開く

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

商品削除 API

```sh
curl -XDELETE http://localhost:8080/product/:productId
```

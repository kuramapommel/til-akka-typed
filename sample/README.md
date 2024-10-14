# Akka Typed の学習用プロジェクト

## サーバの起動

### sbt を使用

1. `sbt run` コマンドでサーバを起動

### java -jar を使用

1. `sbt assembly` コマンドで FAT jar ファイルを作成
2. `java -jar ./target/scala-3.4.3/til-akka-typed.jar` コマンドでサーバを起動

### docker を使用

1. `sbt assembly` コマンドで FAT jar ファイルを作成
2. `docker build --platform linux/amd64 -t til-akka-typed .` コマンドで docker image を作成
   - Apple Silicon の場合 `--platform linux/amd64` を指定
3. `docker-compose up` コマンドでコンテナを起動

## E2E

商品登録 API

```sh
curl -XPOST http://localhost:8080/product -d '{"name": "test", "imageUrl": "https://placehold.jp/111111/777777/150x150.png", "price": 100, "description": "test"}' -H "Content-Type:application/json"
```

商品編集 API

```sh
curl -XPOST http://localhost:8080/product/:productId -d '{"name": "test-product", "imageUrl": "https://placehold.jp/777777/111111/150x150.png", "price": 200, "description": "test-description"}' -H "Content-Type:application/json"
```

# Sample Akka HTTP server

## Interacting with the sample

After starting the sample with `sbt run` the following requests can be made:

Create a product:

    curl -XPOST http://localhost:8080/product -d '{"name": "test", "imageUrl": "https://placehold.jp/111111/777777/150x150.png", "price": 100, "description": "test"}' -H "Content-Type:application/json"

Edit a user:

    curl -XPOST http://localhost:8080/product/:productId -d '{"name": "test-product", "imageUrl": "https://placehold.jp/777777/111111/150x150.png", "price": 200, "description": "test-description"}' -H "Content-Type:application/json"

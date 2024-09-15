package com.kuramapommel.til_akka_typed.domain.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.annotation.showAsInfix

class ProductSpec extends AnyWordSpecLike with Matchers:
  "Product" should:
    "プロダクトID, プロダクト名, 画像URL, 価格, 詳細情報によって生成される" in:
      val id = new ProductId("test-id")
      val name = "product1"
      val imageUrl = "https://placehold.jp/123456/abcdef/150x150.png"
      val price = 100
      val description = "description"

      val product = Product(id, name, imageUrl, price, description)
      product must be(Product(id, name, imageUrl, price, description))

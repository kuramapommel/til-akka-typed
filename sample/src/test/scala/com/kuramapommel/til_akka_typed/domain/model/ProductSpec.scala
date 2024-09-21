package com.kuramapommel.til_akka_typed.domain.model

import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.annotation.showAsInfix

class ProductSpec extends AnyWordSpecLike with Matchers:
  "Product" should:
    "プロダクトID, プロダクト名, 画像URL, 価格, 詳細情報によって生成される" in:
      val id = new ProductId("test-id")
      val name = "product1"
      val imageUrl: ImageURL = "https://placehold.jp/123456/abcdef/150x150.png"
      val price = 100
      val description = "description"

      val product = Product(id, name, imageUrl, price, description)
      product must be(Product(id, name, imageUrl, price, description))

    "edit でプロダクト名, 画像URL, 価格, 詳細情報を変更できる" in:
      val id = new ProductId("test-id")
      val product =
        Product(id, "product1", "https://placehold.jp/123456/abcdef/150x150.png", 100, "description")

      val name = "商品"
      val imageUrl: ImageURL = "https://placehold.jp/abcdef/123456/150x150.png"
      val price = 200
      val description = "説明"
      val edited = product.edit(
        Some("商品"),
        Some("https://placehold.jp/abcdef/123456/150x150.png"),
        Some(200),
        Some("説明")
      )

      edited must be(Product(id, name, imageUrl, price, description))

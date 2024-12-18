package com.kuramapommel.til_akka_typed.domain.model

import akka.serialization.jackson.CborSerializable
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*

/**
 * 商品エンティティ.
 *
 * @constructor
 *   商品エンティティの生成.
 * @param id
 *   商品ID
 * @param name
 *   商品名
 * @param imageUrl
 *   商品画像URL
 * @param price
 *   価格
 * @param description
 *   商品説明
 */
final case class Product(
    id: ProductId,
    name: String,
    imageUrl: ImageURL,
    price: Int,
    description: String,
    deleted: Boolean = false
) extends CborSerializable:

  /**
   * 商品情報の編集.
   *
   * @param nameOpt
   *   商品名
   * @param imageUrlOpt
   *   商品画像URL
   * @param priceOpt
   *   価格
   * @param descriptionOpt
   *   商品説明
   * @return
   *   編集後の商品エンティティ
   */
  def edit(
      nameOpt: Option[String],
      imageUrlOpt: Option[ImageURL],
      priceOpt: Option[Int],
      descriptionOpt: Option[String]
  ): Product =
    copy(
      name = nameOpt.getOrElse(name),
      imageUrl = imageUrlOpt.getOrElse(imageUrl),
      price = priceOpt.getOrElse(price),
      description = descriptionOpt.getOrElse(description)
    )

  /**
   * 商品の削除.
   *
   * @return
   *   削除後の商品エンティティ
   */
  def delete(): Product = copy(deleted = true)

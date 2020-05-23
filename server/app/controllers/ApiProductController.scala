package controllers

import javax.inject._
import models._
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ApiProductController @Inject()(productRepository: ProductRepository,
                                  productCategoryRepository: ProductCategoryRepository,
                                  cartProductRepository: CartProductRepository,
                                  categoryRepository: CategoryRepository,
                                  wishListProductRepository: WishListProductRepository,
                                  discountCodeRepository: DiscountCodeRepository,
                                  returnRepository: ReturnRepository,
                                  opinionRepository: OpinionRepository,
                                  orderProductRepository: OrderProductRepository,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("name")) {
      Future(Ok("No name parameter in query"))
    }
    else if (!params.contains("description")) {
      Future(Ok("No description parameter in query"))
    }
    else if (!params.contains("price")) {
      Future(Ok("No price parameter in query"))
    }
    else if (!params.contains("categories")) {
      Future(Ok("No categories parameter in query"))
    }
    else {
      val catArray = params("categories").replaceAll(" ", "").split( ',' )
      val longCatArray = catArray.map(_.toLong)
      val prodId = productRepository.create(params("name"), params("description"), params("price").toInt)
      prodId.map(id => {
        for (catId <- longCatArray) {
          categoryRepository.exists(catId).map(exists => {
            if (exists) {
              productCategoryRepository.create(id, catId)
            }
          })
        }

      })
      Future(Ok("Product created!"))
    }
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val products = Await.result(productRepository.list(), Duration.Inf)
    Ok(Json.toJson(products))
  }

  def readById: Action[AnyContent] = Action { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      val productCategories = Await.result(productCategoryRepository.getByProductId(params("id").toLong), Duration.Inf)
      val products = Await.result(productRepository.getById(params("id").toLong), Duration.Inf)
      val categories = productCategories.map( p => p.categoryId )
      val jsonProducts = Json.toJson(products)

      val res = JsObject(Seq(
        ("product", jsonProducts),
        ("categories", Json.toJson(categories))
      ))

      Ok(res)
    }

  }

  def readByCategoryId: Action[AnyContent] = Action { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("categoryId")) {
      Ok("No categoryId parameter in query")
    }
    else {
      val categoryId = params("categoryId").toLong
      val productCategories = Await.result(productCategoryRepository.getByCategoryId(categoryId), Duration.Inf)
      val prodArray = new ListBuffer[(Product)]()

      productCategories.map( p => {
        val product = Await.result(productRepository.getById(p.productId), Duration.Inf)
        prodArray += product
      })

      val jsonProducts = Json.toJson(prodArray)
      Ok(jsonProducts)
    }

  }

  def update = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {

      try {
        val id = params("id").toLong
        val products = productRepository.getByIdOption(id)

        products.map(product => product match {
          case Some(p) => {
            val name = if (params.contains("name")) params("name") else p.name
            val description = if (params.contains("description")) params("description") else p.description
            val price = if (params.contains("price")) params("price").toInt else p.price
            val categories = if (params.contains("categories")) params("categories").replaceAll(" ", "")
              .split(',')
              .map(_.toLong) else Array[Long]()

            if (categories.size != 0) productCategoryRepository.deleteProduct(id) // if categories are not empty, delete old one

            val newProduct = Product(id, name, description, price)
            productRepository.update(id, newProduct)
            for (catId <- categories) {
              categoryRepository.exists(catId).map(exists => {
                if (exists) {
                  productCategoryRepository.create(id, catId)
                }
              })
            }
            Ok("Product updated!")
          }
          case None => Ok("No object with such id")
        })
      }
      catch {
        case e: NumberFormatException => Future(Ok("Id has to be integer"))
      }

    }
  }

  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      try {
        productCategoryRepository.deleteProduct(params("id").toLong)
        productRepository.delete(params("id").toLong)
        cartProductRepository.deleteProduct(params("id").toLong)
        wishListProductRepository.deleteProduct(params("id").toLong)
        discountCodeRepository.delete(params("id").toLong)
        returnRepository.delete(params("id").toLong)
        opinionRepository.deleteProduct(params("id").toLong)
        orderProductRepository.deleteProduct(params("id").toLong)
        Ok("Product deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}

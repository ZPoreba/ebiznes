package controllers

import javax.inject._
import models.{CartProductRepository, Category, CategoryRepository, DiscountCodeRepository, OpinionRepository, OrderProductRepository, OrderRepository, Product, ProductCategory, ProductCategoryRepository, ProductRepository, ReturnRepository, WishListProductRepository}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration

@Singleton
class ProductController @Inject()(productRepository: ProductRepository,
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

  val createForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "price" -> number,
      "category" -> seq(number),
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val updateForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "price" -> number,
      "category" -> seq(number),
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val categories = categoryRepository.list()
    categories.map (cat => {
      var cat_list = new ListBuffer[(String, String)]()
      for (c <- cat) {
        cat_list.+=((c.id.toString,c.name))
      }
      val cat_list_seq = cat_list.toList

      Ok(views.html.productadd(createForm, cat_list_seq))
    })
  }

  def createHandle = Action.async { implicit request =>

    var categ:Seq[Category] = Seq[Category]()
    val categories = categoryRepository.list().onComplete{
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    var cat_list = new ListBuffer[(String, String)]()
    for (c <- categ) {
      cat_list.+=((c.id.toString,c.name))
    }
    val cat_list_seq = cat_list.toList

    createForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productadd(errorForm, cat_list_seq))
        )
      },
      product => {
          productRepository.create(product.name, product.description, product.price).map { id =>
            for (c <- product.category) {
              productCategoryRepository.create(id, c)
            }
          Redirect(routes.ProductController.create()).flashing("success" -> "product.created")
        }
      }
    )

  }

  def read: Action[AnyContent] = Action { implicit request =>
    val products = Await.result(productRepository.list(), Duration.Inf)
    val product_categories = new ListBuffer[Seq[ProductCategory]]()

    for (p <- products) {
      val product_categories_result = Await.result(productCategoryRepository.getByProductId(p.id), Duration.Inf)
      product_categories.+=(product_categories_result)
    }
    val cat_list_seq = product_categories.toList

    print(cat_list_seq)

    Ok(views.html.productsread(products, cat_list_seq))
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val produkt = productRepository.getByIdOption(id)
    val product_categories = productCategoryRepository.getByProductId(id)
    val product_categories_result = Await.result(product_categories, Duration.Inf)

    produkt.map(product => product match {
      case Some(p) => Ok(views.html.productread(p, product_categories_result))
      case None => Redirect(routes.ProductController.read())
    })
  }

  def update(id: Long):Action[AnyContent] = Action.async { implicit request =>

    val categories = categoryRepository.list()
    categories.map (cat => {
      var cat_list = new ListBuffer[(String, String)]()
      for (c <- cat) {
        cat_list.+=((c.id.toString,c.name))
      }
      val cat_list_seq = cat_list.toList

      val product = productRepository.getById(id)
      val product_result = Await.result(product, Duration.Inf)

      val prodForm = updateForm.fill( UpdateProductForm(product_result.id, product_result.name, product_result.description, product_result.price, Seq[Int]() ))
      Ok(views.html.productupdate(prodForm, cat_list_seq))

    })

  }

  def updateHandle = Action.async { implicit request =>

    var categ:Seq[Category] = Seq[Category]()
    val categories = categoryRepository.list().onComplete{
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    var cat_list = new ListBuffer[(String, String)]()
    for (c <- categ) {
      cat_list.+=((c.id.toString,c.name))
    }
    val cat_list_seq = cat_list.toList

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm, cat_list_seq))
        )
      },
      product => {

        if(product.category.size > 0) {
          productCategoryRepository.deleteProduct(product.id)
          for (c <- product.category) {
            productCategoryRepository.create(product.id, c)
          }
        }

        productRepository.update(product.id, Product(product.id, product.name, product.description, product.price)).map { _ =>
          Redirect(routes.ProductController.update(product.id)).flashing("success" -> "product updated")
        }
      }
    )
    
  }

  def delete(id: Long): Action[AnyContent]  = Action {

    productCategoryRepository.deleteProduct(id)
    productRepository.delete(id)
    cartProductRepository.deleteProduct(id)
    wishListProductRepository.deleteProduct(id)
    discountCodeRepository.delete(id)
    returnRepository.delete(id)
    opinionRepository.deleteProduct(id)
    orderProductRepository.deleteProduct(id)
    Redirect("/readproducts")

  }

}

case class CreateProductForm(name: String, description: String, price: Int, category: Seq[Int])
case class UpdateProductForm(id: Long, name: String, description: String, price: Int, category: Seq[Int])
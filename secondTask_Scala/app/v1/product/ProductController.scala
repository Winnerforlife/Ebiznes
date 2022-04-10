package v1.product

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ProductFormInput(title: String, price: String, category: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class ProductController @Inject()(cc: ProductControllerComponents)(
    implicit ec: ExecutionContext)
    extends ProductBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[ProductFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "price" -> text,
        "category" -> text
      )(ProductFormInput.apply)(ProductFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("index: ")
    productResourceHandler.find.map { products =>
      Ok(Json.toJson(products))
    }
  }

  def process: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = ProductAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      productResourceHandler.lookup(id).map { product =>
        Ok(Json.toJson(product))
      }
  }

  def delete(id: String): Action[AnyContent] = ProductAction.async {
    implicit request =>
      logger.trace(s"delete: id = $id")
      productResourceHandler.delete(id).map { product =>
        Ok(Json.toJson(product))
      }
  }

  def update(id: String): Action[AnyContent] = ProductAction.async {
    implicit request =>
      logger.trace(s"update: id = $id")
      processJsonUpdate(id)
  }

  private def processJsonProduct[A]()(
      implicit request: ProductRequest[A]): Future[Result] = {
    def failure(badForm: Form[ProductFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ProductFormInput) = {
      productResourceHandler.create(input).map { product =>
        Created(Json.toJson(product)).withHeaders(LOCATION -> product.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def processJsonUpdate[A](id: String)(
    implicit request: ProductRequest[A]): Future[Result] = {
    def failure(badForm: Form[ProductFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ProductFormInput) = {
      productResourceHandler.update(id, input).map { product =>
        Created(Json.toJson(product))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

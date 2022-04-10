package v1.shoppingBasket

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class ShoppingBasketFormInput(title: String, quantity: String, price: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class ShoppingBasketController @Inject()(cc: ShoppingBasketControllerComponents)(
    implicit ec: ExecutionContext)
    extends ShoppingBasketBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[ShoppingBasketFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "quantity" -> text,
        "price" -> text,

      )(ShoppingBasketFormInput.apply)(ShoppingBasketFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = ShoppingBasketAction.async { implicit request =>
    logger.trace("index: ")
    shoppingBasketResourceHandler.find.map { shoppingBasket =>
      Ok(Json.toJson(shoppingBasket))
    }
  }

  def process: Action[AnyContent] = ShoppingBasketAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPost()
  }

  def show(id: String): Action[AnyContent] = ShoppingBasketAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      shoppingBasketResourceHandler.lookup(id).map { shoppingBasket =>
        Ok(Json.toJson(shoppingBasket))
      }
  }

  def delete(id: String): Action[AnyContent] = ShoppingBasketAction.async {
    implicit request =>
      logger.trace(s"delete: id = $id")
      shoppingBasketResourceHandler.delete(id).map { basket =>
        Ok(Json.toJson(Basket))
      }
  }

  def update(id: String): Action[AnyContent] = ShoppingBasketAction.async {
    implicit request =>
      logger.trace(s"update: id = $id")
      processJsonUpdate(id)
  }


  private def processJsonPost[A]()(
      implicit request: ShoppingBasketRequest[A]): Future[Result] = {
    def failure(badForm: Form[ShoppingBasketFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ShoppingBasketFormInput) = {
      shoppingBasketResourceHandler.create(input).map { shoppingBasket =>
        Created(Json.toJson(shoppingBasket))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def processJsonUpdate[A](id: String)(
    implicit request: ShoppingBasketRequest[A]): Future[Result] = {
    def failure(badForm: Form[ShoppingBasketFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ShoppingBasketFormInput) = {
      shoppingBasketResourceHandler.update(id, input).map { shoppingBasket =>
        Created(Json.toJson(shoppingBasket))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}

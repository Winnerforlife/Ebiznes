package v1.shoppingBasket

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the PostResource controller.
  */
class ShoppingBasketRouter @Inject()(controller: ShoppingBasketController) extends SimpleRouter {
  val prefix = "/v1/basket"

  def link(id: ShoppingBasketId): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.index

    case PUT(p"/$id") =>
      controller.update(id)

    case POST(p"/") =>
      controller.process

    case GET(p"/$id") =>
      controller.show(id)

    case DELETE(p"/$id") =>
      controller.delete(id)
  }

}

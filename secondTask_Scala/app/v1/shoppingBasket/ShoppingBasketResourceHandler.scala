package v1.shoppingBasket

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
  * DTO for displaying post information.
  */
case class ShoppingBasketResource(id: String, title: String, quantity: String, price: String)

object ShoppingBasketResource {
  /**
    * Mapping to read/write a PostResource out as a JSON value.
    */
    implicit val format: Format[ShoppingBasketResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[ShoppingBasketResource]]
  */
class ShoppingBasketResourceHandler @Inject()(
                                             routerProvider: Provider[ShoppingBasketRouter],
                                             shoppingBasketRepository: ShoppingBasketRepository)(implicit ec: ExecutionContext) {

  def create(shoppingBasketInput: ShoppingBasketFormInput)(implicit mc: MarkerContext): Future[ShoppingBasketResource] = {
    var index = shoppingBasketRepository.size()

    val data = ShoppingBasketData(ShoppingBasketId((index + 1).toString), shoppingBasketInput.title, shoppingBasketInput.quantity, shoppingBasketInput.price)
    shoppingBasketRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[ShoppingBasketResource]] = {
    val postFuture = shoppingBasketRepository.get(ShoppingBasketId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def delete(id: String)(implicit mc: MarkerContext): Future[Option[ShoppingBasketResource]] = {
    val postFuture = shoppingBasketRepository.delete(ShoppingBasketId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[ShoppingBasketResource]] = {
    shoppingBasketRepository.list().map { postDataList =>
      postDataList.map(postData => createPostResource(postData))
    }
  }

  def update(id: String, shoppingBasketFormInput: ShoppingBasketFormInput)(implicit mc: MarkerContext): Future[ShoppingBasketResource] = {
    val data = ShoppingBasketData(ShoppingBasketId(id), shoppingBasketFormInput.title, shoppingBasketFormInput.quantity, shoppingBasketFormInput.price)
    shoppingBasketRepository.update(ShoppingBasketId(id), data).map { id =>
      createPostResource(data)
    }
  }

  private def createPostResource(p: ShoppingBasketData): ShoppingBasketResource = {
    ShoppingBasketResource(p.id.toString, p.title, p.quantity, p.price)
  }

}

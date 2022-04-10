package v1.shoppingBasket

import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

final case class ShoppingBasketData(id: ShoppingBasketId,  title: String, quantity: String, price: String)

class ShoppingBasketId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object ShoppingBasketId {
  def apply(raw: String): ShoppingBasketId = {
    require(raw != null)
    new ShoppingBasketId(Integer.parseInt(raw))
  }
}

class ShoppingBasketExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PostRepository.
  */
trait ShoppingBasketRepository {
  def size()(implicit mc: MarkerContext): Int

  def create(data: ShoppingBasketData)(implicit mc: MarkerContext): Future[ShoppingBasketId]

  def list()(implicit mc: MarkerContext): Future[Iterable[ShoppingBasketData]]

  def get(id: ShoppingBasketId)(implicit mc: MarkerContext): Future[Option[ShoppingBasketData]]

  def delete(id: ShoppingBasketId)(implicit mc: MarkerContext): Future[Option[ShoppingBasketData]]

  def update(id: ShoppingBasketId, data: ShoppingBasketData)(implicit mc: MarkerContext): Future[Iterable[ShoppingBasketData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class ShoppingBasketRepositoryImpl @Inject()()(implicit ec: ShoppingBasketExecutionContext)
    extends ShoppingBasketRepository {

  private val logger = Logger(this.getClass)

  private val shoppingBasketList = ListBuffer(
    ShoppingBasketData(ShoppingBasketId("1"), "Potato", "2kg", "3 zł"),
    ShoppingBasketData(ShoppingBasketId("2"), "Apple", "10kg", "3 zł"),
    ShoppingBasketData(ShoppingBasketId("3"), "Carrot", "1kg", "2 zł"),
    ShoppingBasketData(ShoppingBasketId("4"), "Biscuit", "0.1kg", "2 zł"),
    ShoppingBasketData(ShoppingBasketId("5"), "Croissant", "0.1kg", "6 zł"),
  )

  override def size()(
    implicit mc: MarkerContext): Int = {
    shoppingBasketList.length
  }

  override def list()(implicit mc: MarkerContext): Future[Iterable[ShoppingBasketData]] = {
    Future {
      logger.trace(s"list: ")
      shoppingBasketList
    }
  }

  override def get(id: ShoppingBasketId)(implicit mc: MarkerContext): Future[Option[ShoppingBasketData]] = {
    Future {
      logger.trace(s"get: id = $id")
      shoppingBasketList.find(shoppingBasket => shoppingBasket.id == id)
    }
  }

  override def delete(id: ShoppingBasketId)(implicit mc: MarkerContext): Future[Option[ShoppingBasketData]] = {
    Future {
      logger.trace(s"delete: id = $id")
      val deleted = shoppingBasketList.find(shoppingBasket => shoppingBasket.id == shoppingBasketId(id))
      shoppingBasketList.remove(shoppingBasketList.indexWhere(shoppingBasket => shoppingBasket.id == shoppingBasketId(id)))
      deleted
    }
  }

  def create(data: ShoppingBasketData)(implicit mc: MarkerContext): Future[ShoppingBasketId] = {
    Future {
      logger.trace(s"create: data = $data")
      shoppingBasketList += data
      data.id
    }
  }

  override def update(id: ShoppingBasketId, data: ShoppingBasketData)( implicit mc: MarkerContext): Future[Iterable[ShoppingBasketData]] = {
    Future {
      logger.trace(s"update: id = $id")
      logger.trace(shoppingBasketList.indexWhere(shoppingBasket => shoppingBasket.id == shoppingBasketId(id)).toString)
      shoppingBasketList.update(shoppingBasketList.indexWhere(shoppingBasket => shoppingBasket.id == shoppingBasketId(id)), data)
      shoppingBasketList.find(shoppingBasket => shoppingBasket.id == shoppingBasketId(id))
    }
  }
}

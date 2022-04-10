package v1.product

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer

final case class ProductData(id: ProductId, title: String, price: String, category: String)

class ProductId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object ProductId {
  def apply(raw: String): ProductId = {
    require(raw != null)
    new ProductId(Integer.parseInt(raw))
  }
}

class ProductExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the ProductRepository.
  */
trait ProductRepository {
  def size()(implicit mc: MarkerContext): Int

  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId]

  def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]]

  def get(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]]

  def delete(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]]

  def update(id: ProductId, data: ProductData)(implicit mc: MarkerContext): Future[Iterable[ProductData]]
}

/**
  * A trivial implementation for the Post Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class ProductRepositoryImpl @Inject()()(implicit ec: Product
  ExecutionContext)
    extends ProductRepository {

  private val logger = Logger(this.getClass)

  private val productList = ListBuffer(
    ProductData(ProductId("1"), "Potato", "3 zł", "Vegetable"),
    ProductData(ProductId("2"), "Carrot", "2 zł", "Vegetable"),
    ProductData(ProductId("3"), "Apple", "3 zł", "Fruit"),
    ProductData(ProductId("4"), "Biscuit", "2 zł", "Sweet"),
    ProductData(ProductId("5"), "Croissant", "6 zł", "Sweet")
  )

  override def size()(
    implicit mc: MarkerContext): Int = {
    productList.length
  }

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace(s"list: ")
      productList
    }
  }

  override def get(id: ProductId)(
      implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      productList.find(product => product.id == id)
    }
  }

  override def delete(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"delete: id = $id")
      val deleted = productList.find(product => product.id == ProductId(id))
      productList.remove(productList.indexWhere(product => product.id == ProductId(id)))
      deleted
    }
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId] = {
    Future {
      logger.trace(s"create: data = $data")
      productList += data
      data.id
    }
  }

  override def update(id: ProductId, data: ProductData)( implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace(s"update: id = $id")
      logger.trace(productList.indexWhere(product => product.id == ProductId(id)).toString)
      productList.update(productList.indexWhere(product => product.id == ProductId(id)), data)
      productList.find(product => product.id == ProductId(id))
    }
  }
}

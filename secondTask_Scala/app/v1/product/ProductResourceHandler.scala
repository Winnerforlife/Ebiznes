package v1.product

import play.api.MarkerContext
import play.api.libs.json._

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying product information.
  */
case class ProductResource(id: String, link: String, title: String, price: String, category: String)

object ProductResource {
  /**
    * Mapping to read/write a ProductResource out as a JSON value.
    */
    implicit val format: Format[ProductResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
class ProductResourceHandler @Inject()(
                                     routerProvider: Provider[ProductRouter],
                                     productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  def create(productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    var index = productRepository.size()

    val data = ProductData(ProductId((index + 1).toString), productInput.title, productInput.price, productInput.category)

    productRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    val postFuture = productRepository.get(ProductId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[ProductResource]] = {
    productRepository.list().map { productDataList =>
      productDataList.map(productData => createPostResource(productData))
    }
  }

  def delete(id: String)(implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    val postFuture = productRepository.delete(ProductId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def update(id: String, productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    val data = ProductData(ProductId(id), productInput.title, productInput.price, productInput.category)
    productRepository.update(ProductId(id), data).map { id =>
      createPostResource(data)
    }
  }

  private def createPostResource(p: ProductData): ProductResource = {
    ProductResource(p.id.toString, routerProvider.get.link(p.id), p.title, p.price, p.category)
  }

}

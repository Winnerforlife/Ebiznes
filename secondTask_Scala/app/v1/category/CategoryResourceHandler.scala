package v1.category

import play.api.MarkerContext
import play.api.libs.json._

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying category information.
  */
case class CategoryResource(id: String, name: String)

object CategoryResource {
  /**
    * Mapping to read/write a CategoryResource out as a JSON value.
    */
  implicit val format: Format[CategoryResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[CategoryResource]]
  */
class CategoryResourceHandler @Inject()(
                                         routerProvider: Provider[CategoryRouter],
                                         categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) {

  def create(categoryInput: CategoryFormInput)(
    implicit mc: MarkerContext): Future[CategoryResource] = {
    var index = categoryRepository.size()

    val data = CategoryData(CategoryId((index + 1).toString), categoryInput.name)
    categoryRepository.create(data).map { id =>
      createPostResource(data)
    }
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[Option[CategoryResource]] = {
    val postFuture = categoryRepository.get(CategoryId(id))
    postFuture.map { maybeCategoryData =>
      maybeCategoryData.map { categoryData =>
        createPostResource(categoryData)
      }
    }
  }

  def delete(id: String)(
    implicit mc: MarkerContext): Future[Option[CategoryResource]] = {
    val postFuture = categoryRepository.delete(CategoryId(id))
    postFuture.map { maybePostData =>
      maybePostData.map { postData =>
        createPostResource(postData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[CategoryResource]] = {
    categoryRepository.list().map { categoryDataList =>
      categoryDataList.map(categoryData => createPostResource(categoryData))
    }
  }

  def update(id: String, categoryInput: CategoryFormInput)(
    implicit mc: MarkerContext): Future[CategoryResource] = {
    val data = CategoryData(CategoryId(id), categoryInput.name)
    categoryRepository.update(CategoryId(id), data).map { id =>
      createPostResource(data)
    }
  }

  private def createPostResource(p: CategoryData): CategoryResource = {
    CategoryResource(p.id.toString, p.name)
  }

}

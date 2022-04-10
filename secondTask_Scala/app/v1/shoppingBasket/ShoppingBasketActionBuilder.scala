package v1.shoppingBasket

import javax.inject.Inject

import net.logstash.logback.marker.LogstashMarker
import play.api.{Logger, MarkerContext}
import play.api.http.{FileMimeTypes, HttpVerbs}
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * A wrapped request for Shopping Basket resources.
  *
  * This is commonly used to hold request-specific information like
  * security credentials, and useful shortcut methods.
  */
trait ShoppingBasketRequestHeader
    extends MessagesRequestHeader
    with PreferredMessagesProvider
class ShoppingBasketRequest[A](request: Request[A], val messagesApi: MessagesApi)
    extends WrappedRequest(request)
    with ShoppingBasketRequestHeader

/**
  * Provides an implicit marker that will show the request in all logger statements.
  */
trait RequestMarkerContext {
  import net.logstash.logback.marker.Markers

  private def marker(tuple: (String, Any)) = Markers.append(tuple._1, tuple._2)

  private implicit class RichLogstashMarker(marker1: LogstashMarker) {
    def &&(marker2: LogstashMarker): LogstashMarker = marker1.and(marker2)
  }

  implicit def requestHeaderToMarkerContext(
      implicit request: RequestHeader): MarkerContext = {
    MarkerContext {
      marker("id" -> request.id) && marker("host" -> request.host) && marker(
        "remoteAddress" -> request.remoteAddress)
    }
  }

}

/**
  * The action builder for the Shopping Basket resource.
  *
  * This is the place to put logging, metrics, to augment
  * the request with contextual data, and manipulate the
  * result.
  */
class ShoppingBasketActionBuilder @Inject()(messagesApi: MessagesApi,
                                          playBodyParsers: PlayBodyParsers)(
    implicit val executionContext: ExecutionContext)
    extends ActionBuilder[ShoppingBasketRequest, AnyContent]
    with RequestMarkerContext
    with HttpVerbs {

  override val parser: BodyParser[AnyContent] = playBodyParsers.anyContent

  type ShoppingBasketRequestBlock[A] = ShoppingBasketRequest[A] => Future[Result]

  private val logger = Logger(this.getClass)

  override def invokeBlock[A](request: Request[A],
                              block: ShoppingBasketRequestBlock[A]): Future[Result] = {
    // Convert to marker context and use request in block
    implicit val markerContext: MarkerContext = requestHeaderToMarkerContext(
      request)
    logger.trace(s"invokeBlock: ")

    val future = block(new ShoppingBasketRequest(request, messagesApi))

    future.map { result =>
      request.method match {
        case GET | HEAD =>
          result.withHeaders("Cache-Control" -> s"max-age: 100")
        case other =>
          result
      }
    }
  }
}

/**
  * Packages up the component dependencies for the Shopping Basket controller.
  *
  * This is a good way to minimize the surface area exposed to the controller, so the
  * controller only has to have one thing injected.
  */
case class ShoppingBasketControllerComponents @Inject()(
                                                       shoppingBasketActionBuilder: ShoppingBasketActionBuilder,
                                                       shoppingBasketResourceHandler: ShoppingBasketResourceHandler,
                                                       actionBuilder: DefaultActionBuilder,
                                                       parsers: PlayBodyParsers,
                                                       messagesApi: MessagesApi,
                                                       langs: Langs,
                                                       fileMimeTypes: FileMimeTypes,
                                                       executionContext: scala.concurrent.ExecutionContext)
    extends ControllerComponents

/**
  * Exposes actions and handler to the ShoppingBasketController by wiring the injected state into the base class.
  */
class ShoppingBasketBaseController @Inject()(pcc: ShoppingBasketControllerComponents)
    extends BaseController
    with RequestMarkerContext {
  override protected def controllerComponents: ControllerComponents = pcc

  def ShoppingBasketAction: ShoppingBasketActionBuilder = pcc.shoppingBasketActionBuilder

  def shoppingBasketResourceHandler: ShoppingBasketResourceHandler = pcc.shoppingBasketResourceHandler
}

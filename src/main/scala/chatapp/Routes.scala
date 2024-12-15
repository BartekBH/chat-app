package chatapp

import cats.MonadThrow
import fs2.{Stream, Pipe}
import fs2.io.file.Files
import org.http4s.{HttpApp, HttpRoutes, StaticFile}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import cats.effect.std.Queue
import cats.effect.kernel.Concurrent
import cats.syntax.all.*

class Routes[F[_]: Files: Concurrent] extends Http4sDsl[F] {
  def service(wsb: WebSocketBuilder2[F]): HttpApp[F] = {
    HttpRoutes.of[F] {
      case request @ get -> Root / "chat.html" =>
        StaticFile
          .fromPath(
            fs2.io.file.Path(getClass.getClassLoader.getResource("chat.html").getFile),
            Some(request)
          )
        .getOrElseF(NotFound())

      case GET -> Root / "ws" =>
        val wrappedQueue: F[Queue[F, WebSocketFrame]] = {
          Queue.unbounded[F, WebSocketFrame]
        }
        
        wrappedQueue.flatMap { actualQueue =>
          val send: Stream[F, WebSocketFrame] = {
            Stream.fromQueueUnterminated(actualQueue)
          }
          val receive: Pipe[F, WebSocketFrame, Unit] = {
            _.foreach(actualQueue.offer)
          }
          wsb.build(send, receive)
        }
    }
  }.orNotFound

}

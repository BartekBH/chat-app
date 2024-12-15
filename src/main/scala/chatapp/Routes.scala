package chatapp

import cats.MonadThrow
import fs2.io.file.Files
import org.http4s.{HttpApp, HttpRoutes, StaticFile}
import org.http4s.dsl.Http4sDsl

class Routes[F[_]: Files: MonadThrow] extends Http4sDsl[F] {
  def service: HttpApp[F] = {
    HttpRoutes.of[F] { case request @ get -> Root / "chat.html" =>
      StaticFile
        .fromPath(
          fs2.io.file.Path(getClass.getClassLoader.getResource("chat.html").getFile),
          Some(request)
        )
      .getOrElseF(NotFound())
    }
  }.orNotFound

}

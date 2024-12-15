package chatapp

import cats.effect.Async
import com.comcast.ip4s.{host, port}
import fs2.io.file.Files
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import cats.syntax.all.*

object Server {
  def server[F[_]: Async: Files: Network]: F[Unit] = {
    val host = host"0.0.0.0"
    val port = port"8080"
    EmberServerBuilder
      .default[F]
      .withHost(host)
      .withPort(port)
      .withHttpWebSocketApp(wsb => new Routes().service(wsb))
      .build
      .useForever
      .void
  }

}

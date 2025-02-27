package chatapp

import cats.effect.Async
import cats.effect.kernel.Ref
import cats.effect.std.Queue
import cats.syntax.all.*
import com.comcast.ip4s.{host, port}
import fs2.concurrent.Topic
import fs2.io.file.Files
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.websocket.WebSocketFrame

object Server {
  def server[F[_]: {Async, Files, Network}](
    q: Queue[F, OutputMessage],
    t: Topic[F, OutputMessage],
    im: InputMessage[F],
    chatP: ChatProtocol[F]
  ): F[Unit] = {
    val host = host"0.0.0.0"
    val port = port"8080"
    EmberServerBuilder
      .default[F]
      .withHost(host)
      .withPort(port)
      .withHttpWebSocketApp(wsb => new Routes().service(wsb, q, t, im, chatP))
      .build
      .useForever
      .void
  }

}

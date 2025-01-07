package chatapp

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import chatapp.Server.server
import fs2.concurrent.Topic
import org.http4s.websocket.WebSocketFrame
import fs2.Stream

object Program extends IOApp.Simple {
  def program: IO[Unit] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame]
      t <- Topic[IO, WebSocketFrame]
      s <- Stream(
        Stream.fromQueueUnterminated(q).through(t.publish),
        Stream.eval(server[IO](q, t))
      ).parJoinUnbounded.compile.drain
    } yield s
  }

  override def run: IO[Unit] = program
}

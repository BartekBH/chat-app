package chatapp

import cats.effect.{IO, IOApp}
import chatapp.Server.server

object Program extends IOApp.Simple {
  override def run: IO[Unit] = server[IO]

}

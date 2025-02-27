package chatapp

import cats.effect.kernel.Resource
import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import chatapp.Server.server
import dev.profunktor.redis4cats.effect.Log.Stdout.given
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import fs2.Stream
import fs2.concurrent.Topic
import natchez.Trace.Implicits.noop
import skunk.Session

import scala.concurrent.duration.*

object Program extends IOApp.Simple {
  private def makeRedis: Resource[IO, RedisCommands[IO, String, String]] =
    Redis[IO].utf8("redis://127.0.0.1:6379")

  private def makePostgres: Resource[IO, Resource[IO, Session[IO]]] =
    Session
      .pooled[IO](
        host = "localhost",
        port = 5432,
        user = "postgres",
        password = Some("password"),
        database = "chatapp",
        max = 10
      )

  def program: IO[Unit] = {
    makeRedis.use { redis =>
      makePostgres.use { session =>
        for {
          postgresprotocol <- PostgresProtocol.make[IO](session)
          redisprotocol <- RedisProtocol.make[IO](redis)
          chatprotocol <- ChatProtocol.make[IO](redisprotocol, postgresprotocol)
          im <- InputMessage.make[IO](chatprotocol)
          q <- Queue.unbounded[IO, OutputMessage]
          t <- Topic[IO, OutputMessage]
          s <- Stream(
            Stream.fromQueueUnterminated(q).through(t.publish),
            Stream
              .awakeEvery[IO](30.seconds)
              .map(_ => KeepAlive)
              .through(t.publish),
            Stream.eval(server[IO](q, t, im, chatprotocol))
          ).parJoinUnbounded.compile.drain
        } yield s
      }
    }
  }

  override def run: IO[Unit] = program
}

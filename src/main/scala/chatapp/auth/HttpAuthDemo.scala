package auth

import cats.data.{Kleisli, OptionT}
import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.headers.Authorization
import org.http4s.implicits.*
import org.http4s.server.*

case class User(id: Long, name: String)

// 1 - basic authentication
object HttpAuthDemo extends IOApp.Simple {

  val routes: HttpRoutes[IO] =
    HttpRoutes.of {
      case GET -> Root / "welcome" / user => // localhost:8080/welcome/daniel
        Ok(s"Welcome, $user")
    }

  // simple auth method - basic
  // Request[IO] => IO[Either[String, User]]
  // Kleisli[IO, Request[IO], Either[String, User]]
  // Kleisli[F, A, B] equivalent to A => F[B]
  val basicAuthMethod = Kleisli.apply[IO, Request[IO], Either[String, User]] { req =>
    // auth logic
    val authHeader = req.headers.get[Authorization]
    authHeader match {
      case Some(Authorization(BasicCredentials(creds))) =>
        IO(Right(User(1L /* fetch from DB */, creds._1)))
        // check your own password
      case Some(_) => IO(Left("No basic credentials"))
      case None => IO(Left("Unauthorized!!"))
    }
  }

  val onFailure: AuthedRoutes[String, IO] = Kleisli { (req: AuthedRequest[IO, String]) =>
    OptionT.pure[IO](Response[IO](status = Status.Unauthorized))
  }

  // middleware
  val userBasicAuthMiddleware: AuthMiddleware[IO, User] = AuthMiddleware(basicAuthMethod, onFailure)

  val authRoutes = AuthedRoutes.of[User, IO] {
    case GET -> Root / "welcome" as user => // localhost:8080/welcome/daniel
      Ok(s"Welcome, $user") // business logic
  }

  val server: Resource[IO, Server] =
    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(userBasicAuthMiddleware(authRoutes).orNotFound)
      .build

  override def run: IO[Unit] =
    server.use(_ => IO.never).void
}

// 2 - digest authentication
object HttpDigestDemo extends IOApp.Simple {



  val server: Resource[IO, Server] =
    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(???)
      .build

  override def run: IO[Unit] =
    server.use(_ => IO.never).void
}


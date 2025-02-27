package chatapp.domain

import cats.Applicative
import cats.data.Validated
import chatapp.domain.validateutility.validateItem
import cats.syntax.all.*
import io.circe.Encoder

import java.util.UUID

object user {
  case class UserId(id: UUID) derives Encoder.AsObject
  case class UserName(name: String) derives Encoder.AsObject
  case class User(id: UserId, name: UserName) derives Encoder.AsObject {
    def toMap = Map((id.id.toString, name.name))
  }
  object User {
    def apply[F[_]: Applicative](
      id: UUID,
      name: String
    ): F[Validated[String, User]] =
      validateItem(name, User(UserId(id), UserName(name)), "User name")
        .pure[F]
  }
}

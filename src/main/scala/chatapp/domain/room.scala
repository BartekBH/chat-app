package chatapp.domain

import cats.Applicative
import cats.data.Validated
import chatapp.domain.validateutility.validateItem
import cats.syntax.all._

import java.util.UUID

object room {
  case class RoomId(id: UUID)
  case class RoomName(name: String)
  case class Room(id: RoomId, name: RoomName) {
    def toMap = Map((id.id.toString, name.name))
  }
  object Room {
    def apply[F[_]: Applicative](
      id: UUID,
      name: String
    ): F[Validated[String, Room]] =
      validateItem(name, Room(RoomId(id), RoomName(name)), "Room")
        .pure[F]
  }
}

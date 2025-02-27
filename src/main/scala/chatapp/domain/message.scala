package chatapp.domain

import chatapp.domain.room.RoomId
import chatapp.domain.user._

import java.time.LocalDateTime
import java.util.UUID

object message {
  case class MessageId(id: UUID)
  case class MessageText(value: String)
  case class MessageTime(time: LocalDateTime)
  case class InsertMessage(
    id: MessageId,
    value: MessageText,
    time: MessageTime,
    userId: UserId,
    roomId: RoomId
  )
  case class FetchMessage(value: MessageText, from: User)
}

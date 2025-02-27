package chatapp

import cats.data.Validated.{Invalid, Valid}
import cats.effect.std.UUIDGen
import cats.effect.{Concurrent, Resource}
import cats.syntax.all.*
import chatapp.SqlCommands.*
import chatapp.codecs.codesc.*
import chatapp.domain.message.*
import chatapp.domain.room.*
import chatapp.domain.user.*
import fs2.Stream
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import java.time.LocalDateTime

trait PostgresProtocol[F[_]] {
  def createUser(name: String): F[Either[String, User]]
  def createRoom(name: String): F[Either[String, Room]]
  def deleteRoom(roomId: RoomId): F[Unit]
  def deleteUser(userId: UserId): F[Unit]
  def saveMessage(
    message: String,
    time: LocalDateTime,
    userId: UserId,
    roomId: RoomId
  ): F[Unit]
  def fetchMessages(roomId: RoomId): F[Stream[F, FetchMessage]]
  def deleteRoomMessages(roomId: RoomId): F[Unit]
}

object PostgresProtocol {
  def make[F[_]: UUIDGen: Concurrent](postgres: Resource[F, Session[F]]): F[PostgresProtocol[F]] =
    postgres.use { session =>
      new PostgresProtocol[F] {
        override def createUser(name: String): F[Either[String, User]] =
          session.prepare(insertUser).flatMap { cmd =>
            UUIDGen.randomUUID.flatMap { id =>
              User(id, name).flatMap {
                case Valid(u) =>
                  cmd.execute(u).as(Right(u))
                case Invalid(err) =>
                  Left(err).pure[F]
              }
            }
          }

        override def createRoom(name: String): F[Either[String, Room]] =
          session.prepare(insertRoom).flatMap { cmd =>
            UUIDGen.randomUUID.flatMap { id =>
              Room(id, name).flatMap {
                case Valid(r) =>
                  cmd.execute(r).as(Right(r))
                case Invalid(err) =>
                  Left(err).pure[F]
              }
            }
          }

        private def deleteUserOrRoom[A](id: A, command: Command[A]): F[Unit] =
          session.prepare(command).flatMap { cmd =>
            cmd.execute(id).void
          }

        override def deleteUser(userId: UserId): F[Unit] =
          deleteUserOrRoom[UserId](userId, delUser)

        override def deleteRoom(roomId: RoomId): F[Unit] =
          deleteUserOrRoom[RoomId](roomId, delRoom)

        override def saveMessage(message: String, time: LocalDateTime, userId: UserId, roomId: RoomId): F[Unit] =
          session.prepare(insertMessage).flatMap { cmd =>
            UUIDGen.randomUUID.flatMap { id =>
              cmd
                .execute(
                  InsertMessage(
                    MessageId(id),
                    MessageText(message),
                    MessageTime(time),
                    userId,
                    roomId
                  )
                )
                .void
            }
          }

        override def fetchMessages(roomId: RoomId): F[Stream[F, FetchMessage]] =
          session.prepare(getMessage).map { cmd =>
            cmd.stream(roomId, 32)
          }

        override def deleteRoomMessages(roomId: RoomId): F[Unit] =
          session.prepare(delMessages).flatMap { cmd =>
            cmd.execute(roomId).void
          }
      }.pure[F]
    }
}

private object SqlCommands {
  val usercodec: Codec[User] =
    (userId ~ userName).imap {
      case id ~ name => User(id, name)
    }(user => (user.id, user.name))

  val roomcodec: Codec[Room] =
    (roomId ~ roomName).imap {
      case id ~ name => Room(id, name)
    }(room => (room.id, room.name))

  val insertUser: Command[User] =
    sql"""
         INSERT INTO users
         VALUES($usercodec)
       """
      .command

  val insertRoom: Command[Room] =
    sql"""
         INSERT INTO rooms
         VALUES($roomcodec)
       """
      .command

  val delUser: Command[UserId] =
    sql"""
         UPDATE users
         SET name = "deletedUser"
         WHERE id = $userId
       """
      .command

  val delRoom: Command[RoomId] =
    sql"""
        DELETE FROM rooms
        WHERE id = $roomId
    """
      .command

  val messagecodec: Codec[InsertMessage] =
    (messageId ~ messageText ~ messageTime ~ userId ~ roomId).imap {
      case mi ~ mt ~ mtm ~ ui ~ ri => InsertMessage(mi, mt, mtm, ui, ri)
    }(m => ((((m.id, m.value), m.time), m.userId), m.roomId))

  val insertMessage: Command[InsertMessage] =
    sql"""
         INSERT INTO messages
         VALUES($messagecodec)
       """
      .command

  val fetchmessagecodec: Codec[FetchMessage] =
    (messageText ~ userId ~ userName).imap {
      case mt ~ ui ~ un => FetchMessage(mt, User(ui, un))
    }(m => ((m.value, m.from.id), m.from.name))

  val getMessage: Query[RoomId, FetchMessage] =
    sql"""
         SELECT messages.message, users.id, users.name
         FROM messages
         INNER JOIN users
         ON messages.user_id = users.id
         WHERE messages.room_id = $roomId
       """
      .query(fetchmessagecodec)

  val delMessages: Command[RoomId] =
    sql"""
         DELETE FROM messages
         WHERE room_id = $roomId
       """
      .command
}


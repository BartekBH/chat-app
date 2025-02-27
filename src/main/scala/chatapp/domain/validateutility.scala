package chatapp.domain

import cats.data.Validated

object validateutility {
  def validateItem[F](
    value: String,
    userOrRoom: F,
    name: String
  ): Validated[String, F] = {
    Validated.cond(
      (value.length >= 2 && value.length <= 10),
      userOrRoom,
      s"$name must be between 2 and 10 characters"
    )
  }
}

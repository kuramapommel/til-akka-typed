package com.kuramapommel.til_akka_typed

import com.kuramapommel.til_akka_typed.UserRegistry.ActionPerformed

//#json-formats
import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol

object JsonFormats:
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol.*

  given userJsonFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
  given usersJsonFormat: RootJsonFormat[Users] = jsonFormat1(Users.apply)

  given actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed.apply)
//#json-formats

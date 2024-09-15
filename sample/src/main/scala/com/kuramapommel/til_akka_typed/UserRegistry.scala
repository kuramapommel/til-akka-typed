package com.kuramapommel.til_akka_typed

//#user-registry-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import scala.collection.immutable

//#user-case-classes
final case class User(name: String, age: Int, countryOfResidence: String)
final case class Users(users: immutable.Seq[User])
//#user-case-classes

object UserRegistry:
  // actor protocol
  enum Command:
    case GetUsers(replyTo: ActorRef[Users])
    case CreateUser(user: User, replyTo: ActorRef[ActionPerformed])
    case GetUser(name: String, replyTo: ActorRef[GetUserResponse])
    case DeleteUser(name: String, replyTo: ActorRef[ActionPerformed])

  final case class GetUserResponse(maybeUser: Option[User])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(users: Set[User]): Behavior[Command] =
    Behaviors.receiveMessage:
      case Command.GetUsers(replyTo) =>
        replyTo ! Users(users.toSeq)
        Behaviors.same
      case Command.CreateUser(user, replyTo) =>
        replyTo ! ActionPerformed(s"User ${user.name} created.")
        registry(users + user)
      case Command.GetUser(name, replyTo) =>
        replyTo ! GetUserResponse(users.find(_.name == name))
        Behaviors.same
      case Command.DeleteUser(name, replyTo) =>
        replyTo ! ActionPerformed(s"User $name deleted.")
        registry(users.filterNot(_.name == name))

//#user-registry-actor

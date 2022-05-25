package object model {

  case class User(id: Option[Long], name: String, surname: String)

  case class Author(id: Option[Long], name: String, surname: String)

  case class Book(id: Option[Long], name: String, author_id: Long, user_id: Option[Long])


  case object UserNotFoundError

  case object AuthorNotFoundError

  case object BookNotFoundError

}

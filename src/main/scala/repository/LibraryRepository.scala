package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import model.{User, Book, Author, UserNotFoundError, AuthorNotFoundError, BookNotFoundError}
import doobie._
import doobie.implicits._

class LibraryRepository(transactor: Transactor[IO]) {
  def getUsers: Stream[IO, User] = {
    sql"SELECT * FROM users".query[User].stream.transact(transactor)
  } 

  def getAuthors: Stream[IO, Author] = {
    sql"SELECT * FROM authors".query[Author].stream.transact(transactor)
  }

  def getBooks: Stream[IO, Book] = {
    sql"SELECT * FROM books".query[Book].stream.transact(transactor)
  } 

  def getUser(id: Long): IO[Either[UserNotFoundError.type, User]] = {
    sql"SELECT id, name, surname FROM users WHERE id = $id".query[User].option.transact(transactor).map {
      case Some(user) => Right(user)
      case None => Left(UserNotFoundError)
    }
  }

  def getBook(id: Long): IO[Either[BookNotFoundError.type, Book]] = {
    sql"SELECT * FROM books WHERE id = $id".query[Book].option.transact(transactor).map {
      case Some(book) => Right(book)
      case None => Left(BookNotFoundError)
    }
  }

  def getAuthor(id: Long): IO[Either[AuthorNotFoundError.type, Author]] = {
    sql"SELECT id, name, surname FROM authors WHERE id = $id".query[Author].option.transact(transactor).map {
      case Some(author) => Right(author)
      case None => Left(AuthorNotFoundError)
    }
  }


  def createUser(user: User): IO[User] = {
    sql"INSERT INTO users (name, surname) VALUES (${user.name}, ${user.surname})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      user.copy(id = Some(id))
    }
  }

  def createAuthor(author: Author): IO[Author] = {
    sql"INSERT INTO authors (name, surname) VALUES (${author.name}, ${author.surname})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      author.copy(id = Some(id))
    }
  }

  def createBook(book: Book): IO[Book] = {
    sql"INSERT INTO books (name, author_id) VALUES (${book.name}, ${book.author_id})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      book.copy(id = Some(id))
    }
  }

  def deleteUser(id: Long): IO[Either[UserNotFoundError.type, Unit]] = {
    sql"DELETE FROM users WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(UserNotFoundError)
      }
    }
  }

  def deleteAuthor(id: Long): IO[Either[AuthorNotFoundError.type, Unit]] = {
    sql"DELETE FROM authors WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(AuthorNotFoundError)
      }
    }
  }

  def deleteBook(id: Long): IO[Either[BookNotFoundError.type, Unit]] = {
    sql"DELETE FROM books WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(BookNotFoundError)
      }
    }
  }

  def updateUser(id: Long, user: User): IO[Either[UserNotFoundError.type, User]] = {
    sql"UPDATE users SET name = ${user.name}, surname = ${user.surname} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(user.copy(id = Some(id)))
      } else {
        Left(UserNotFoundError)
      }
    }
  }

  def updateAuthor(id: Long, author: Author): IO[Either[AuthorNotFoundError.type, Author]] = {
    sql"UPDATE authors SET name = ${author.name}, surname = ${author.surname} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(author.copy(id = Some(id)))
      } else {
        Left(AuthorNotFoundError)
      }
    }
  }

  def updateBook(id: Long, book: Book): IO[Either[BookNotFoundError.type, Book]] = {
    sql"UPDATE books SET name = ${book.name}, author_id = ${book.author_id}, user_id = ${book.user_id} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(book.copy(id = Some(id)))
      } else {
        Left(BookNotFoundError)
      }
    }
  }

}

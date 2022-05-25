package service

import cats.effect.IO
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import model.{User, Book, Author, UserNotFoundError, AuthorNotFoundError, BookNotFoundError}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{Location, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Uri}
import repository.LibraryRepository

class LibraryService(repository: LibraryRepository) extends Http4sDsl[IO] {

  val routes = HttpRoutes.of[IO] {
   
    case GET -> Root / "users" =>
      Ok(Stream("[") ++ repository.getUsers.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))
    
    case GET -> Root / "books" =>
      Ok(Stream("[") ++ repository.getBooks.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))
    
    case GET -> Root / "authors" =>
      Ok(Stream("[") ++ repository.getAuthors.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.application.json))
    
    case GET -> Root / "users" / LongVar(id) =>
      for {
        getResult <- repository.getUser(id)
        response <- userResult(getResult)
      } yield response
    
    case GET -> Root / "books" / LongVar(id) =>
      for {
        getResult <- repository.getBook(id)
        response <- bookResult(getResult)
      } yield response
    
    case GET -> Root / "authors" / LongVar(id) =>
      for {
        getResult <- repository.getAuthor(id)
        response <- authorResult(getResult)
      } yield response
    
    case req @ POST -> Root / "users" =>
      for {
        user <- req.decodeJson[User]
        createdUser <- repository.createUser(user)
        response <- Created(createdUser.asJson, Location(Uri.unsafeFromString(s"/users/${createdUser.id.get}")))
      } yield response
    
    case req @ POST -> Root / "authors" =>
      for {
        author <- req.decodeJson[Author]
        createdAuthor <- repository.createAuthor(author)
        response <- Created(createdAuthor.asJson, Location(Uri.unsafeFromString(s"/authors/${createdAuthor.id.get}")))
      } yield response

    case req @ POST -> Root / "books" =>
      for {
        book <- req.decodeJson[Book]
        createdBook <- repository.createBook(book)
        response <- Created(createdBook.asJson, Location(Uri.unsafeFromString(s"/books/${createdBook.id.get}")))
      } yield response
    
    case req @ PUT -> Root / "users" / LongVar(id) =>
      for {
        user <-req.decodeJson[User]
        updateResult <- repository.updateUser(id, user)
        response <- userResult(updateResult)
      } yield response
    
    case req @ PUT -> Root / "authors" / LongVar(id) =>
      for {
        author <-req.decodeJson[Author]
        updateResult <- repository.updateAuthor(id, author)
        response <- authorResult(updateResult)
      } yield response
    
    case req @ PUT -> Root / "books" / LongVar(id) =>
      for {
        book <-req.decodeJson[Book]
        updateResult <- repository.updateBook(id, book)
        response <- bookResult(updateResult)
      } yield response
    
    case DELETE -> Root / "users" / LongVar(id) =>
      repository.deleteUser(id).flatMap {
        case Left(UserNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
    
    case DELETE -> Root / "authors" / LongVar(id) =>
      repository.deleteAuthor(id).flatMap {
        case Left(AuthorNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
    
    case DELETE -> Root / "books" / LongVar(id) =>
      repository.deleteBook(id).flatMap {
        case Left(BookNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
    
    // case req @ POST -> Root / "books" / "read_csv" => 
    //   // for {
    //    req.decode[Multipart[F]] { m =>
    //       val filename = m.parts.flatMap(_.filename).mkString("\n")
    //       Ok(s"""Multipart Data\nParts:${m.parts.length}\n${m.parts.map(_.filename).mkString("\n")}""")
    //     }
    // // } yield responce
  }

  private def userResult(result: Either[UserNotFoundError.type, User]) = {
    result match {
      case Left(UserNotFoundError) => NotFound()
      case Right(user) => Ok(user.asJson)
    }
  }

  private def authorResult(result: Either[AuthorNotFoundError.type, Author]) = {
    result match {
      case Left(AuthorNotFoundError) => NotFound()
      case Right(author) => Ok(author.asJson)
    }
  }

  private def bookResult(result: Either[BookNotFoundError.type, Book]) = {
    result match {
      case Left(BookNotFoundError) => NotFound()
      case Right(book) => Ok(book.asJson)
    }
  }
}

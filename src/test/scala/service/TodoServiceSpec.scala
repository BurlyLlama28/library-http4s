package service

import cats.effect.IO
import fs2.Stream
import io.circe.Json
import io.circe.literal._
import model.{User, Book, Author, UserNotFoundError, AuthorNotFoundError, BookNotFoundError}
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import repository.LibraryRepository

class LibraryServiceSpec extends AnyWordSpec with MockFactory with Matchers {
  private val repository = stub[LibraryRepository]

  private val service = new LibraryService(repository).routes

  "LibraryService" should {

    // User part
    "create a user" in {
      val id = 1
      val user = User(None, "Bohdan", "Nadhob")
      (repository.createUser _).when(user).returns(IO.pure(user.copy(id = Some(id))))
      val createJson = json"""
        {
          "name": ${user.name},
          "surname": ${user.surname}
        }"""

      val response = serve(Request[IO](POST, uri"/users").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${user.name},
          "surname": ${user.surname}
        }"""
    }

    "update a user" in {
      val id = 1
      val user = User(None, "Bohdan", "Nadhob")
      (repository.updateUser _).when(id, user).returns(IO.pure(Right(user.copy(id = Some(id)))))
      val updateJson = json"""
        {
          "name": ${user.name},
          "surname": ${user.surname}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/users/$id")).withEntity(updateJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${user.name},
          "surname": ${user.surname}
        }"""
    }


    "return a single user" in {
      val id = 1
      val user = User(Some(id), "Bohdan", "Nadhob")
      (repository.getUser _).when(id).returns(IO.pure(Right(user)))

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/users/$id")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${user.name},
          "surname": ${user.surname}
        }"""
    }

    "return all users" in {
      val id1 = 1
      val user1 = User(Some(id1), "Bohdan", "Nadhob")
      val id2 = 2
      val user2 = User(Some(id2), "Oleg", "Gelo")
      val users = Stream(user1, user2)
      (() => repository.getUsers ).when().returns(users)

      val response = serve(Request[IO](GET, uri"/users"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        [
         {
            "id": $id1,
            "name": ${user1.name},
            "surname": ${user1.surname}
         },
         {
            "id": $id2,
            "name": ${user2.name},
            "surname": ${user2.surname}
         }
        ]"""
    }

    "delete a user" in {
      val id = 1
      (repository.deleteUser _).when(id).returns(IO.pure(Right(())))

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/users/$id")))
      response.status shouldBe Status.NoContent
    }

    // Author part
    "create a author" in {
      val id = 1
      val author = Author(None, "Bohdan", "Nadhob")
      (repository.createAuthor _).when(author).returns(IO.pure(author.copy(id = Some(id))))
      val createJson = json"""
        {
          "name": ${author.name},
          "surname": ${author.surname}
        }"""

      val response = serve(Request[IO](POST, uri"/authors").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${author.name},
          "surname": ${author.surname}
        }"""
    }

    "update a author" in {
      val id = 1
      val author = Author(None, "Bohdan", "Nadhob")
      (repository.updateAuthor _).when(id, author).returns(IO.pure(Right(author.copy(id = Some(id)))))
      val updateJson = json"""
        {
          "name": ${author.name},
          "surname": ${author.surname}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/authors/$id")).withEntity(updateJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${author.name},
          "surname": ${author.surname}
        }"""
    }


    "return a single author" in {
      val id = 1
      val author = Author(Some(id), "Bohdan", "Nadhob")
      (repository.getAuthor _).when(id).returns(IO.pure(Right(author)))

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/authors/$id")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${author.name},
          "surname": ${author.surname}
        }"""
    }

    "return all authors" in {
      val id1 = 1
      val author1 = Author(Some(id1), "Bohdan", "Nadhob")
      val id2 = 2
      val author2 = Author(Some(id2), "Oleg", "Gelo")
      val authors = Stream(author1, author2)
      (() => repository.getAuthors ).when().returns(authors)

      val response = serve(Request[IO](GET, uri"/authors"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        [
         {
            "id": $id1,
            "name": ${author1.name},
            "surname": ${author1.surname}
         },
         {
            "id": $id2,
            "name": ${author2.name},
            "surname": ${author2.surname}
         }
        ]"""
    }

    "delete a author" in {
      val id = 1
      (repository.deleteAuthor _).when(id).returns(IO.pure(Right(())))

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/authors/$id")))
      response.status shouldBe Status.NoContent
    }

    // Book part
    "create a book" in {
      val id = 1
      val book = Book(None, "Harry Potter", 1, Some(1))
      (repository.createBook _).when(book).returns(IO.pure(book.copy(id = Some(id))))
      val createJson = json"""
        {
          "name": ${book.name},
          "author_id": ${book.author_id},
          "user_id": ${book.user_id}
        }"""

      val response = serve(Request[IO](POST, uri"/books").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "name": ${book.name},
          "author_id": ${book.author_id},
          "user_id": ${book.user_id}
        }"""
    }

  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}

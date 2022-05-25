CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  description TEXT,
  importance TEXT
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  name TEXT,
  surname TEXT
);

CREATE TABLE books (
  id SERIAL PRIMARY KEY,
  name TEXT,
  author_id int8 NOT NULL,
  user_id int8
);

CREATE TABLE authors (
  id SERIAL PRIMARY KEY,
  name TEXT,
  surname TEXT
);

ALTER TABLE books
ADD CONSTRAINT BOOKS_AUTHOR_ID_FK
FOREIGN KEY (author_id) REFERENCES authors;

ALTER TABLE books
ADD CONSTRAINT BOOKS_USER_ID_FK
FOREIGN KEY (user_id) REFERENCES users;
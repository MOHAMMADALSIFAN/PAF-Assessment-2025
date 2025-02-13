drop database if exists movies;

create database movies;

use movies;

create table imdb(
  imdb_id varchar(16),
  vote_average float default 0,
  vote_count int default 0,
  release_date date,
  revenue decimal(15,2) default 1000000.00,
  budget decimal(15,2) default 1000000.00,
  runtime int default 90,

  constraint pk_imdb_id primary key (imdb_id)
);

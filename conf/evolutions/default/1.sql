# --- !Ups
CREATE TABLE TweetImages (
    id bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    filename varchar(255) NOT NULL
);

# --- !Downs

DROP TABLE TweetImages;
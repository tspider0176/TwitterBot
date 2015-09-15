#  Twitter Control App
For study Play framework and Slick.

## Usage
* Make twitter4j.properties in conf/.  
% touch twitter4j.properties
  
* Add following sentence.  
oauth.consumerKey={your consumer key}  
oauth.consumerSecret={your consumer secret}  
oauth.accessToken={your access token}  
oauth.accessTokenSecret={your access token secret}  
  
* Make sure to confirm database configuration in application.conf at conf/  
db.default.driver=com.mysql.jdbc.Driver  
db.default.url="jdbc:mysql://localhost/tweetimagedb"  
db.default.username=root  
db.default.password=""  
  
* MySQL server start.  
% mysql.server start  

* Launch MySQL console.  
% mysql -u \<user> -p \<password>  
* Create database.  
% CREATE DATABASE tweetimagedb;  
  
* Change cloned directory.  
% ./activator  
% run  
  
* After use, MySQL server stop.  
% mysql.server stop  
  
## Functions
* Display current user information  
* Tweet
* Tweet with random image in images/
* Auto followBack and auto remove

## Environment
* Scala 2.11.6
* Play framework 2.4.3
* MySQL 5.6.26 Homebrew
* mysql-connector-java 5.1.36
* twitter4j 4.0.4
* Slick 3.0.3
* play-slick 1.0.1

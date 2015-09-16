#  Twitter Control App
Very beginning of learning Scala, Play framework and Slick.  
Feel free to refer to this project. 

## Language
* Scala 2.11.6
  
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

---

* MySQL server start.  
% mysql.server start  

* Launch MySQL console.  
% mysql -u \<user> -p \<password>  
* Create database.  
% CREATE DATABASE tweetimagedb;  
* Exit MySQL console.
% exit;  

---

* Change project cloned directory.  
% ./activator  
  
* On activator console, run server  
% run  
  
* Access to below on web browser  
http://localhost:9000/  

---

* Server stop
% Ctrl-d  
  
* MySQL server stop.  
% mysql.server stop  
  
## Functions
* Display current user information  
* Tweet
* Delete tweet
* Tweet with random image in images/
* Get user timeline and reply to particular users
  -- In this code, users who tweet specific word.
* Auto followBack and auto remove
Details in routes in conf/ and Application.scala in app/

## Environment
* Play framework 2.4.3
* MySQL 5.6.26 Homebrew
* mysql-connector-java 5.1.36
* twitter4j 4.0.4
* Slick 3.0.3
* play-slick 1.0.1

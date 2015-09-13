#  Twitter Control App

## Usage
* Make twitter4j.properties in conf/.  
% touch twitter4j.properties
* Add following sentence.  
oauth.consumerKey={your consumer key}  
oauth.consumerSecret={your consumer secret}  
oauth.accessToken={your access token}  
oauth.accessTokenSecret={your access token secret}  
  
* Change cloning directory  
% ./activator  
% run  
  
* MySQL start  
% mysql.server start  
  
* MySQL stop  
% mysql.server stop  
  
## Functions
* Tweet
* Auto followBack and auto remove
* Display current user information
* Rundomly tweet with image in reserved folder

## Environment
* Scala 2.11.6
* Play framework 2.4.3
* MySQL 5.6.26 Homebrew
* mysql-connector-java 5.1.36
* twitter4j 4.0.4
* Slick 3.0.3
* play-slick 1.0.1

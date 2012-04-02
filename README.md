# cblog

A blogging software written in clojure. 

Next todos:

* <del>Refactor sandbar "layout" function to moustache tempalte</del>
* Write the persistent user sessions to mongodb
* <del>Datamodel for posts,...</del>
* S3 connection and binary data upload

Planned Features:

* Search Machine friendly URLs
* Posts (Pictures, Videos), Tags, Categories
* Comments, Askimet Integration
* ATOM, RSS feeds

This is still not production ready!

Why just another blog?

* Maybe my wordpress instance is going offline (server not longer available) in a few days or month
* Building reference projects for clojure
* Run it on Heroku & S3

Tech goal:

* High scalable
* No data stored at application servers
* Optional caching with memcached
* Binary data in S3, other data in MongoDB (sessions, posts, ...)
* "Reference" building for a clojure web stack

## Usage

git clone, ./run.sh

## License

Copyright (C) 2012 Martin Kremers

Distributed under the GPLv3, see http://www.gnu.org/licenses/gpl.html

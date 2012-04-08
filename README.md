# cblog
**This is still not production ready!**

![build status](https://secure.travis-ci.org/kremers/cblog.png?branch=master)

A blogging software written in clojure. 

Next todos:

* <del>Refactor sandbar "layout" function to moustache tempalte</del>
* <del>Datamodel for posts,...</del>
* <del>Simple functionality to add crud posts</del>
* Defensive bootstrap of database with default categories and users
* Settings section (configure blog name, admin password, ...)
* AdminUI, Output and Selection for categories
* S3 connection and binary data upload

Later:

* Write the persistent user sessions to mongodb, currently existing frameworks do not work with monger and clojure 1.3. Fork and refactor them
* Media Section
* User Management
* Enhance AdminUI with more ajax feeling & (Web-)design changes
* Tag Cloud

Planned Features:

* Search Machine friendly URLs
* Posts (Pictures, Videos), Tags, Categories
* Comments, Askimet Integration
* ATOM, RSS feeds

Why just another blog?

* Maybe my wordpress instance is going offline (server not longer available) in a few days or month
* Building reference projects for clojure
* Run it on Heroku & S3

Tech goal:

* High scalable
* No data stored on application servers
* Optional caching with memcached
* Binary data in S3, other data in MongoDB (sessions, posts, ...)
* "Reference" building for a clojure web stacks

## Usage

git clone, ./run.sh

## License

Copyright (C) 2012 Martin Kremers

Distributed under the GPLv3, see http://www.gnu.org/licenses/gpl.html

# cblog
Version 1.0.0 and instructions for the basic setup will be released soon. 

DEMO: http://www.martinkremers.de

Local setup: 

1. Install MongoDB on localhost
2. git clone & ./run.sh
3. Browse "/bootstrap"
4. Browse "/admin" and login with default credentials admin:admin
5. Change admin password
6. Enter amazon s3 credentials in Settings section
 
Heroku setup:

1. git push to heroku
2. setup mongohq & logging for the application
3. Next steps see local setup 3.ff

Super-awesome features:

- No local storage, using amazon s3
- No local session, using mongodb session adapter
- No local data,    using mongodb 
- Markdown for post editing
- Tagcloud, Image minifier,...

Blogging software written in clojure. 

Next todos:

* <del>Configurable Blogroll</del>
* <del>Tags & Tag Cloud</del>
* <del>Add fancy ajax response in media section (delete,...)</del>
* <del>Slugify s3 key names (spaces cause errors,...)</del>
* <del>Thumbnail generation for images</del>
* <del>S3 connection and binary data upload & Media Section</del>
* <del>RSS feed (RSS & Atom templates for mustache are available)</del>
* <del>Fix link to recent posts</del>
* <del>Refactor sandbar "layout" function to moustache tempalte</del>
* <del>Datamodel for posts,...</del>
* <del>Simple functionality to add crud posts</del>
* <del>Defensive bootstrap of database with default categories, settings and users</del>
* <del>CRUD for categories</del>
* <del>Categories with URLfriendly name</del>
* <del>Routes to view categories and posts by /:category</del> and /:category/:post
* <del>AdminUI, Output and Selection for categories</del>
* <del>HideTitle Option for posts</del>
* <del>Settings section (configure blog name, admin password, ...)</del>
* <del>Enhance AdminUI with more ajax feeling & (Web-)design changes</del>
* <del>Fix broken build (heroku and 'local runnable' app - see core.clj)</del>
* <del>Fix broken links from adminui</del>
* <del>Change password functionality - settings page</del>
* <del>Automatic (client side) generation of URLfriendly name</del>
* <del>Print application health on admin welcome page.  Runtime/getRuntime ...</del>
* <del>Fix broken links "recent posts"</del>
* <del>Fix "number does not work as password"</del>
* <del>Write the persistent user sessions to mongodb, currently existing frameworks do not work with monger and clojure 1.3. Fork and refactor them</del>
* Image include functionality for wysiwyg editor
* A lot error handling stuff (messages on admin overview) - missing settings,...
* Move this page to a todo page and write instructions how to install this blog *maybe
* Implement cache with clojure.core.cache *maybe
* Minify javascript

After initial release:
* Write a session timeout (check monger-session for date values)
* Comments, Askimet Integration

Tech goal:

* Building reference projects for clojure
* High scalable
* No data stored on application servers
* Run it on Heroku & S3
* Optional caching with memcached
* Binary data in S3, other data in MongoDB (sessions, posts, ...)
* "Reference" building for a clojure web stacks

Ideas: 

* More Info on admin welcome page (server load, database stats)

## Usage

git clone, ./run.sh

## License

Copyright (C) 2012 Martin Kremers

Distributed under the GPLv3, see http://www.gnu.org/licenses/gpl.html

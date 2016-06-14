# Geo_Beers_Wars
Vert.X server with MySQL database for Geo Beers Wars web app

Geo Beers Wars is a game when you have to win pubs in your town with your friends.
You play with your team and need to be the more and the longuer in a pub to win. You can also chat on the app.

HTTP Server:
* host: localhost
* port: 5169

MySQL Database settings preffered (Configuration in server.initDB()):
* "host": "localhost"
* "port":3306
* "username":"root"
* "password":...
* "database":"geo_beers_wars"


Requests:

Get chat_table:
* GET http://localhost:5169/api/chat

Add chat to the chat_table:
* POST http://localhost:5169/api/pub
* body: {"from":"web", "to":"Khaaaaaa", "message": "What's up?"}

Get pub_table:
* GET http://localhost:5169/api/pub

Add pub to pub_table
* POST http://localhost:5169/api/pub
* body: {"latitude":"69.696969", "longitude":"51.515151", "icon": "red"}

Update pub
* PUT http://localhost:5169/api/pub/latitude (http://localhost:5169/api/pub/2.2 for latitude 2.2)
* body: {"longitude":2.2, "icon": "rouge"}

Get pub ID
* POST http://localhost:5169/api/pubId
* body: {"latitude": "69.697", "longitude": "69.697"}

Get user_table:
* GET http://localhost:5169/api/user

Add user to user_table
* POST http://localhost:5169/api/user
* body: {"username":"Khaaaaaa", "password":"rate", "team": "rouge", "last_id_pub": 1}

Update user
* PUT http://localhost:5169/api/user/id (http://localhost:5169/api/user/1 for id 1)
* body: {"team":"bleu", "last_id_pub":"2"}

Get team score
* GET http://localhost:5169/api/team

POST request will response with Json Object created in MySQL databse

chat_table:
* id
* personFrom
* personTo
* message
* date

pub_table:
* id
* latitude
* longitude
* icon

user_table:
* id
* username
* password
* team
* last_id_pub
* score
* last_time

Team:
* Bleu
* Rouge
* Rose
* Vert

If Intellij suddenly throwing ClassNotFoundException
* File --> Project Structure --> Modules
* Dependencies & add slf4J-api-1.X.XX (see utils folder)

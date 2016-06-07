# Geo_Beers_Wars
MySQL Server for Geo Beers Wars web app

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
* "database":"geo_beers_wars


Requests:

Get chat_table:
GET http://localhost:5169/api/chat

Get pub_table:
GET http://localhost:5169/api/chat

Add chat to the chat_table:
POST http://localhost:5169/api/pub
body: {"from":"web", "to":"Khaaaaaa", "message": "What's up?"}

Add chat to the chat_table
POST http://localhost:5169/api/pub
body: {"latitude":"69.696969", "longitude":"51.515151", "icon": "red"}

POST request will response with Json Object created in MySQL databse

If Intellij suddenly throwing ClassNotFoundException
* File --> Project Structure --> Modules
* Dependencies & add slf4J-api-1.X.XX (see utils folder)

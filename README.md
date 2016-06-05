# Geo_Beers_Wars
MySQL Server for Geo Beers Wars web app

Geo Beers Wars is a game when you have to win pubs in your town with your friends.
You play with your team and need to be the more and the longuer in a pub to win. You can also chat on the app.

MySQL Database settings preffered (password and port are asked when you use server.initDB()):
* "host": "localhost"
* "port":3306
* "username":"root"
* "password":...
* "database":"geo_beers_wars

If Intellij suddenly throwing ClassNotFoundException
* File --> Project Structure --> Modules
* Dependencies & add slf4J-api-1.X.XX (see utils folder)

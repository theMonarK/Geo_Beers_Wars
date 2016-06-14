package org.khaaaaaa.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.khaaaaaa.vertx.model.Message;
import org.khaaaaaa.vertx.model.Pub;
import org.khaaaaaa.vertx.model.User;

import java.util.List;


/**
 * Created by anthony on 31/05/2016.
 */
public class Server extends AbstractVerticle {

    private JsonObject mySQLClientConfig;
    private AsyncSQLClient mySQLClient;

    /*
    Start HTTP server and route GET /api/chat to have JsonArray of chat_table
     */

    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);
        // Bind "/" to the greatest
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Khaaaaaa is great! Fear Khaaaaaa</h1>");
        });

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .listen(5169);
        // Bind "/api/chat" to the chat_table
        router.get("/api/chat").handler(this::getChatTable);

        router.route("/api/chat*").handler(BodyHandler.create());
        router.post("/api/chat").handler(this::addChatMsg);

        // Bind "/api/pub" to the pub_table
        router.get("/api/pub").handler(this::getPubTable);

        router.route("/api/pub*").handler(BodyHandler.create());
        router.post("/api/pub").handler(this::addPub);
        router.put("/api/pub/:id").handler(this::updatePub);

        // Bind "/api/pub" to the pub_table
        router.get("/api/user").handler(this::getUserTable);

        router.route("/api/user*").handler(BodyHandler.create());
        router.post("/api/user").handler(this::addUser);
        router.put("/api/user/:id").handler(this::updateUser);

        // Bind "/api/chat" to the chat_table
        router.get("/api/team").handler(this::getTeamScore);

        // Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));
    }

    /*
    Configure MySQL Database and crate chat_table if not exits.
     */
    private void initDB() {
        this.mySQLClientConfig = new JsonObject()
                .put("host", "localhost")
                .put("port", 3306)
                .put("username", "root")
                .put("password", "vor9060sj")
                .put("database", "geo_beers_wars");
        this.mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
        this.createPubTable();
        this.createChatTable();
        this.createUserTable();

        vertx.setPeriodic(300000, id -> {
            // This handler will get called every 5 minutes
            System.out.println("Update user db");
            this.updateScore();
        });
    }

    /*
    Create chat_table in the MySQL DB if doesn't already exist
     */

    private void createChatTable(){

        String sqlChat = "CREATE TABLE IF NOT EXISTS `chat_table` (" +
                "  `id` int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "  `personFrom` VARCHAR(32) NOT NULL," +
                "  `personTo` VARCHAR(32) NOT NULL," +
                "  `message`  TEXT(150) NOT NULL," +
                "  `date` DATETIME NOT NULL);";

        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");

                //Create chat_table
                connection.execute(sqlChat, execute -> {
                    if (execute.succeeded()) {
                        System.out.println("Chat Table created !");
                    } else {
                        System.out.println("Chat table failed !");
                    }
                });

                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                this.connexionFailed();
            }
        });
    }

    private void createUserTable(){

        String sqlUser = "CREATE TABLE IF NOT EXISTS `user_table` (" +
                "  `id` int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "  `username` VARCHAR(32) NOT NULL," +
                "  `password` VARCHAR(32) NOT NULL," +
                "  `team`  VARCHAR(32) NOT NULL," +
                "  `last_id_pub` INT NOT NULL," +
                "  `score` INT NULL," +
                "  `last_time` DATETIME NOT NULL);";

        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");

                //Create chat_table
                connection.execute(sqlUser, execute -> {
                    if (execute.succeeded()) {
                        System.out.println("User Table created !");
                    } else {
                        System.out.println("User table failed !");
                    }
                });

                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                this.connexionFailed();
            }
        });
    }

    private void getTeamScore(RoutingContext routingContext){

        this.initDB();
        this.mySQLClient.getConnection(resConnection -> {

            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT team, SUM(u.score) FROM user_table u GROUP BY u.team;", resSelectTeam -> {

                    if (resSelectTeam.succeeded()) {
                        System.out.print("Select Team_score\n");

                        // Encode JsonArray of the chat_table and send it
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily(resSelectTeam.result().toJson().getMap().get("rows")));
                        }else{
                            System.out.print("Select team score failed\n");
                            routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "text/html")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily("Select team score failed"));
                    }
                });
                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end(this.connexionFailed());
            }
        });

    }

    private void createPubTable(){

        String sqlPub = "CREATE TABLE IF NOT EXISTS `pub_table` (" +
                "  `id` int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "  `latitude` FLOAT NULL," +
                "  `longitude` FLOAT NULL," +
                "  `icon`  LONGTEXT NOT NULL);";


        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");

                //Create pubs_table
                connection.execute(sqlPub, execute -> {
                    if (execute.succeeded()) {
                        System.out.println("Pub table created !");
                    } else {
                        System.out.println("Pub table failed !");
                    }
                });

                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                this.connexionFailed();
            }
        });
    }


    /*
    Test if the connexion with MySQL database can be established
     */
    public void testConnectionDB(){
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {

                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                System.out.print("Connexion failed\n");
            }
        });
    }

    /*
    Return chat_table from geo_beers_wars database
     */
    private void getChatTable(RoutingContext routingContext){

        //Need to initDB or this.mySQLClient = null
        //initDB();
        this.mySQLClient.getConnection(resConnection -> {

            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT * FROM geo_beers_wars.chat_table;", resSelectChat->{

                    if(resSelectChat.succeeded()) {
                        System.out.print("Select chat_table\n");

                        // Encode JsonArray of the chat_table and send it
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily(resSelectChat.result().toJson().getMap().get("rows")));
                    }
                    else{
                        System.out.print("Select chat_table failed\n");
                        routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "text/html")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily("Select chat_table failed"));
                    }
                });
                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end(this.connexionFailed());
            }
        });
    }

    /*
   Return user_table from geo_beers_wars database
    */
    private void getUserTable(RoutingContext routingContext){

        //Need to initDB or this.mySQLClient = null
        //initDB();
        this.mySQLClient.getConnection(resConnection -> {

            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT * FROM geo_beers_wars.user_table;", resSelectUser->{

                    if(resSelectUser.succeeded()) {
                        System.out.print("Select user_table\n");

                        // Encode JsonArray of the chat_table and send it
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily(resSelectUser.result().toJson().getMap().get("rows")));
                    }
                    else{
                        System.out.print("Select user_table failed\n");
                        routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "text/html")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily("Select user_table failed"));
                    }
                });
                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end(this.connexionFailed());
            }
        });
    }

    /*
   Return user_table from geo_beers_wars database
    */
    private void updateScore(){

        //Need to initDB or this.mySQLClient = null
        this.mySQLClient.getConnection(resConnection -> {

            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT id,score FROM geo_beers_wars.user_table WHERE last_time >= NOW() - INTERVAL 35 MINUTE;", resSelectUser->{

                    if(resSelectUser.succeeded()) {


                        List<JsonObject> updateList = resSelectUser.result().getRows();
                        for (JsonObject update:updateList) {
                            JsonArray params = new JsonArray().add(update.getInteger("score")+1)
                                    .add(update.getInteger("id"));
                            String sqlUpdate = "UPDATE user_table SET score = ? WHERE id=?";
                            connection.updateWithParams(sqlUpdate, params, resUpdate -> {
                                if (resUpdate.succeeded()) {
                                    System.out.print("User update\n");
                                    connection.close();
                                    System.out.print("Connexion closed\n");
                                }

                                //Sending error response
                                else {
                                    System.out.print("User update failed\n");
                                }
                            });
                        }
                    }
                    else{
                        System.out.print("Update failed\n");
                    }
                });
                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                System.out.print("Connexion failed\n");
            }
        });

    }

    /*
    Return pub_table from geo_beers_wars database
     */
    private void getPubTable(RoutingContext routingContext){

        //Need to initDB or this.mySQLClient = null
        initDB();
        this.mySQLClient.getConnection(resConnection -> {

            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT * FROM geo_beers_wars.pub_table;", resSelectPub->{

                    if(resSelectPub.succeeded()) {
                        System.out.print("Select pub_table\n");

                        // Encode JsonArray of the chat_table and send it
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily(resSelectPub.result().toJson().getMap().get("rows")));
                    }
                    else{
                        System.out.print("Select pub_table failed\n");
                        routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "text/html")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily("Select pub_table failed"));
                    }
                });
                connection.close();
                System.out.print("Connexion closed\n");

            } else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end(this.connexionFailed());
            }
        });
    }

    /*
    Add a message to the database. personFrom, personTo and message needed. ID and Date generated automatically
     */
    private void addChatMsg(RoutingContext routingContext) {

        final Message message = Json.decodeValue(routingContext.getBodyAsString(),Message.class);
        String sql = "INSERT INTO chat_table VALUES (?, ?, ?, ?, ?);";
        JsonArray params = new JsonArray().addNull()
                .add(message.getFrom())
                .add(message.getTo())
                .add(message.getMessage())
                .add(getDate());

        initDB();
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sql, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("Chat message added\n");
                        message.setId(resUpdate.result().getKeys().getInteger(0));

                        //Sending the message as Json and code 201 (CREATED)
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(message));
                        connection.close();
                        System.out.print("Connexion closed\n");
                    }

                    //Sending error response
                    else{
                        System.out.print("Chat message failed\n");
                        routingContext.response()
                                .setStatusCode(503)
                                .putHeader("content-type", "text/html")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily("Service Unavailable"));
                    }
                });

                //Sending error response
            }else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end(this.connexionFailed());
            }
        });
    }


    /*
    Add a pub to the database. latitude, longitude and icon needed. ID generated automatically
     */

    private void addPub(RoutingContext routingContext) {

        Pub pub = Json.decodeValue(routingContext.getBodyAsString(),Pub.class);
        String sql = "INSERT INTO pub_table VALUES (?, ?, ?, ?);";

        // Normalize icon name base for the db
        pub.setIcon(this.normalizeIcon(pub.getIcon()));
        JsonArray params = new JsonArray().addNull()
                .add(pub.getLatitude())
                .add(pub.getLongitude())
                .add(pub.getIcon());

        initDB();
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sql, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("Pub added\n");
                        pub.setId(resUpdate.result().getKeys().getInteger(0));

                        //Sending the message as Json and code 201 (CREATED)
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily(pub));
                        connection.close();
                        System.out.print("Connexion closed\n");
                    }

                    //Sending error response
                    else{
                        System.out.print("Add pub failed\n");
                        routingContext.response()
                                .setStatusCode(503)
                                .putHeader("content-type", "text/html")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily("Service Unavailable"));
                    }
                });

                //Sending error response
            }else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .end(this.connexionFailed());
            }
        });
    }

    /*
    Add a pub to the database. latitude, longitude and icon needed. ID generated automatically
     */

    private void addUser(RoutingContext routingContext) {

        final User user = Json.decodeValue(routingContext.getBodyAsString(),User.class);
        String sql = "INSERT INTO user_table VALUES (?, ?, ?, ?, ?, ?);";

        JsonArray params = new JsonArray().addNull()
                .add(user.getUsername())
                .add(user.getPassword())
                .add(user.getTeam())
                .add(user.getLast_id_pub())
                .add(getDate());

        initDB();
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sql, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("User added\n");
                        user.setId(resUpdate.result().getKeys().getInteger(0));

                        //Sending the message as Json and code 201 (CREATED)
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .end(Json.encodePrettily(user));
                        connection.close();
                        System.out.print("Connexion closed\n");
                    }

                    //Sending error response
                    else{
                        System.out.print("Add user failed\n");
                        routingContext.response()
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .setStatusCode(503)
                                .putHeader("content-type", "text/html")
                                .end(Json.encodePrettily("Service Unavailable"));
                    }
                });

                //Sending error response
            }else {
                routingContext.response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .end(this.connexionFailed());
            }
        });
    }

    private void updatePub(RoutingContext routingContext) {

        final String id = routingContext.request().getParam("id");
        JsonObject json = routingContext.getBodyAsJson();
        String sqlUpdate = "UPDATE pub_table SET latitude = ?, longitude=?, icon=? WHERE id=?";
        JsonArray params = new JsonArray().add(json.getString("latitude"))
                .add(json.getString("longitude"))
                .add(this.normalizeIcon(json.getString("icon")))
                .add(id);

        initDB();
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sqlUpdate, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("Pub updated\n");

                        //Sending the message as Json and code 201 (CREATED)
                        routingContext.response()
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(params));
                        connection.close();
                        System.out.print("Connexion closed\n");
                    }

                    //Sending error response
                    else{
                        System.out.print("Pub update failed\n");
                        routingContext.response()
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .setStatusCode(503)
                                .putHeader("content-type", "text/html")
                                .end(Json.encodePrettily("Service Unavailable"));
                    }
                });

                //Sending error response
            }else {
                routingContext.response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .end(this.connexionFailed());
            }
        });
    }

    private void updateUser(RoutingContext routingContext) {

        final String id = routingContext.request().getParam("id");
        JsonObject json = routingContext.getBodyAsJson();
        String sqlUpdate = "UPDATE user_table SET team = ?, last_id_pub=?, last_time=? WHERE id=?";
        JsonArray params = new JsonArray().add(json.getString("team"))
                .add(json.getInteger("last_id_pub"))
                .add(this.getDate())
                .add(id);

        initDB();
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sqlUpdate, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("User updated\n");

                        //Sending the message as Json and code 201 (CREATED)
                        routingContext.response()
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(params));
                        connection.close();
                        System.out.print("Connexion closed\n");
                    }

                    //Sending error response
                    else{
                        System.out.print("User update failed\n");
                        routingContext.response()
                                .putHeader("Access-Control-Allow-Origin", "*")
                                .setStatusCode(503)
                                .putHeader("content-type", "text/html")
                                .end(Json.encodePrettily("Service Unavailable"));
                    }
                });

                //Sending error response
            }else {
                routingContext.response()
                        .putHeader("Access-Control-Allow-Origin", "*")
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .end(this.connexionFailed());
            }
        });
    }




    /*
    Date generator for database
     */
    private String getDate(){
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }

    /*
    Generate request error for wrong connexion
     */

    private String connexionFailed(){
        return "Connexion failed for:<br>" +
                "host: "+mySQLClientConfig.getString("host")+"<br>"+
                "port: "+mySQLClientConfig.getInteger("port").toString()+"<br>"+
                "username: "+mySQLClientConfig.getString("username")+"<br>"+
                "password: "+mySQLClientConfig.getString("password")+"<br>"+
                "database: "+mySQLClientConfig.getString("database")+"<br>";
    }

    private String normalizeIcon(String icon){
        switch(icon){
            case "rouge":
                return ".../img/rouge.png";
            case "rose":
                return ".../img/rose.png";
            case "bleu":
                return ".../img/bleu.png";
            case "vert":
                return ".../img/vert.png";
        }
        return ".../img/rouge.png";
    }
}

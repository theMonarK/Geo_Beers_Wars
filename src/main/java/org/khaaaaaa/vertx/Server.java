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
import org.khaaaaaa.vertx.model.Message;


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

            } else {
                this.connexionFailed();
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

            } else {
                this.connexionFailed();
            }
        });
    }

    /*
    Test if the connexion with the MySQL database can be established
     */
    public void testConnectionDB(){
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {

                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.close();

            } else {
                this.connexionFailed();
            }
        });
    }

    /*
    Return chat_table from geo_beers_wars database
     */
    private void getChatTable(RoutingContext routingContext){

        //Need to initDB or this.mySQLClient = null
        initDB();
        this.mySQLClient.getConnection(resConnection -> {

            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT * FROM geo_beers_wars.chat_table;", resSelectChat->{

                    if(resSelectChat.succeeded()) {
                        System.out.print("Select chat_table worked\n");

                        // Encode JsonArray of the chat_table and send it
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(resSelectChat.result().toJson().getMap().get("rows")));
                    }
                    else{
                        System.out.print("Select chat_table failed\n");
                        routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "text/html")
                                .end(Json.encodePrettily("Select chat_table failed"));
                    }
                });
                connection.close();

            } else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .end(this.connexionFailed());
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
                        System.out.print("Select pub_table worked\n");

                        // Encode JsonArray of the chat_table and send it
                        routingContext.response()
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(resSelectPub.result().toJson().getMap().get("rows")));
                    }
                    else{
                        System.out.print("Select pub_table failed\n");
                        routingContext.response()
                                .setStatusCode(400)
                                .putHeader("content-type", "text/html")
                                .end(Json.encodePrettily("Select pub_table failed"));
                    }
                });
                connection.close();

            } else {
                routingContext.response()
                        .setStatusCode(400)
                        .putHeader("content-type", "text/html")
                        .end(this.connexionFailed());
            }
        });
    }

    /*
    Add a message to the database. personFrom, personTo and message needed. ID and Date generated automatically
     */
    public void addChatMsg(RoutingContext routingContext) {

        final Message message = Json.decodeValue(routingContext.getBodyAsString(),Message.class);
        String sql = "INSERT INTO chat_table VALUES (?, ?, ?, ?, ?);";
        JsonArray params = new JsonArray().addNull()
                .add(message.getFrom())
                .add(message.getTo())
                .add(message.getMessage())
                .add(getDate());

        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sql, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("Chat message added\n");

                        //Sending the message as Json and code 201 (CREATED)
                        routingContext.response()
                                .setStatusCode(201)
                                .putHeader("content-type", "application/json; charset=utf-8")
                                .end(Json.encodePrettily(message));
                        connection.close();
                    }

                    //Sending error response
                    else{
                        System.out.print("Chat message failed\n");
                        routingContext.response()
                                .setStatusCode(503)
                                .putHeader("content-type", "text/html")
                                .end(Json.encodePrettily("Service Unavailable"));
                    }
                });

                //Sending error response
            }else {
                routingContext.response()
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
        String fail = ("Connexion failed for:<br>" +
                "host: "+mySQLClientConfig.getString("host")+"<br>"+
                "port: "+mySQLClientConfig.getInteger("port").toString()+"<br>"+
                "username: "+mySQLClientConfig.getString("username")+"<br>"+
                "password: "+mySQLClientConfig.getString("password")+"<br>"+
                "database: "+mySQLClientConfig.getString("database")+"<br>"
        );
        return fail;
    }
}

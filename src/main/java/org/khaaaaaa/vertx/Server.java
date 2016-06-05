package org.khaaaaaa.vertx;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLConnection;


/**
 * Created by anthony on 31/05/2016.
 */
public class Server {

    private JsonObject mySQLClientConfig;
    private AsyncSQLClient mySQLClient;


    /*
    Configure MySQL Database and crate chat_table if not exits.
     */
    public void initDB(int port,String password){
         this.mySQLClientConfig = new JsonObject()
                .put("host", "localhost")
                .put("port",port)
                .put("username","root")
                .put("password",password)
                .put("database","geo_beers_wars");
        Vertx vertx = Vertx.vertx();
        this.mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);

        String sql = "CREATE TABLE IF NOT EXISTS `chat_table` (" +
                "  `id` int(11) PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "  `personFrom` VARCHAR(32) NOT NULL," +
                "  `personTo` VARCHAR(32) NOT NULL," +
                "  `message`  TEXT(150) NOT NULL," +
                "  `date` DATETIME NOT NULL);";

        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");

                connection.execute(sql, execute -> {
                    if (execute.succeeded()) {
                        System.out.println("Table initialized !");
                    } else {
                        System.out.println("Create table failed !");
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
    public JsonArray[] getChatTable(){
        final JsonArray[] chatTableJson = {new JsonArray()};
        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.query("SELECT * FROM geo_beers_wars.chat_table;", resSelectChat->{
                    if(resSelectChat.succeeded()) {
                        System.out.print("Select chat_table worked\n");
                        chatTableJson[0] = (JsonArray) resSelectChat.result().toJson().getMap().get("rows");
                    }
                    else{
                        System.out.print("Select chat_table failed\n");
                    }
                });
                connection.close();

            } else {
                this.connexionFailed();
            }
        });
        return chatTableJson;
    }

    /*
    Add a message to the database. personFrom, personTo and message needed. ID and Date generated automatically
     */
    public void addChatMsg(String from,String to,String message) {
        String sql = "INSERT INTO chat_table VALUES (?, ?, ?, ?, ?);";
        JsonArray params = new JsonArray().addNull()
                .add(from)
                .add(to)
                .add(message)
                .add(getDate());

        this.mySQLClient.getConnection(resConnection -> {
            if (resConnection.succeeded()) {
                SQLConnection connection = resConnection.result();
                System.out.print("Connexion established\n");
                connection.updateWithParams(sql, params, resUpdate -> {
                    if (resUpdate.succeeded()) {
                        System.out.print("Chat message added\n");
                        connection.close();
                    }
                    else{
                        System.out.print("Chat message failed\n");
                    }
                });
            }else {
                this.connexionFailed();
            }
        });
    }

    /*
    Date generator for database
     */
    public String getDate(){
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }

    private void connexionFailed(){
        System.out.print("Connexion failed for:\n" +
                "host: "+mySQLClientConfig.getString("host")+"\n"+
                "port: "+mySQLClientConfig.getInteger("port").toString()+"\n"+
                "username: "+mySQLClientConfig.getString("username")+"\n"+
                "password: "+mySQLClientConfig.getString("password")+"\n"+
                "database: "+mySQLClientConfig.getString("database")+"\n"
        );
    }
}

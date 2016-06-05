package org.khaaaaaa.vertx;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by anthony on 31/05/2016.
 */

/*
* If Intellij suddenly throwing ClassNotFoundException
* File --> Project Structure --> Modules
* Dependencies & add slf4J-api-1.X.XX (see utils folder)
* */
public class Main {

    public static void main(String... args) {

        Server server = new Server();
        server.initDB(3306,"password");
        //server.testConnectionDB();
        //JsonArray[] chatTable = server.getChatTable();
        server.addChatMsg("Dude","Test","Come on!");

    }

}

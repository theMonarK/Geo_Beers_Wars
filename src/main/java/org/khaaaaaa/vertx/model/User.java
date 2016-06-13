package org.khaaaaaa.vertx.model;

/**
 * Created by anthony on 13/06/2016.
 */
public class User {

    private int id;
    private String username;
    private String password;
    private String team;
    private int last_id_pub;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public int getLast_id_pub() {
        return last_id_pub;
    }

    public void setLast_id_pub(int last_id_pub) {
        this.last_id_pub = last_id_pub;
    }
}

package jp.takuji31.metro.example;

/**
 * Created by takuji on 2017/04/22.
 */

public class User {
    private long id;
    private String name;
    private String screenName;

    public User() {
    }

    public User(long id, String name, String screenName) {
        this.id = id;
        this.name = name;
        this.screenName = screenName;
    }
}

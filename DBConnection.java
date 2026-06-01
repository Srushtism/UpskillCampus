package musicplayer;

import java.sql.*;

public class DBConnection {

    public static Connection getConnection() throws Exception {

        Class.forName("org.sqlite.JDBC");

        return DriverManager.getConnection(
                "jdbc:sqlite:musicplayer.db");
    }

    public static void createTables() {

        try (
            Connection con = getConnection();
            Statement st = con.createStatement()
        ) {

            st.execute(
                "CREATE TABLE IF NOT EXISTS songs("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT,"
                + "artist TEXT,"
                + "path TEXT)"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS playlists("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT UNIQUE)"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS playlist_songs("
                + "playlist_id INTEGER,"
                + "song_id INTEGER)"
            );

            System.out.println("Tables Created");

        } catch(Exception e) {

            e.printStackTrace();
        }
    }
}
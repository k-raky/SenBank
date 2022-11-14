package models;

import java.sql.*;
import java.util.Date;

public class Password {

    public static Connection connectToDB() {

        String url = "jdbc:mysql://194.163.133.64/jsit";
        //String url = "jdbc:mysql://localhost/jsit";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, "desktop", "jPB)fn5YKgHm");
            //conn = DriverManager.getConnection(url, "raky", "passer");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Database connection established");
        return conn;
    }

    public static void updatePassword(String password) {

        Connection conn = connectToDB();
        String lock = "lock table desktop_passwords write";
        String query = "Update desktop_passwords set value=?, updated_at=? where id=1";
        String unlock = "unlock tables";
        try {
            Statement lockStatement = conn.createStatement();
            lockStatement.execute(lock);
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, password);
            statement.setTimestamp(2, new Timestamp(new Date().getTime()));
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("password updated !");
            }
            lockStatement.execute(unlock);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}

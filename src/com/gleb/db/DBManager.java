package com.gleb.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    public static final String url = "jdbc:mysql://localhost:3306/payment";
    public static final String user = "db_admin";
    public static final String passwd = "admin_password";

    public static void startDB() {
        Connection cn;

        try {
            cn = DriverManager.getConnection(DBManager.url, DBManager.user, DBManager.passwd);
            Statement st = cn.createStatement();

            String query =
                    "create table if not exists users (" +
                            "username text not null primary key auto_increment, " +
                            "password text not null, " +
                            "score float not null, " +
                            "token text " +
                            ") default charset=utf8;";

            st.executeUpdate(query);

            query = "create table if not exists transaction (" +
                    "username text not null primary key, " +
                    "size float not null, " +
                    "date int not null " +
                    ") default charset=utf8;";

            st.executeUpdate(query);


            cn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

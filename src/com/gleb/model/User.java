package com.gleb.model;

import com.gleb.db.DBManager;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.UUID;

public class User {
    public static void reg(String username, String password) {
        try {
            String query = "insert into " +
                    "users(username, password, score) " +
                    "values(?, ?, ?)";


            Connection cn = DriverManager.getConnection(DBManager.url, DBManager.user, DBManager.passwd);
            PreparedStatement pst = cn.prepareStatement(query);

            pst.setString(1, username);
            pst.setString(2, md5(password));
            pst.setDouble(3, 8.0);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static String login(String username, String password) {
        try {
            String query = "select password, token " +
                    "from users " +
                    "where username='?'";

            Connection cn = DriverManager.getConnection(DBManager.url, DBManager.user, DBManager.passwd);
            PreparedStatement pst = cn.prepareStatement(query);

            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            pst.close();

            if (!rs.next() || !rs.getString("password").equals(password)) {
                return null;
            }

            String token = rs.getString("token");

            if (token == null) {
                token = generateToken();

                query = "update users " +
                        "set token='?' " +
                        "where username='?'";

                pst = cn.prepareStatement(query);
                pst.setString(1, token);
                pst.setString(2, username);
                pst.executeUpdate();
                pst.close();
            }

            return token;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int logout(String username, String token) {
        try {
            String query = "select token " +
                    "from users " +
                    "where username='?'";

            Connection cn = DriverManager.getConnection(DBManager.url, DBManager.user, DBManager.passwd);
            PreparedStatement pst = cn.prepareStatement(query);

            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            pst.close();

            if (!rs.next() || !rs.getString("token").equals(token)) {
                return 1;
            }

            query = "update users " +
                    "set token=null " +
                    "where username='?'";

            pst = cn.prepareStatement(query);
            pst.setString(1, username);
            pst.executeUpdate();
            pst.close();

            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static int payment(String username, String token) {
        try {
            String query = "select score " +
                    "from users " +
                    "where username='?' and token='?'";

            Connection cn = DriverManager.getConnection(DBManager.url, DBManager.user, DBManager.passwd);
            PreparedStatement pst = cn.prepareStatement(query);

            pst.setString(1, username);
            pst.setString(2, token);

            ResultSet rs = pst.executeQuery();
            pst.close();

            if (!rs.next()) {
                return 1;
            }

            float score = rs.getFloat("score");

            if (score < 1.1) {
                return 2;
            }

            score -= 1.1;

            query = "update users " +
                    "set score=? " +
                    "where username='?' and token='?'";

            pst = cn.prepareStatement(query);
            pst.setFloat(1, score);
            pst.setString(2, username);
            pst.setString(3, token);
            pst.executeUpdate();
            pst.close();

            query = "insert into " +
                    "transactions(username, size, date) " +
                    "values(?, ?, ?)";

            pst = cn.prepareStatement(query);
            pst.setString(1, username);
            pst.setFloat(2, (float)1.1);
            pst.setLong(3, (new java.util.Date()).getTime());
            pst.executeUpdate();
            pst.close();

            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    private static String md5(String st) {
        MessageDigest messageDigest;
        byte[] digest = new byte[0];

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(st.getBytes());
            digest = messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger bigInt = new BigInteger(1, digest);
        StringBuilder md5Hex = new StringBuilder(bigInt.toString(16));

        while( md5Hex.length() < 32 ){
            md5Hex.insert(0, "0");
        }

        return md5Hex.toString();
    }
}

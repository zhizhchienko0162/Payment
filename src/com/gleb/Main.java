package com.gleb;

import com.gleb.db.DBManager;
import com.gleb.net.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        DBManager.startDB();

        Server server = new Server("localhost", 8000, 0, 10);
        server.start();
    }
}

package org.example;

import java.sql.SQLException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws SQLException, ParseException {
        DB db = new DB();
        Window window = new Window(db);
    }
}
package org.example;

import java.sql.SQLException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) throws SQLException, ParseException {
        DB db = new DB();
        Window window = new Window(db);
        //Generator g = new Generator();
        //g.generateOrgs(1000);
        //g.generateRes(1000);
        //g.generateApps(10000);
        //g.truncate();
        //g.time();
    }
}
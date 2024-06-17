package org.example;

import java.sql.*;
import java.util.Random;

import static java.lang.Math.ceil;
import static java.lang.Math.random;
public class Generator {

    final static int NUMBER = 9999;
    final static int N = 100;

    final static String URL = "jdbc:mysql://localhost:3306/с";
    final static String USERNAME = "root";
    final static String PASSWORD = "17luv01LelYa2006";
    static Connection connection;
    public Generator(){
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static void generateOrgs(int amount){
        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            for (int i = 0; i < amount; i++){
                try(Statement statement = connection.createStatement()) {
                    int leftLimit = 97; // буква 'a'
                    int rightLimit = 122; // буква 'z'
                    int targetStringLength = 10;
                    Random random = new Random();

                    String a = random.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();

                    targetStringLength = 7;
                    Random random1 = new Random();

                    String n = random1.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();


                    statement.executeUpdate("INSERT INTO organisation(adress, name) VALUES ('" + a + "', '" +
                            n + "')");
                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

        public static void generateRes(int amount){
        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            for (int i = 0; i < amount; i++){
                try(Statement statement = connection.createStatement()) {

                    int leftLimit = 97; // буква 'a'
                    int rightLimit = 122; // буква 'z'
                    int targetStringLength = 10;
                    Random random = new Random();

                    String n = random.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();

                    statement.executeUpdate("INSERT INTO research(name) VALUES ('" + n + "')");
                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    public static void generateApps(int amount){

        final int orgs = 1;
        final int res = 1;

        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            for (int i = 0; i < amount; i++){
                try(Statement statement = connection.createStatement()) {
                    Application application = new Application();

                    int leftLimit = 97; // буква 'a'
                    int rightLimit = 122; // буква 'z'
                    int targetStringLength = 10;
                    Random random = new Random();

                    String fio = random.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();
                    application.fio = fio;

                    Random randomt = new Random();

                    String t = randomt.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();
                    application.theme = t;

                    Random randome = new Random();

                    String e = randomt.ints(leftLimit, rightLimit + 1)
                            .limit(targetStringLength)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                            .toString();
                    application.email = e;

                    application.amount = (int)ceil(((random() * (20 - 1) ) + 1));

                    application.organisation = (int)ceil(((random() * (orgs - 1) ) + 1));

                    application.classifier = (int)ceil(((random() * (res - 1) ) + 1));

                    application.reqSum = (int)ceil(((random() * (2000000 - 100000) ) + 100000));

                    application.allSum = (int)ceil(((random() * (2000000 - 100000) ) + 100000));

                    int year = (int)ceil(((random() * 2023 - 2000)) + 2000);
                    int day = (int)ceil(((random() * 28 - 1)) + 1);
                    int month = (int)ceil(((random() * 12 - 1)) + 1);

                    int status = (int)ceil(((random() * 7)));
                    if (status == 4) status = 5;

                    String date = year + "-" + month + "-" + day + "-";

                    statement.executeUpdate("INSERT INTO application(fio, date, amount, theme, requested_sum, allotted_sum, "+
                            "email, organisation, classifier, status) VALUES ('" + application.fio + "', '" +
                            date + "', " + application.amount + ", '" + application.theme + "', " +
                            application.reqSum + ", "+ application.allSum +", '" + application.email + "', " +
                            1 + ", " + 1 + "," + status + ");");
                } catch (SQLException ex){
                    ex.printStackTrace();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static long s(long[] array){
        long s = 0;
        for (long i: array) s += i;
        return s / array.length;
    }



    public static long updateKey(int i) {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeUpdate("UPDATE application SET fio = 'iii' WHERE id = " + (100 * i));
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static long update(int i) {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeUpdate("UPDATE application SET fio = 'iii' WHERE status = 8");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }
    public static long selectKey(int i) {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeQuery("SELECT fio FROM application WHERE id = " + (100 * i));
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static long select(int i) {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeQuery("SELECT fio FROM application WHERE status = 8");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }
    public static long selectMask(int i) {//фио из 12 букв
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeQuery("SELECT fio FROM application WHERE fio = 'aaaaaaaaaaaa'");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static void truncate() throws SQLException {
        Statement flags0 = connection.createStatement();
        flags0.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        flags0.close();

        Statement apps = connection.createStatement();
        apps.executeUpdate("TRUNCATE application");
        apps.close();

        Statement res = connection.createStatement();
        res.executeUpdate("TRUNCATE research");
        res.close();

        Statement orgs = connection.createStatement();
        orgs.executeUpdate("TRUNCATE organisation");
        orgs.close();

        Statement flags1 = connection.createStatement();
        flags1.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        flags1.close();

    }
    public static long deleteKey(int i) {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeUpdate("DELETE FROM application WHERE id = " + (100 * i ));
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static long deleteGroup() {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeUpdate("DELETE FROM application LIMIT 100");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static long delete(int i) {
        long time = 0;
        try (Statement statement = connection.createStatement(); Statement insert = connection.createStatement()) {
            insert.executeUpdate("INSERT into application(fio, date, amount, theme, requested_sum, email, organisation, classifier, status) VALUES ('fio', '2022-04-22', 5, 'theme', 555, 'email', 1, 1, 8)");
            time = System.nanoTime();
            statement.executeUpdate("DELETE FROM application WHERE status = 8");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static long add() {
        long time = 0;
        try (Statement statement = connection.createStatement()) {
            time = System.nanoTime();
            statement.executeUpdate("INSERT into application(fio, date, amount, theme, requested_sum, email, organisation, classifier, status) VALUES ('fio', '2022-04-22', 5, 'theme', 555, 'email', 1, 1, 1) ");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public static long addGroup(){
        long time = System.nanoTime();
        for(int j = 0; j < 100; j++) {
            try(Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO application(fio, date, amount, theme, requested_sum, " +
                        "email, organisation, classifier, status) VALUES" +
                        "('fio', '2022-04-22', 5, 'theme', 555, 'email', 1, 1, 0)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return System.nanoTime() - time;
    }

    public static long optimize200(){
        long time = 0;
        try(Statement remove = connection.createStatement(); Statement optimize = connection.createStatement()){
            remove.executeUpdate("DELETE FROM application LIMIT 200");
            time = System.nanoTime();

            optimize.executeUpdate("OPTIMIZE TABLE application");
        } catch (SQLException e){
            e.printStackTrace();
        }
        return System.nanoTime() - time;
    }


    public static void time() throws SQLException {
        long[] t = new long[N];



        for (int i = 0; i < N; i++){
            t[i] = update(i);
            //System.out.println("upt" + update(i));
        }
        System.out.println("update: " + s(t));

        for (int i = 0; i < N; i++){
            t[i] = updateKey(i);
            //System.out.println("upd key" +  updateKey(i));
        }
        System.out.println("update key : " + s(t));

        for (int i = 0; i < N; i++){
            t[i] = selectKey(i);
            //System.out.println("sel key " + selectKey(i));
        }
        System.out.println("select key : " + s(t)); // по ранд ключам

        for (int i = 0; i < N; i++){
            t[i] = select(i);
            //System.out.println( "sel " + select(i));
        }
        System.out.println("select : " + s(t));

        for (int i = 0; i < N; i++){
            t[i] = selectMask(i);
            //System.out.println("sel mask " + selectMask(i));
        }
        System.out.println("select mask: " + s(t));

        for (int i = 0; i < 100; i++){
            t[i] = add();
        }
        System.out.println("add : " + s(t));

        for (int i = 0; i < N; i++){
            t[i] = deleteKey(i);
            //System.out.println("del key" + deleteKey(i));
        }
        System.out.println("delete key : " + s(t));// по уникальному стр

        for (int i = 0; i < N; i++){
            t[i] = delete(i);
            //System.out.println("del " + delete(i));
        }
        System.out.println("delete : " + s(t));

        /*Statement statement = connection.createStatement();
        statement.executeUpdate("OPTIMIZE TABLE application");

        long tag = System.nanoTime();
        addGroup();
        System.out.println("add group: " + (System.nanoTime() - tag));

        long tdg = System.nanoTime();
        deleteGroup();
        System.out.println("delete group: " + (System.nanoTime() - tdg));

        long tot = System.nanoTime();
        optimize200();
        System.out.println("optimize: " + (System.nanoTime() - tot));*/
    }


}

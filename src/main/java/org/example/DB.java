package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

import static java.lang.Math.floor;

public class DB {
    enum Status{
        PENDING,//0
        ACCEPTED,//1
        DENIED,
        FAILED,//2
        IN_PROGRESS,//4
        SUCCESS//3
    }
    enum Reason{
        SQL_ERROR,
        OK,
        NO_INTEREST_IN_THEME,//5
        FAILED_IN_PAST,//6
        APPLICATION_OF_HEAD_ALREADY_APPLIED,//7
        AFTER_DEADLINE//8
    }

    public int start = 0, end = 10;
    public int start6 = 0, end6 = 10;
    public int start7 = 0, end7 = 10;
    public int start8 = 0, end8 = 10;

    public int offset = 0;
    private int budget;
    private Date date;
    private Date deadline;
    Connection connection;

    public boolean setDate(Date date){this.date = date;
        try (Statement statement = connection.createStatement()){
            ResultSet year = statement.executeQuery("SELECT deadline, budget FROM year WHERE year = '" +
                                                                    (date.getYear() + 1900) + "'");
            year.next();
            deadline = year.getDate("deadline");
            System.out.println(deadline);
            budget = year.getInt("budget");
            if (checkDeadline()) calculateBudget();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean closeConnection(){
        try{
            connection.close();
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public DB(){
        final String URL = "jdbc:mysql://localhost:3306/rffi_model3t";
        final String USERNAME = "root";
        final String PASSWORD = "eHsdffgr848gtwr";
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement statement = connection.createStatement();
            /*ResultSet s = statement.executeQuery("SELECT COUNT(id) FROM application WHERE status = 1");
            s.next();*/
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public String[] getCompleted(String search){
        try (Statement statement = connection.createStatement()) {
            String[] toReturn = new String[10];
            ResultSet resultSet = statement.executeQuery("SELECT fio FROM application WHERE fio LIKE '"+
                    search + "%' AND status = 4");
            int i = 0;
            while (resultSet.next()){
                if(i == 10) break;
                toReturn[i] = resultSet.getString("fio");
                i++;
            }
            return toReturn;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public DataBaseObject[] getObjects(String search, String table){
        try (Statement statement = connection.createStatement()) {
            DataBaseObject[] toReturn = new DataBaseObject[10];
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + table + " WHERE name LIKE '"+
                    search + "%'");
            int i = 0;
            while (resultSet.next()){
                toReturn[i] = new DataBaseObject(resultSet.getInt("id"), resultSet.getString("name"));
                i++;
            }
            return toReturn;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    public boolean setResult(String name, boolean status){
        System.out.println(name);
        int r = 0;
        try(Statement setStatus = connection.createStatement()) {
            if (status){
                r = setStatus.executeUpdate("UPDATE application SET status = 3 WHERE status = 4 AND fio = '" + name + "'");
            } else {
                r = setStatus.executeUpdate("UPDATE application SET status = 2 WHERE status = 4 AND fio = '" + name + "'");
            }
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return r > 0;
    }

    public boolean results(){
        try(Statement statement = connection.createStatement();
            Statement undone = connection.createStatement()
        ) {
            ResultSet resultsDate = statement.executeQuery("SELECT deadline FROM year WHERE year = '" +
                                                            (date.getYear() + 1900 - 1) + "'");
            System.err.println("DATE: " + (date.getYear() + 1900 - 1));
            if (!resultsDate.isBeforeFirst()) return false;
            resultsDate.next();
            Date rDate = resultsDate.getDate("deadline");
            rDate.setYear(rDate.getYear() + 1);
            ResultSet set = undone.executeQuery("SELECT id FROM application WHERE status = 4 AND YEAR(date) = " + (date.getYear() + 1900 - 1));
            System.err.println("DATE: " + (rDate.getYear() + 1900 - 1));
            if (rDate.before(date) && set.isBeforeFirst()) return true;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean checkDeadline(){
        //return date.equals(deadline);
        return date.after(deadline);
    }
    private void calculateBudget(){
        System.out.println(budget);
        int sum = 0;
        try(Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery("SELECT *FROM application WHERE status = 1");
            boolean isOK = true;
            while (set.next()){
                int reqSum = set.getInt("requested_sum");
                String name = set.getString("fio");
                if (reqSum > budget * 0.1) reqSum = (int)floor(budget * 0.1);
                sum += reqSum;
                Statement update = connection.createStatement();
                System.out.println(reqSum);
                update.executeUpdate("UPDATE application SET allotted_sum = " + reqSum + " WHERE fio = '" + name + "'");
                if (sum > budget) isOK = false;
                update.close();
            }
            Statement s0 = connection.createStatement();
            set = s0.executeQuery("SELECT *FROM application WHERE status = 1");
            System.out.println(sum);
            if (!isOK){
                while (set.next()){
                    String name = set.getString("fio");

                    double n = (double) set.getDouble("allotted_sum") / sum * budget;

                    System.out.println("n: " + n + "; sum: " + sum + "; budg:" + budget);
                    System.out.println(n);
                    Statement s = connection.createStatement();
                    s.executeUpdate("UPDATE application SET allotted_sum = " + n + " WHERE fio = '" + name + "'");
                    s.close();
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE application SET status = 4 WHERE status = 1");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static String removeLastChar(String str) {
        if (str == null || str.length() == 0) {
            return str;
        }
        return str.substring(0, str.length() - 1);
    }
    public static boolean isFileExists(File file) {
        return file.exists() && !file.isDirectory();
    }
    private void generateDenyLetter(int status, int id){
        String text = "";
        String theme = "";
        String path = "denyLetters/";
        try (Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery("SELECT fio, theme FROM application WHERE id = " + id);
            resultSet.next();
            text = resultSet.getString("fio");
            theme = resultSet.getString("theme");
            path = path + text;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        File file = new File(path + ".txt");
        int i = 1;
        if (isFileExists(file)) path += '0';
        file = new File(path + ".txt");
        while (isFileExists(file)){
            path = removeLastChar(path);
            path += i;
            file = new File(path + ".txt");
            i++;
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file, true)) {
            writer.write(text + "!\n");
            writer.write("Ваша заявка по теме " + theme + " была отклонена по причине:\n");
            switch (status) {
                case 0 -> writer.write("Тема исследования не представляет интереса.\n");
                case 1 -> writer.write("Руководитель провалил проект ранее.\n");
                case 2 -> writer.write("Руководителю уже одобрена заявка на другой проект.\n");
                case 3 -> writer.write("Заявка подана позднее срока.\n");
            }
            writer.write("С уважением,\nРоссийский Фонд Фундаментальных Исследованний.");
            writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private void generateDenyLetter(int status, String name, String theme){
        String path = "denyLetters/" + name;
        File file = new File(path + ".txt");
        int i = 1;
        if (isFileExists(file)) path += '0';
        file = new File(path + ".txt");
        while (isFileExists(file)){
            path = removeLastChar(path);
            path += i;
            file = new File(path + ".txt");
            i++;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file, true)) {
            writer.write(name + "!\n");
            writer.write("Ваша заявка по теме " + theme + " была отклонена по причине:\n");
            switch (status) {
                case 0 -> writer.write("Тема исследования не представляет интереса.\n");
                case 1 -> writer.write("Руководитель провалил проект ранее.\n");
                case 2 -> writer.write("Руководителю уже одобрена заявка на другой проект.\n");
                case 3 -> writer.write("Заявка подана позднее срока.\n");
            }
            writer.write("С уважением,\nРоссийский Фонд Фундаментальных Исследованний.");
            writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    private void generateAcceptLetter(int id){
        String text = "";
        String theme = "";
        String path = "acceptLetters/";
        try (Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery("SELECT fio, theme FROM application WHERE id = " + id);
            resultSet.next();
            text = resultSet.getString("fio");
            theme = resultSet.getString("theme");
            path = path + text;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        File file = new File(path + ".txt");
        int i = 1;
        if (isFileExists(file)) path += '0';
        file = new File(path  + ".txt");
        while (isFileExists(file)){
            path = removeLastChar(path);
            path += i;
            file = new File(path + ".txt");
            i++;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file, true)) {
            writer.write(text + "!\n");
            writer.write("Ваша заявка по теме " + theme + " была принята.\nЖелаем удачи в исследованиях!\n");
            writer.write("С уважением,\nРоссийский Фонд Фундаментальных Исследованний.");
            writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public Reason addApplication(Application application){
        if (application.date.after(deadline)){
            generateDenyLetter(3, application.fio, application.theme);
            return Reason.AFTER_DEADLINE;
        }
        if (ifEverFailed(application.fio)){
            generateDenyLetter(1, application.fio, application.theme);
        }
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO application(fio, date, amount, theme, requested_sum, "+
                    "email, organisation, classifier, status) VALUES ('" + application.fio + "', '" +
                    application.date + "', " + application.amount + ", '" + application.theme + "', " +
                    application.reqSum + ", '" + application.email + "', " +
                    application.organisation + ", " + application.classifier + ", 0);");
        } catch (SQLException e){
            e.printStackTrace();
            return Reason.SQL_ERROR;
        }
        return Reason.OK;
    }
    public Reason deny(int id){
        try(Statement statement = connection.createStatement()) {
            int a = statement.executeUpdate("UPDATE application SET status = 5 WHERE id = " + id);
            if(a != 1){
                return Reason.SQL_ERROR;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        generateDenyLetter(0, id);
        return Reason.OK;
    }
    private boolean ifEverFailed(String fio){
        try(Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery("SELECT id FROM application WHERE fio = '" + fio +
                                                        "' AND status = 2");
            if (set.next()) return true;
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public Reason accept(int id){
        try(    Statement extractFio = connection.createStatement();
                Statement boolStatement = connection.createStatement();
                Statement statement = connection.createStatement()) {
            ResultSet fio = extractFio.executeQuery("SELECT fio FROM application WHERE id = " + id);
            start += 10; end += 10;
            fio.next();
            String stringFIO = fio.getString("fio");

            ResultSet resultSet = boolStatement.executeQuery("SELECT id FROM application WHERE status = 1 AND fio = '" + stringFIO + "'");
            if (resultSet.next()){
                generateDenyLetter(2, id);
                return Reason.APPLICATION_OF_HEAD_ALREADY_APPLIED;
            }
            statement.executeUpdate("UPDATE application SET status = 1 WHERE id = " + id);
        } catch (SQLException e){
            e.printStackTrace();
            return Reason.SQL_ERROR;
        }
        generateAcceptLetter(id);
        return Reason.OK;
    }
    public void getPendingApplications(JPanel applications){
        try(Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM application WHERE status = 0 LIMIT " +
                    start + ", " + end);
            start += 10; end += 10;
            applications.removeAll();

            while (resultSet.next()){

                JPanel panel = new JPanel(new FlowLayout());

                JTextField id = new JTextField(4);
                JTextField amount = new JTextField(4);
                JTextField fio = new JTextField(16);
                JTextField theme = new JTextField(16);
                JTextField requestedSum = new JTextField(8);
                JTextField classifier = new JTextField(8);

                id.setEditable(false);
                amount.setEditable(false);
                fio.setEditable(false);
                theme.setEditable(false);
                requestedSum.setEditable(false);
                classifier.setEditable(false);

                id.setText(resultSet.getString("id"));
                amount.setText(resultSet.getString("amount"));
                fio.setText(resultSet.getString("fio"));
                theme.setText(resultSet.getString("theme"));
                requestedSum.setText(resultSet.getString("requested_sum"));
                classifier.setText(resultSet.getString("classifier"));

                panel.add(id);
                panel.add(amount);
                panel.add(fio);
                panel.add(theme);
                panel.add(requestedSum);
                panel.add(classifier);

                applications.add(panel);
            }


        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void getAcceptedApplications(JPanel applications){
        try(Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM application WHERE status = 1 LIMIT " +
                    start + ", " + end + ";");
            applications.removeAll();

            while (resultSet.next()){

                JPanel panel = new JPanel(new FlowLayout());

                JTextField id = new JTextField(4);
                JTextField amount = new JTextField(4);
                JTextField fio = new JTextField(16);
                JTextField theme = new JTextField(16);
                JTextField requestedSum = new JTextField(8);
                JTextField classifier = new JTextField(8);

                id.setEditable(false);
                amount.setEditable(false);
                fio.setEditable(false);
                theme.setEditable(false);
                requestedSum.setEditable(false);
                classifier.setEditable(false);

                id.setText(resultSet.getString("id"));
                amount.setText(resultSet.getString("amount"));
                fio.setText(resultSet.getString("fio"));
                theme.setText(resultSet.getString("theme"));
                requestedSum.setText(resultSet.getString("requested_sum"));
                classifier.setText(resultSet.getString("classifier"));

                panel.add(id);
                panel.add(amount);
                panel.add(fio);
                panel.add(theme);
                panel.add(requestedSum);
                panel.add(classifier);

                applications.add(panel);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void getDeniedApplications(JPanel applications){
        JLabel label = new JLabel();
        try(Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            String text = "Тема не представляет интереса";
            ResultSet resultSet = statement.executeQuery("SELECT *FROM application WHERE status = 5 " +
                    "LIMIT " + start + ", " + end + ";");
            start += 10; end += 10;
            //int oldStart = start, oldEnd = end;
            if (!resultSet.next()){
                text = "Руководитель имеет проваленные проекты";
                resultSet = statement.executeQuery("SELECT *FROM application WHERE status = 6 " +
                        "LIMIT " + (start6) + ", " + (end6) + ";");
                start6 += 10; end6 += 10;
                start -= 10; end -= 10;
                if (!resultSet.next()){
                    text = "Заявка руководителя уже принята";
                    resultSet = statement.executeQuery("SELECT *FROM application WHERE status = 7 " +
                            "LIMIT " + (start7) + ", " + (end7) + ";");
                    start7 += 10; end7 += 10;
                    start6 -= 10; end6 -= 10;
                    if (!resultSet.next()){
                        text = "Заявка подана после дедлайна";
                        resultSet = statement.executeQuery("SELECT *FROM application WHERE status = 8 " +
                                "LIMIT " + (start8) + ", " + (end8) + ";");
                        start7 -= 10; end7 -= 10;
                        start8 += 10; end8 += 10;
                    }
                }
            }
            applications.removeAll();
            label.setText(text);
            applications.add(label);
            resultSet.beforeFirst();
            while (resultSet.next()) {
                JPanel panel = new JPanel(new FlowLayout());

                JTextField id = new JTextField(4);
                JTextField amount = new JTextField(4);
                JTextField fio = new JTextField(16);
                JTextField theme = new JTextField(16);
                JTextField requestedSum = new JTextField(8);
                JTextField classifier = new JTextField(8);

                id.setEditable(false);
                amount.setEditable(false);
                fio.setEditable(false);
                theme.setEditable(false);
                requestedSum.setEditable(false);
                classifier.setEditable(false);

                id.setText(resultSet.getString("id"));
                amount.setText(resultSet.getString("amount"));
                fio.setText(resultSet.getString("fio"));
                theme.setText(resultSet.getString("theme"));
                requestedSum.setText(resultSet.getString("requested_sum"));
                classifier.setText(resultSet.getString("classifier"));

                panel.add(id);
                panel.add(amount);
                panel.add(fio);
                panel.add(theme);
                panel.add(requestedSum);
                panel.add(classifier);

                System.out.println(id.getText());
                applications.add(panel);
            }
            System.out.println();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void getFinancialReport(JPanel applications){
        try(Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM application WHERE status = 3 OR status = 4 OR status = 5 LIMIT ";
            ResultSet resultSet = statement.executeQuery(query + start + ", " + end + ";");
            applications.removeAll();

            while (resultSet.next()){
                System.out.println(resultSet.getString(2));

                JPanel panel = new JPanel(new FlowLayout());

                JTextField id = new JTextField(4);
                JTextField amount = new JTextField(4);
                JTextField fio = new JTextField(16);
                JTextField theme = new JTextField(16);
                JTextField requestedSum = new JTextField(8);
                JTextField allottedSum = new JTextField(8);
                JTextField classifier = new JTextField(8);
                JTextField alllottedSum = new JTextField(8);

                id.setEditable(false);
                amount.setEditable(false);
                fio.setEditable(false);
                theme.setEditable(false);
                requestedSum.setEditable(false);
                allottedSum.setEditable(false);
                classifier.setEditable(false);

                id.setText(resultSet.getString("id"));
                amount.setText(resultSet.getString("amount"));
                fio.setText(resultSet.getString("fio"));
                theme.setText(resultSet.getString("theme"));
                requestedSum.setText(resultSet.getString("requested_sum"));
                allottedSum.setText(resultSet.getString("allotted_sum"));
                classifier.setText(resultSet.getString("classifier"));
                alllottedSum.setText(resultSet.getString("allotted_sum"));

                panel.add(id);
                panel.add(amount);
                panel.add(fio);
                panel.add(theme);
                panel.add(requestedSum);
                panel.add(alllottedSum);
                panel.add(classifier);

                applications.add(panel);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public boolean financial(){
        String path = "reports/Financial";
        File file = new File(path + ".txt");
        int i = 1;
        if (isFileExists(file)) path += '0';
        file = new File(path  + ".txt");
        while (isFileExists(file)){
            path = removeLastChar(path);
            path += i;
            file = new File(path + ".txt");
            i++;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(FileWriter writer = new FileWriter(file, true)) {
            writer.write("ФИНАНСОВФЫЙ ОТЧЕТ\n");
            try (Statement statement = connection.createStatement()){
                ResultSet resultSet = statement.executeQuery("SELECT *FROM year" );
                while (resultSet.next()){
                    writer.write(resultSet.getString("year") + "\n");
                    writer.write("budget: " + resultSet.getString("budget") + "\n");
                    try (Statement f = connection.createStatement()){
                        ResultSet apps = f.executeQuery("SELECT *FROM application WHERE YEAR(date) = " +
                                (resultSet.getInt("year")) + " AND status = 1 ");
                        writer.write("Принято\n");
                        writer.write("---------------------------------------------\n");
                        while (apps.next()){
                            writer.write(apps.getString("fio") + " | ");
                            writer.write(apps.getString("theme") + " | ");
                            writer.write(apps.getString("classifier") + " | ");
                            writer.write(apps.getString("requested_sum") + " | ");
                            String allS = apps.getString("allotted_sum");
                            if (allS == null) allS = "0";
                            writer.write(allS);
                            writer.write("\n");
                        }
                        writer.write("---------------------------------------------\n");
                    } catch (SQLException exc){
                        exc.printStackTrace();
                        return false;
                    }
                    try (Statement f = connection.createStatement()){
                        ResultSet apps = f.executeQuery("SELECT *FROM application WHERE YEAR(date) = " +
                                (resultSet.getInt("year")) + " AND status = 2 ");
                        writer.write("Провалено\n");
                        writer.write("---------------------------------------------\n");
                        while (apps.next()){
                            writer.write(apps.getString("fio") + " | ");
                            writer.write(apps.getString("theme") + " | ");
                            writer.write(apps.getString("classifier") + " | ");
                            writer.write(apps.getString("requested_sum") + " | ");
                            String allS = apps.getString("allotted_sum");
                            if (allS == null) allS = "0";
                            writer.write(allS);
                            writer.write("\n");
                        }
                        writer.write("---------------------------------------------\n");

                    } catch (SQLException exc){
                        exc.printStackTrace();
                        return false;
                    }
                    try (Statement f = connection.createStatement()){
                        ResultSet apps = f.executeQuery("SELECT *FROM application WHERE YEAR(date) = " +
                                (resultSet.getInt("year")) + " AND status = 4 ");
                        writer.write("В процессе\n");
                        writer.write("---------------------------------------------\n");
                        while (apps.next()){
                            writer.write(apps.getString("fio") + " | ");
                            writer.write(apps.getString("theme") + " | ");
                            writer.write(apps.getString("classifier") + " | ");
                            writer.write(apps.getString("requested_sum") + " | ");
                            String allS = apps.getString("allotted_sum");
                            if (allS == null) allS = "0";
                            writer.write(allS);
                            writer.write("\n");
                        }
                        writer.write("---------------------------------------------\n");

                    } catch (SQLException exc){
                        exc.printStackTrace();
                        return false;
                    }
                    try (Statement f = connection.createStatement()){
                        ResultSet apps = f.executeQuery("SELECT *FROM application WHERE YEAR(date) = " +
                                (resultSet.getInt("year")) + " AND status = 3 ");
                        writer.write("Завершено\n");
                        writer.write("---------------------------------------------\n");
                        while (apps.next()){
                            writer.write(apps.getString("fio") + " | ");
                            writer.write(apps.getString("theme") + " | ");
                            writer.write(apps.getString("classifier") + " | ");
                            writer.write(apps.getString("requested_sum") + " | ");
                            String allS = apps.getString("allotted_sum");
                            if (allS == null) allS = "0";
                            writer.write(allS);
                            writer.write("\n");
                        }
                        writer.write("---------------------------------------------\n");

                    } catch (SQLException exc){
                        exc.printStackTrace();
                        return false;
                    }
                }
            } catch (SQLException ex){
                ex.printStackTrace();
                return false;
            }
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
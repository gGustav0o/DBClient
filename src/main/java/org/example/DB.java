package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Calendar;

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
    private final Calendar currentDate = Calendar.getInstance();
    private final Calendar deadline    = Calendar.getInstance();

    public final int portion = 10;
    Connection connection;

    public boolean setDate(Date date){
        currentDate.setTime(date);
        String sqlGetYear = "SELECT deadline, budget FROM year WHERE year = ?";
        try(PreparedStatement getYearStatement = connection.prepareStatement(sqlGetYear)) {
            getYearStatement.setInt(1, currentDate.get(Calendar.YEAR));
            try(ResultSet year = getYearStatement.executeQuery()) {
                year.next();
                deadline.setTime(year.getDate("deadline"));
                budget = year.getInt("budget");
                if (isTodayAfterDeadline()) calculateBudget();
                return true;
            } catch (SQLException executeException){
                executeException.printStackTrace();
            }
        } catch (SQLException prepareException) {
            prepareException.printStackTrace();
            return false;
        }
        return false;
    }
    public void closeConnection(){
        try{
            connection.close();
        } catch (SQLException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
    public DB(){
        final String URL = "jdbc:mysql://localhost:3306/rffi_model3t";
        final String USERNAME = "root";
        final String PASSWORD = "eHsdffgr848gtwr";

        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public String[] getCompleted(String search){
        String sqlGetCompleted = "SELECT fio FROM application WHERE fio LIKE '?%'  AND status = 4";
        try (PreparedStatement getCompletedStatement = connection.prepareStatement(sqlGetCompleted)) {
            getCompletedStatement.setString(1, search);
            try(ResultSet resultSet = getCompletedStatement.executeQuery()) {
                int i = 0;
                String[] toReturn = new String[10];
                while (resultSet.next()){
                    if(i == 10) break;
                    toReturn[i] = resultSet.getString("fio");
                    i++;
                }
                return toReturn;
            } catch (SQLException executeException) {
                executeException.printStackTrace();
            }
        } catch (SQLException prepareException){
            prepareException.printStackTrace();
        }
        return null;
    }
    public DataBaseObject[] getObjects(String search, String table){
        String sqlGetObjects = "SELECT * FROM ? WHERE name LIKE '?%'";
        try (PreparedStatement getObjectsStatement = connection.prepareStatement(sqlGetObjects)) {
            getObjectsStatement.setString(1, table);
            getObjectsStatement.setString(2,search);
            try(ResultSet resultSet = getObjectsStatement.executeQuery()) {
                DataBaseObject[] toReturn = new DataBaseObject[10];
                int i = 0;
                while (resultSet.next()){
                    toReturn[i] = new DataBaseObject(resultSet.getInt("id"), resultSet.getString("name"));
                    i++;
                }
                return toReturn;
            } catch (SQLException executeException) {
                executeException.printStackTrace();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
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
    public boolean setResult(String name, boolean status){
        int r;
        String sqlUpdateStatus = "UPDATE application SET status = ? WHERE status = 4 AND fio = '?'";
        try(PreparedStatement updateStatusStatement = connection.prepareStatement(sqlUpdateStatus)) {
            updateStatusStatement.setString(2,name);

            if (status)
                updateStatusStatement.setInt(1, 3);
            else
                updateStatusStatement.setInt(1, 2);

            r = updateStatusStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return r > 0;
    }
    public boolean results(){
        String sqlGetDeadline   = "SELECT deadline FROM year WHERE year = ? "
             , sqlGetInProgress = "SELECT id FROM application WHERE status = 4 AND YEAR(date) = ?";

        try(PreparedStatement getDeadlineStatement   = connection.prepareStatement(sqlGetDeadline);
            PreparedStatement getInProgressStatement = connection.prepareStatement(sqlGetInProgress)
        ) {
            getDeadlineStatement  .setInt(1, currentDate.get(Calendar.YEAR) - 1);
            getInProgressStatement.setInt(1, currentDate.get(Calendar.YEAR) - 1);

            try(ResultSet lastYearDeadline = getDeadlineStatement.executeQuery();){
                if (!lastYearDeadline.isBeforeFirst()) return false;
                lastYearDeadline.next();
                Date lastYearDeadlineDate = lastYearDeadline.getDate("deadline");
                Calendar lastYearDeadlineCalendar = Calendar.getInstance();
                lastYearDeadlineCalendar.setTime(lastYearDeadlineDate);
                lastYearDeadlineCalendar.set(Calendar.YEAR, lastYearDeadlineCalendar.get(Calendar.YEAR) + 1);
                try(ResultSet set = getInProgressStatement.executeQuery()) {
                    if (lastYearDeadlineCalendar.before(currentDate) && set.isBeforeFirst()) return true;
                } catch (SQLException executeGetInProgressException) {
                    executeGetInProgressException.printStackTrace();
                }
            } catch (SQLException executeGetDeadlineException){
                executeGetDeadlineException.printStackTrace();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
    private boolean isTodayAfterDeadline(){
        return currentDate.after(deadline);
    }
    private void calculateBudget(){
        int totalSum = 0;
        try(Statement statement = connection.createStatement()) {
            ResultSet acceptedApplications = statement.executeQuery("SELECT * FROM application WHERE status = 1");
            boolean isOK = true;
            String sqlUpdateAllottedSum = "UPDATE application SET allotted_sum = ?  WHERE fio = '?'";
            try(PreparedStatement updateAllottedSumStatement = connection.prepareStatement(sqlUpdateAllottedSum)){
                while (acceptedApplications.next()){
                    int    reqSum = acceptedApplications.getInt("requested_sum");
                    String name   = acceptedApplications.getString("fio");

                    if (reqSum > budget * 0.1) reqSum = (int)floor(budget * 0.1);
                    totalSum += reqSum;
                    updateAllottedSumStatement.setDouble(1, reqSum);
                    updateAllottedSumStatement.setString(2, name);
                    updateAllottedSumStatement.executeUpdate();
                    if (totalSum > budget) isOK = false;
                }

                if (!isOK){
                    try {
                        acceptedApplications.beforeFirst();
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                    while (acceptedApplications.next()){
                        String name           = acceptedApplications.getString("fio");
                        double newAllottedSum = acceptedApplications.getDouble("allotted_sum") / totalSum * budget;
                        updateAllottedSumStatement.setDouble(1, newAllottedSum);
                        updateAllottedSumStatement.setString(2, name);
                        updateAllottedSumStatement.executeUpdate();
                    }
                }
            } catch (SQLException e){
                e.printStackTrace();
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
    private File createFile(String path){
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
            if(!file.createNewFile()){
                System.err.println("File " + path + ".txt already exists");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }
    private String generateDenyText(String name, String theme, int status){
        String text = name + "!\n";
        text += "Ваша заявка по теме " + theme + " была отклонена по причине:\n";
        switch (status) {
            case 0 -> text += "Тема исследования не представляет интереса.\n";
            case 1 -> text += "Руководитель провалил проект ранее.\n";
            case 2 -> text += "Руководителю уже одобрена заявка на другой проект.\n";
            case 3 -> text += "Заявка подана позднее срока.\n";
        }
        text += "С уважением,\nРоссийский Фонд Фундаментальных Исследованний.";
        return text;
    }
    private void generateDenyLetter(int status, int id){
        String text = "";
        String theme = "";
        String path = "denyLetters/";
        String sqlGetFioTheme = "SELECT fio, theme FROM application WHERE id = ?";
        try(PreparedStatement getFioThemeStatement = connection.prepareStatement(sqlGetFioTheme)){
            try(ResultSet resultSet = getFioThemeStatement.executeQuery()){
                if(!resultSet.isBeforeFirst()){
                    System.err.println("Empty ResultSet for 'SELECT fio, theme FROM application WHERE id = " + id + "'");
                    return;
                }
                resultSet.next();
                text  = resultSet.getString("fio");
                theme = resultSet.getString("theme");
                path = path + text;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        File file = createFile(path);
        text = generateDenyText(text, theme, status);
        writeMessage(file, text);
    }

    private void generateDenyLetter(int status, String name, String theme){
        String path = "denyLetters/" + name;
        File file = createFile(path);
        String text = generateDenyText(name, theme, status);
        writeMessage(file, text);
    }

    private void writeMessage(File file, String text){
        try(FileWriter writer = new FileWriter(file, true)) {
            writer.write(text);
            writer.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private String generateAcceptText(String name, String theme){
        String text = name + "!\n";
        text += "Ваша заявка по теме " + theme + " была принята.\nЖелаем удачи в исследованиях!\n";
        text += "С уважением,\nРоссийский Фонд Фундаментальных Исследованний.";
        return text;
    }
    private void generateAcceptLetter(int id){
        String text = "";
        String theme = "";
        String path = "acceptLetters/";
        String sqlGetFioTheme = "SELECT fio, theme FROM application WHERE id = ?";
        try (PreparedStatement getFioThemeStatement = connection.prepareStatement(sqlGetFioTheme)){
            try(ResultSet resultSet = getFioThemeStatement.executeQuery()){
                resultSet.next();
                text  = resultSet.getString("fio");
                theme = resultSet.getString("theme");
                path = path + text;
            } catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        File file = createFile(path);
        text = generateAcceptText(text, theme);
        writeMessage(file, text);
    }
    public Reason addApplication(Application application){
        Calendar applicationDate = Calendar.getInstance();
        applicationDate.setTime(application.date);
        if (applicationDate.after(deadline)){
            generateDenyLetter(3, application.fio, application.theme);
            return Reason.AFTER_DEADLINE;
        }
        if (ifEverFailed(application.fio)){
            generateDenyLetter(1, application.fio, application.theme);
        }
        String sqlInsertApplication = """
                INSERT INTO
                    application
                    (fio, date, amount, theme, requested_sum, email, organisation, classifier, status)
                VALUES
                    ('?', '?', ? '?', ?, '?', ?, ?, 0)
                """;
        try(PreparedStatement insertApplicationStatement = connection.prepareStatement(sqlInsertApplication)) {
            insertApplicationStatement.setString(1, application.fio);
            insertApplicationStatement.setDate(2, new Date(application.date.getTime()));
            insertApplicationStatement.setInt(3, application.amount);
            insertApplicationStatement.setString(4, application.theme);
            insertApplicationStatement.setDouble(5, application.reqSum);
            insertApplicationStatement.setString(6, application.email);
            insertApplicationStatement.setInt(7, application.organisation);
            insertApplicationStatement.setInt(8, application.classifier);
            insertApplicationStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return Reason.SQL_ERROR;
        }
        return Reason.OK;
    }
    public Reason deny(int id){
        String sqlSetStatus = "UPDATE application SET status = 5 WHERE id = ?";
        try(PreparedStatement setStatusStatement = connection.prepareStatement(sqlSetStatus)) {
            setStatusStatement.setInt(1, id);
            int a = setStatusStatement.executeUpdate();
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
        String sqlSelectId = "SELECT id FROM application WHERE fio = '?' AND status = 2";
        try(PreparedStatement selectIdStatement = connection.prepareStatement(sqlSelectId)) {
            selectIdStatement.setString(1, fio);
            try(ResultSet resultSet = selectIdStatement.executeQuery()){
                if (resultSet.next()) return true;
            } catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public Reason accept(int id){
        String sqlSelectFio    = "SELECT fio FROM application WHERE id = ?";
        String sqlSelectId     = "SELECT id FROM application WHERE status = 1 AND fio = '?'";
        String sqlUpdateStatus = "UPDATE application SET status = 1 WHERE id = ?";

        try(    PreparedStatement selectFioStatement    = connection.prepareStatement(sqlSelectFio);
                PreparedStatement selectIdStatement     = connection.prepareStatement(sqlSelectId);
                PreparedStatement updateStatusStatement = connection.prepareStatement(sqlUpdateStatus)) {
            selectFioStatement.setInt(1, id);

            try(ResultSet fio = selectFioStatement.executeQuery()) {
                start += 10; end += 10;
                fio.next();
                String selectedFio = fio.getString("fio");
                selectIdStatement.setString(1, selectedFio);
            } catch (SQLException e){
                e.printStackTrace();
            }

            try(ResultSet selectedId = selectIdStatement.executeQuery()){
                if (selectedId.next()){
                    generateDenyLetter(2, id);
                    return Reason.APPLICATION_OF_HEAD_ALREADY_APPLIED;
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
            updateStatusStatement.setInt(1, id);
            updateStatusStatement.executeUpdate();
        } catch (SQLException e){
            e.printStackTrace();
            return Reason.SQL_ERROR;
        }
        generateAcceptLetter(id);
        return Reason.OK;
    }

    //TODO: move to the Window class
    public void getPendingApplications(JPanel applications){
        String sqlSelectApplications = "SELECT * FROM application WHERE status = 0 LIMIT ?, ?";
        try(PreparedStatement selectApplicationsStatement = connection.prepareStatement(sqlSelectApplications)) {
            selectApplicationsStatement.setInt(1, start);
            selectApplicationsStatement.setInt(2, end);
            try(ResultSet resultSet = selectApplicationsStatement.executeQuery()) {
                start += portion; end += portion;
                applications.removeAll();

                while (resultSet.next()){

                    JPanel panel = new JPanel(new FlowLayout());

                    JTextField id           = new JTextField(4)
                             , amount       = new JTextField(4)
                             , fio          = new JTextField(16)
                             , theme        = new JTextField(16)
                             , requestedSum = new JTextField(8)
                             , classifier   = new JTextField(8);

                    Window.setEditable(false, id, amount, fio, theme, requestedSum, classifier);

                    id          .setText(resultSet.getString("id"));
                    amount      .setText(resultSet.getString("amount"));
                    fio         .setText(resultSet.getString("fio"));
                    theme       .setText(resultSet.getString("theme"));
                    requestedSum.setText(resultSet.getString("requested_sum"));
                    classifier  .setText(resultSet.getString("classifier"));

                    Window.addComponents(panel, id, amount, fio, theme, requestedSum, classifier);

                    applications.add(panel);
                }
            } catch (SQLException e){
                e.printStackTrace();
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void getAcceptedApplications(JPanel applications){
        String sqlSelectApplications = "SELECT * FROM application WHERE status = 1 LIMIT ?, ?";
        try(PreparedStatement selectApplicationsStatement = connection.prepareStatement(sqlSelectApplications)) {
            selectApplicationsStatement.setInt(1, start);
            selectApplicationsStatement.setInt(2, end);
            try(ResultSet resultSet = selectApplicationsStatement.executeQuery()) {
                applications.removeAll();

                while (resultSet.next()){

                    JPanel panel = new JPanel(new FlowLayout());

                    JTextField id           = new JTextField(4)
                             , amount       = new JTextField(4)
                             , fio          = new JTextField(16)
                             , theme        = new JTextField(16)
                             , requestedSum = new JTextField(8)
                             , classifier   = new JTextField(8);

                    Window.setEditable(false, id, amount, fio, theme, requestedSum, classifier);

                    id          .setText(resultSet.getString("id"));
                    amount      .setText(resultSet.getString("amount"));
                    fio         .setText(resultSet.getString("fio"));
                    theme       .setText(resultSet.getString("theme"));
                    requestedSum.setText(resultSet.getString("requested_sum"));
                    classifier  .setText(resultSet.getString("classifier"));

                    Window.addComponents(panel, id, amount, fio, theme, requestedSum, classifier);
                    applications.add(panel);
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void getDeniedApplications(JPanel applications){
        JLabel label = new JLabel();
        applications.removeAll();

        String sqlSelectApplication = "SELECT * FROM application WHERE status = ? LIMIT ?, ?";

        try(PreparedStatement selectApplicationStatement = connection.prepareStatement(sqlSelectApplication)) {
            String text = "Тема не представляет интереса";
            selectApplicationStatement.setInt(1, 5);
            selectApplicationStatement.setInt(2, start);
            selectApplicationStatement.setInt(3, end);
            ResultSet resultSet = null;
            ResultSet resultSet5 = selectApplicationStatement.executeQuery();
            start += 10; end += 10;
            if (!resultSet5.next()){
                resultSet5.close();
                text = "Руководитель имеет проваленные проекты";
                selectApplicationStatement.setInt(1, 6);
                selectApplicationStatement.setInt(2, start6);
                selectApplicationStatement.setInt(3, end6);
                ResultSet resultSet6 = selectApplicationStatement.executeQuery();
                start6 += 10; end6 += 10;
                start  -= 10; end  -= 10;
                if (!resultSet6.next()){
                    resultSet6.close();
                    text = "Заявка руководителя уже принята";
                    selectApplicationStatement.setInt(1, 7);
                    selectApplicationStatement.setInt(2, start7);
                    selectApplicationStatement.setInt(3, end7);
                    ResultSet resultSet7 = selectApplicationStatement.executeQuery();
                    start7 += 10; end7 += 10;
                    start6 -= 10; end6 -= 10;
                    if (!resultSet7.next()){
                        resultSet7.close();
                        text = "Заявка подана после дедлайна";
                        selectApplicationStatement.setInt(1, 8);
                        selectApplicationStatement.setInt(2, start8);
                        selectApplicationStatement.setInt(3, end8);

                        ResultSet resultSet8 = selectApplicationStatement.executeQuery();
                        if (!resultSet8.next()) {
                            resultSet8.close();
                            start7 -= 10; end7 -= 10;
                            start8 += 10; end8 += 10;
                        } else
                            resultSet = resultSet8;
                    } else
                        resultSet = resultSet7;
                } else
                    resultSet = resultSet6;
            } else
                resultSet = resultSet5;

            while (resultSet != null && resultSet.next()) {
                JPanel panel = new JPanel(new FlowLayout());

                JTextField id = new JTextField(4)
                         , amount = new JTextField(4)
                         , fio = new JTextField(16)
                         , theme = new JTextField(16)
                         , requestedSum = new JTextField(8)
                         , classifier = new JTextField(8);

                Window.setEditable(false, id, amount, fio, theme, requestedSum, classifier);

                id          .setText(resultSet.getString("id"));
                amount      .setText(resultSet.getString("amount"));
                fio         .setText(resultSet.getString("fio"));
                theme       .setText(resultSet.getString("theme"));
                requestedSum.setText(resultSet.getString("requested_sum"));
                classifier  .setText(resultSet.getString("classifier"));

                Window.addComponents(panel, id, amount, fio, theme, requestedSum, classifier);

                applications.add(panel);

                label.setText(text);
                applications.add(label);
                resultSet.beforeFirst();
            }
            if(resultSet != null) resultSet.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void getFinancialReport(JPanel applications){
        applications.removeAll();
        String sqlSelectApplication = "SELECT * FROM application WHERE status = 3 OR status = 4 OR status = 5 LIMIT ?, ?";
        try(PreparedStatement selectApplicationStatement = connection.prepareStatement(sqlSelectApplication)) {
            selectApplicationStatement.setInt(1, start);
            selectApplicationStatement.setInt(2, end);
            try(ResultSet resultSet = selectApplicationStatement.executeQuery()){
                while (resultSet.next()){
                    System.out.println(resultSet.getString(2));

                    JPanel panel = new JPanel(new FlowLayout());

                    JTextField id = new JTextField(4)
                            , amount = new JTextField(4)
                            , fio = new JTextField(16)
                            , theme = new JTextField(16)
                            , requestedSum = new JTextField(8)
                            , allottedSum = new JTextField(8)
                            , classifier = new JTextField(8);

                    Window.setEditable(false, id, amount, fio, theme, requestedSum, allottedSum, classifier);

                    id          .setText(resultSet.getString("id"));
                    amount      .setText(resultSet.getString("amount"));
                    fio         .setText(resultSet.getString("fio"));
                    theme       .setText(resultSet.getString("theme"));
                    requestedSum.setText(resultSet.getString("requested_sum"));
                    allottedSum .setText(resultSet.getString("allotted_sum"));
                    classifier  .setText(resultSet.getString("classifier"));

                    Window.addComponents(panel, id, amount, fio, theme, requestedSum, classifier);
                    applications.add(panel);
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    private String generateReportRows(ResultSet applications) throws SQLException {
        String text = "---------------------------------------------\n";
        while (applications.next()){
            text += applications.getString("fio")           + " | ";
            text += applications.getString("theme")         + " | ";
            text += applications.getString("classifier")    + " | ";
            text += applications.getString("requested_sum") + " | ";
            String allottedSum = applications.getString("allotted_sum");
            if (allottedSum == null) allottedSum = "0";
            text += allottedSum + "\n";
        }
        text += "---------------------------------------------\n";
        return text;
    }
    private void writeReportRows(PreparedStatement selectApplicationStatement, String text, File file) throws SQLException {
        try(ResultSet applications = selectApplicationStatement.executeQuery()) {
            text += generateReportRows(applications);
        }
        writeMessage(file, text);
    }
    public boolean financial(){
        String path = "reports/Financial";
        String text = "ФИНАНСОВФЫЙ ОТЧЕТ\n";

        File file = createFile(path);
        if (file == null) return false;

        try(Statement selectYearStatement = connection.createStatement()) {
            try(ResultSet years = selectYearStatement.executeQuery("SELECT * FROM year")) {
                while (years.next()){
                    text += years.getString("year") + "\n";
                    text += "budget: " + years.getString("budget") + "\n";
                    String sqlSelectApplication = "SELECT * FROM application WHERE YEAR(date) = ? AND status = ?";
                    try(PreparedStatement selectApplicationStatement = connection.prepareStatement(sqlSelectApplication)) {
                        selectApplicationStatement.setInt(1, years.getInt("year"));
                        selectApplicationStatement.setInt(2, 1);
                        text += "Принято\n";
                        writeReportRows(selectApplicationStatement, text, file);
                        text = "Провалено\n";
                        selectApplicationStatement.setInt(2, 2);
                        writeReportRows(selectApplicationStatement, text, file);
                        text = "В процессе\n";
                        selectApplicationStatement.setInt(2, 4);
                        writeReportRows(selectApplicationStatement, text, file);
                        text = "Завершено\n";
                        selectApplicationStatement.setInt(2, 3);
                        writeReportRows(selectApplicationStatement, text, file);
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
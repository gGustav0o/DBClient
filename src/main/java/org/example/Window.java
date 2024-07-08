package org.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Window {
    private final JFrame frame;
    private final Container container;
    private final DB db;
    public Window(DB db){

        this.db = db;

        frame = new JFrame("Российский фонд фундаментальных исследований");
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                db.closeConnection();
                System.exit(0);
            }
        });

        container = frame.getContentPane();
        container.setLayout(new FlowLayout( FlowLayout.LEFT, 10, 10));
        frame.setContentPane(container);
        frame.setVisible(true);

        String         date;
        boolean        isOk;
        java.util.Date dateParsed;

        do {
            date = JOptionPane.showInputDialog("Введите текущую дату");
            try {
                isOk       = true;
                dateParsed = new SimpleDateFormat("dd.MM.yyyy").parse(date);
            } catch (ParseException parseException){
                parseException.printStackTrace();
                isOk = false;
                optionPaneMessage("Неверно введена дата!", "Дата");
                continue;
            }

            if (!db.setDate(new java.sql.Date(dateParsed.getTime()))){
                isOk = false;
                optionPaneMessage("Не указана дата завершегия приема заявок!", "Дата");
            }
        }  while (!isOk);

        if (date == null) System.exit(1);

        results();
    }
    private void optionPaneMessage(String message, String title){
        JOptionPane.showMessageDialog(
                frame
                , message
                , title
                , JOptionPane.INFORMATION_MESSAGE
        );
    }
    public static void setEditable(boolean editable, JTextField ... fields){
        for (JTextField i: fields)
            i.setEditable(editable);
    }
    public static void addComponents(Container container, JComponent... components){
        for (JComponent i: components)
            container.add(i);
    }
    public static void displayApplications(JPanel applications, String... values){
        JPanel panel = new JPanel(new FlowLayout());

        for (String i: values){
            JTextField iField = new JTextField();
            iField.setEditable(false);
            iField.setText(i);
            panel.add(iField);
        }

        applications.add(panel);
    }
    private void addComponents(JPanel panel, JComponent... components){
        for (JComponent i: components)
            panel.add(i);
    }
    private void clear(){
        db.start = 0;
        db.end   = 10;

        container.removeAll();
        container.setLayout(new BorderLayout());
    }
    private void renewComponent(JComponent component){
        component.revalidate();
        component.repaint();
    }
    private void renewContainer(){
        container.revalidate();
        container.repaint();
    }
    private void renewContainer(JComponent component){
        component.revalidate();
        component.repaint();
        container.revalidate();
        container.repaint();
    }
    private void results(){
        if (db.results()) {
            optionPaneMessage("Введите результаты работ!", "Время проведения исследований");
            writeResults();
        } else mainMenu();
    }
    private void updateBox(JComboBox<String> box, DataBaseObject[] array, String input, String table){
        box.removeAllItems();
        DataBaseObject[] searchResults = db.getObjects(input, table);

        for (int i = 0; i < 10; i++){
            if (searchResults[i] == null) break;
            array[i] = searchResults[i];
            box.addItem(searchResults[i].name);
        }
        renewContainer(box);
    }
    private int getId(){
        String strID = JOptionPane.showInputDialog("Введите id");
        if (strID == null){
            System.err.println("Window/getId/nullId");
            return -1;
        }
        int id = 0;
        try {
            id = Integer.parseInt(strID);
        } catch (NumberFormatException ex){
            ex.printStackTrace();
            optionPaneMessage("Ошибка при вводе id", "id");
        }
        return id;
    }
    public void mainMenu(){

        container.removeAll();
        container.setLayout(new FlowLayout());
        container.revalidate();

        JButton    newApplication    = new JButton("Новая заявка")
              ,    view              = new JButton("Рассмотрение заявок")
              ,    reports           = new JButton("Отчеты")
              ,    writeResults      = new JButton("Результаты исследований");

        JPanel alignmentPanel = new JPanel(new FlowLayout());
        alignmentPanel.setBorder(BorderFactory.createTitledBorder("Главное меню"));

        view          .addActionListener(e -> consider());
        reports       .addActionListener(e -> reports());
        writeResults  .addActionListener(e -> writeResults());
        newApplication.addActionListener(e -> newApplication());

        addComponents(alignmentPanel, newApplication, view, reports, writeResults);
        container.add(alignmentPanel);
        renewContainer();
    }
    private void writeResults(){

        container.removeAll();
        container.setLayout(new VerticalLayout());
        container.revalidate();

        JButton mainMenu = new JButton("Главное меню");
        mainMenu.addActionListener(e -> results());

        container.add(mainMenu);

        JPanel search  = new JPanel(new FlowLayout());

        JTextField name = new JTextField(16);
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setPrototypeDisplayValue("################");

        JButton success = new JButton("Успех!")
              , fail    = new JButton("Провал!");

        success.addActionListener(e -> {
            String n = (String)comboBox.getSelectedItem();
            if (n == null)
                optionPaneMessage("Введите имя!", "Результаты работ");
            else {
                if (db.setResult(n, true))
                    optionPaneMessage("Успешно!" ,"Результаты работ");
                else
                    optionPaneMessage("Ошибка!" ,"Результаты работ");
            }
        });

        fail.addActionListener(e -> {
            String n = (String)comboBox.getSelectedItem();
            if (n == null)
                optionPaneMessage("Введите имя!", "Результаты работ");
            else{
                if (db.setResult(n, false))
                    optionPaneMessage("Успешно!" ,"Результаты работ");
                else
                    optionPaneMessage("Ошибка!" ,"Результаты работ");
            }
        });

        name.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        comboBox.removeAllItems();
                        String input = name.getText();
                        String[] searchResults = db.getCompleted(input);
                        for (int i = 0; i < 10; i++){
                            comboBox.addItem(searchResults[i]);
                        }
                        renewComponent(comboBox);
                        renewContainer();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        comboBox.removeAllItems();
                        String input = name.getText();
                        String[] searchResults = db.getCompleted(input);
                        for (int i = 0; i < 10; i++){
                            comboBox.addItem(searchResults[i]);
                        }
                        renewComponent(comboBox);
                        renewContainer();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {

                    }
                }
        );

        addComponents(search, name, comboBox, success, fail);
        addComponents(container, search);
        renewContainer();
    }
    private void newApplication(){

        container.removeAll();
        container.setLayout(new GridLayout());

        JPanel     alignmentPanel    = new JPanel(new VerticalLayout())
                 , researchPanel     = new JPanel(new FlowLayout())
                 , organisationPanel = new JPanel(new FlowLayout());

        JTextField fio               = new JTextField(45)
                 , date              = new JTextField(15)
                 , amount            = new JTextField(3)
                 , theme             = new JTextField(45)
                 , reqSum            = new JTextField(45)
                 , email             = new JTextField(64)
                 , classifier        = new JTextField(3)
                 , organisation      = new JTextField(3);

        JLabel     fioLabel          = new JLabel("ФИО руководителя")
                 , dateLabel         = new JLabel("Дата подачи заявки")
                 , amountLabel       = new JLabel("Количество участников проекта")
                 , themeLabel        = new JLabel("Тема проекта")
                 , reqSumLabel       = new JLabel("Запрашиваемая сумма")
                 , mailLabel         = new JLabel("Email")
                 , classifierLabel   = new JLabel("Классификатор исследования")
                 , organisationLabel = new JLabel("Организация");

        fio         .setToolTipText("ФИО руководителя");
        date        .setToolTipText("Дата подачи заявки");
        amount      .setToolTipText("Количество участников проекта");
        theme       .setToolTipText("Тема проекта");
        reqSum      .setToolTipText("Запрашиваемая сумма");
        email       .setToolTipText("Email");
        classifier  .setToolTipText("Классификатор исследования");
        organisation.setToolTipText("Организация");

        JComboBox<String> researchBox     = new JComboBox<>();
        JComboBox<String> organisationBox = new JComboBox<>();

        addComponents(researchPanel, classifier, researchBox);
        addComponents(organisationPanel, organisation, organisationBox);

        addComponents(alignmentPanel
                , dateLabel
                , date
                , fioLabel
                , fio
                , amountLabel
                , amount
                , reqSumLabel
                , reqSum
                , themeLabel
                , theme
                , mailLabel
                , email
                , classifierLabel
                , researchPanel
                , organisationLabel
                , organisationPanel
        );

        Application application = new Application();
        application.classifier = -1;
        application.organisation = -1;

        date.addActionListener(e -> {
            try {
                java.util.Date applicationDate = new SimpleDateFormat("dd.MM.yyyy").parse(date.getText());
                application.date =  new java.sql.Date(applicationDate.getTime());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });

        fio   .addActionListener(e -> application.fio    = fio  .getText());
        theme .addActionListener(e -> application.theme  = theme.getText());
        email .addActionListener(e -> application.email  = email.getText());
        amount.addActionListener(e -> application.amount = Integer.parseInt(amount.getText()));
        reqSum.addActionListener(e -> application.reqSum = Integer.parseInt(reqSum.getText()));

        DataBaseObject[] org = new DataBaseObject[10];
        DataBaseObject[] res = new DataBaseObject[10];
        updateBox(organisationBox, org, "", "organisation");
        updateBox(researchBox,     res, "", "research");

        organisationBox.addActionListener(e -> {
            String item = (String)organisationBox.getSelectedItem();
            if(item == null) return;
            for (DataBaseObject i: org){
                if(i != null && i.name.equals(item)){
                    application.organisation = i.id;
                    break;
                }
            }
        });

        researchBox.addActionListener(e -> {
            String item = (String)researchBox.getSelectedItem();
            if(item == null) return;
            for (DataBaseObject i: res){
                if(i != null && i.name.equals(item)){
                    application.classifier = i.id;
                    break;
                }
            }
        });

        classifier.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateBox(researchBox, res, classifier.getText(), "research");
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateBox(researchBox, res, classifier.getText(), "research");
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {

                    }
                }
        );

        organisation.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void changedUpdate(DocumentEvent e) {}

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateBox(organisationBox, org, organisation.getText(), "organisation");
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateBox(organisationBox, org, organisation.getText(), "organisation");
                    }
                }
        );

        JButton ok       = new JButton("Подтвердить")
              , mainMenu = new JButton("Главное меню");

        mainMenu.addActionListener(e -> mainMenu());

        ok.addActionListener(e -> {
            application.fio = fio.getText();
            /*
                java.util.Date javaDate = new java.util.Date();
                application.date =  new java.sql.Date(javaDate.getTime());
            */
            if (application.organisation == -1 || application.classifier == -1)
                optionPaneMessage("Выберете классификатор и организацию", "Классификатор и организация");
            java.util.Date applicationDate = null;
            try {
                applicationDate = new SimpleDateFormat("dd.MM.yyyy").parse(date.getText());
            } catch (ParseException ex) {
                ex.printStackTrace();
                optionPaneMessage("Неверно введена дата!", "Дата");
            }
            assert applicationDate != null;
            application.date =  new java.sql.Date(applicationDate.getTime());

            try {
                application.amount = Integer.parseInt(amount.getText());
                application.reqSum = Integer.parseInt(reqSum.getText());
            } catch (NumberFormatException ex){
                optionPaneMessage("Ошибка ввода", "Добавление заявки");
            }
            application.theme = theme.getText();
            application.email = email.getText();


            String message = "";
            String title   = "Добавление заявки";
            switch (db.addApplication(application)){
                case OK             -> message = "Добавлено!";
                case AFTER_DEADLINE -> message = "Заявка подана слишком поздно!";
                case FAILED_IN_PAST -> message = "Руководитель имеет проваленные проекты!";
                case SQL_ERROR      -> message = "Ошибка добавления";
            }
            optionPaneMessage(message, title);
            mainMenu();
        });

        alignmentPanel.add(ok);
        alignmentPanel.add(mainMenu);
        alignmentPanel.setBorder(BorderFactory.createTitledBorder("Данные заявки"));

        container.add(alignmentPanel);
        frame.setContentPane(container);
    }
    private void addTitle(JPanel title){

        JTextField id           = new JTextField(4)
                 , amount       = new JTextField(4)
                 , fio          = new JTextField(16)
                 , theme        = new JTextField(16)
                 , requestedSum = new JTextField(8)
                 , classifier   = new JTextField(8);

        setEditable(false
                , id
                , amount
                , fio
                , theme
                , requestedSum
                , classifier
        );

        id          .setText("id");
        amount      .setText("amount");
        fio         .setText("fio");
        theme       .setText("theme");
        requestedSum.setText("requested sum");
        classifier  .setText("classifier");

        addComponents(title
                , id
                , amount
                , fio
                , theme
                , requestedSum
                , classifier
        );


        title.setBackground(Color.PINK);
        title.setSize(new Dimension(500, 20));

    }
    private void consider(){
        clear();

        JPanel controls = new JPanel(new GridLayout(2, 1));
        JButton mainMenu = new JButton("Главное меню");
        controls.add(mainMenu);

        JButton prev   = new JButton("<")
              , accept = new JButton("Принять заявку")
              , deny   = new JButton("Отклонить заявку")
              , next   = new JButton(">");

        JPanel panel = new JPanel(new BorderLayout())
             , title = new JPanel(new FlowLayout());

        addTitle(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel navigation = new JPanel(new FlowLayout());
        addComponents(navigation, prev, accept, deny, next);

        Color color = Color.PINK;
        controls.setBackground(color);
        controls.setSize(new Dimension(500, 20));
        panel   .setSize(new Dimension(300, 700));
        controls.add(navigation);
        container.add(controls, BorderLayout.NORTH);
        container.add(panel   , BorderLayout.CENTER);

        renewContainer();

        JPanel applications = new JPanel(new GridLayout(10, 1));

        mainMenu.addActionListener(e -> mainMenu());
        accept.addActionListener(e -> {
            int id = getId();
            String message = "";
            String title1 = "Рассмотрение заявки";
            switch (db.accept(id)){
                case OK                                  -> message = "Заявка принята!";
                case SQL_ERROR                           -> message = "Ошибка ввода!";
                case APPLICATION_OF_HEAD_ALREADY_APPLIED -> message = "Руководителю уже одобрили другую заявку!";
            }
            optionPaneMessage(message, title1);
        });

        deny.addActionListener(e -> {
            int id = getId();
            String message = "";
            String title12 = "Рассмотрение заявки";
            switch (db.deny(id)){
                case OK        -> message = "Заявка Отклонена!";
                case SQL_ERROR -> message = "Ошибка ввода!";
            }
            JOptionPane.showMessageDialog(frame, message, title12, JOptionPane.INFORMATION_MESSAGE);
        });

        next.addActionListener(e -> {
            try {
                db.getPendingApplications(applications);
                panel.add(applications, BorderLayout.CENTER);

                renewContainer();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

        prev.addActionListener(e -> {
            try {
                if (db.start == 0) return;
                db.start -= 20;
                db.end   -= 20;
                if (db.start < 0) {
                    db.start += 10;
                    db.end   += 10;
                    return;
                }
                db.getPendingApplications(applications);
                panel.add(applications, BorderLayout.CENTER);
                renewContainer();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });

    }
    private void financial(){
        if (db.financial())
            optionPaneMessage("Отчет создан", "Финансовый отчет");
        else
            optionPaneMessage("Ошибка при создании отчета", "Финансовый отчет");
    }
    private void accepted(){
        clear();

        JButton mainMenu = new JButton("Главное меню");

        JPanel controls = new JPanel(new GridLayout(2, 1));
        controls.add(mainMenu);

        JButton prev = new JButton("<")
              , next = new JButton(">");

        JLabel label = new JLabel();
        label.setText("Принятые заявки");

        JPanel panel = new JPanel(new BorderLayout())
             , title = new JPanel(new FlowLayout());

        addTitle(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel navigation = new JPanel(new FlowLayout());
        addComponents(navigation, prev, label, next);

        controls.setSize(new Dimension(500, 20));
        panel   .setSize(new Dimension(300, 700));
        controls.add(navigation);
        container.add(controls, BorderLayout.NORTH);
        container.add(panel   , BorderLayout.CENTER);

        renewContainer();

        JPanel applications = new JPanel(new GridLayout(10, 1));

        mainMenu.addActionListener(e -> mainMenu());
        mainMenu.addActionListener(e -> mainMenu());
        prev.addActionListener(e -> {
            try {
                db.start -= 10;
                db.end   -= 10;
                if (db.start < 0) {
                    db.start += 10;
                    db.end   += 10;
                    return;
                }
                db.getAcceptedApplications(applications);
                panel.add(applications, BorderLayout.CENTER);
                renewContainer();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
        next.addActionListener(e -> {
            try {
                db.getAcceptedApplications(applications);
                panel.add(applications, BorderLayout.CENTER);

                renewContainer();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }
    private void declined(){
        clear();

        JButton mainMenu = new JButton("Главное меню");

        JPanel controls = new JPanel(new GridLayout(2, 1));
        controls.add(mainMenu);

        JButton prev = new JButton("<")
              , next = new JButton(">");

        JLabel label = new JLabel();
        label.setText("Отклоненный заявки");

        JPanel panel = new JPanel(new BorderLayout())
             , title = new JPanel(new FlowLayout());

        addTitle(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel navigation = new JPanel(new FlowLayout());
        addComponents(navigation, prev, label, next);

        controls.setSize(new Dimension(500, 20));
        panel   .setSize(new Dimension(300, 700));
        controls.add(navigation);
        container.add(controls, BorderLayout.NORTH);
        container.add(panel   , BorderLayout.CENTER);

        renewContainer();

        JPanel applications = new JPanel(new GridLayout(11, 1));

        mainMenu.addActionListener(e -> mainMenu());
        mainMenu.addActionListener(e -> mainMenu());
        prev.addActionListener(e -> {
            if (db.start == 0) return;
            try {
                if (db.start8 > 0){
                    db.start8 -= 20;
                    db.end8   -= 20;
                }
                else if (db.start7 > 0) {
                    db.start7 -= 20;
                    db.end7   -= 20;
                }
                else  if (db.start6 > 0){
                    db.start6 -= 20;
                    db.end6   -= 20;
                }
                else {
                    db.start -= 20;
                    db.end   -= 20;
                }

                if (db.start < 0) {
                    db.start += 10;
                    db.end   += 10;
                }
                if (db.start6 < 0) {
                    db.start6 += 10;
                    db.end6   += 10;
                }
                if (db.start7 < 0) {
                    db.start7 += 10;
                    db.end7   += 10;
                }
                if (db.start8 < 0) {
                    db.start8 += 10;
                    db.end8   += 10;
                }

                db.getDeniedApplications(applications);
                panel.add(applications, BorderLayout.CENTER);
                renewContainer();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
        next.addActionListener(e -> {
            try {
                db.getDeniedApplications(applications);
                panel.add(applications, BorderLayout.CENTER);
                renewContainer();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        });
    }
    private void reports(){
        db.start = 0;
        db.end = 10;

        container.removeAll();
        container.setLayout(new FlowLayout());

        JButton financial = new JButton("Финансовый отчет")
              , accepted  = new JButton("Принятые заявки")
              , denied    = new JButton("Отклоненные заявки");

        addComponents(container, financial, accepted, denied);
        renewContainer();

        financial.addActionListener(e -> financial());
        accepted .addActionListener(e -> accepted());
        denied   .addActionListener(e -> declined());
    }
}

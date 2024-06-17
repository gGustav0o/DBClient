package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Window {
    private final JFrame frame;
    private final Container container;
    private final DB db;
    public Window(DB db){

        this.db = db;

        frame = new JFrame("Российский фонд фундаментальных исследований");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);

        container = frame.getContentPane();
        container.setLayout(new FlowLayout( FlowLayout.LEFT, 10, 10));

        String date;
        java.util.Date dateParsed;
        boolean isOk = true;

        do {
            dateParsed = null;
            date = JOptionPane.showInputDialog("Введите текущую дату");

            try {
                isOk = true;
                dateParsed = new SimpleDateFormat("dd.MM.yyyy").parse(date);
            } catch (ParseException e){
                e.printStackTrace();
                isOk = false;
                JOptionPane.showMessageDialog(frame, "Неверно введена дата!", "Дата", JOptionPane.INFORMATION_MESSAGE);
                continue;
            }

            if (!db.setDate(new java.sql.Date(dateParsed.getTime()))){
                isOk = false;
                JOptionPane.showMessageDialog(frame, "Не указана дата завершегия приема заявок!", "Дата", JOptionPane.INFORMATION_MESSAGE);
            }
        }  while (!isOk);


        if (date == null) System.exit(1);
        /*if (db.results()) {
            frame.setContentPane(container);
            frame.setVisible(true);
            results();
        }*/
        else mainMenu();
    }
    private void results(){
        JOptionPane.showMessageDialog(null, "Введите результаты работ!", "Время проведения исследований", JOptionPane.INFORMATION_MESSAGE);
        writeResults();
    }
    private void writeResults(){
        container.removeAll();
        container.setLayout(new VerticalLayout());

        JButton mainMenu = new JButton("Главное меню");
        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /*if (db.results()){
                    JOptionPane.showMessageDialog(null, "Введите результаты работ!", "Время проведения исследований", JOptionPane.INFORMATION_MESSAGE);
                } else mainMenu();*/
                mainMenu();
            }
        });

        container.add(mainMenu);

        JPanel search = new JPanel(new FlowLayout());
        JPanel buttons = new JPanel(new FlowLayout());
        JTextField name = new JTextField(16);
        JComboBox<String> s = new JComboBox<String>();
        s.setPrototypeDisplayValue("################");

        JButton success = new JButton("Успех!");
        JButton fail = new JButton("Провал!");

        success.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String n = (String)s.getSelectedItem();
                if (n == null)
                    JOptionPane.showMessageDialog(null, "Введите имя!", "Результаты работ", JOptionPane.INFORMATION_MESSAGE);
                else {
                    if (db.setResult(n, true))
                        JOptionPane.showMessageDialog(null, "Успешно!", "Результаты работ", JOptionPane.INFORMATION_MESSAGE);
                    else
                        JOptionPane.showMessageDialog(null, "Ошибка!", "Результаты работ", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        fail.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String n = (String)s.getSelectedItem();
                if (n == null)
                    JOptionPane.showMessageDialog(null, "Введите имя!", "Результаты работ", JOptionPane.INFORMATION_MESSAGE);
                else{
                    if (db.setResult(n, false))
                        JOptionPane.showMessageDialog(null, "Успешно!", "Результаты работ", JOptionPane.INFORMATION_MESSAGE);
                    else
                        JOptionPane.showMessageDialog(null, "Ошибка!", "Результаты работ", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                s.removeAllItems();
                String input = name.getText();
                String[] searchResults = db.getCompleted(input);
                for (int i = 0; i < 10; i++){
                    s.addItem(searchResults[i]);
                    System.out.println(searchResults[i]);
                }
                s.revalidate();
                s.repaint();
                container.revalidate();
                container.repaint();
            }
        });

        search.add(name);
        search.add(s);
        buttons.add(success);
        buttons.add(fail);

        container.add(search);
        container.add(buttons);

        container.revalidate();
        container.repaint();
    }
    public void mainMenu(){
        container.removeAll();
        container.setLayout(new FlowLayout());
        container.revalidate();
        JButton newApplication = new JButton("Новая заявка");
        JButton view = new JButton("Рассмотрение заявок");
        JButton reports = new JButton("Отчеты");
        JButton writeResults = new JButton("Результаты исследований");
        JPanel alignmentPanel = new JPanel(new FlowLayout());
        alignmentPanel.setBorder(BorderFactory.createTitledBorder("Главное меню"));

        writeResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeResults();
            }
        });
        newApplication.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newApplication();
            }
        });
        view.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consider();
            }
        });
        reports.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reports();
            }
        });


        alignmentPanel.add(newApplication);
        alignmentPanel.add(view);
        alignmentPanel.add(reports);
        alignmentPanel.add(writeResults);
        container.add(alignmentPanel);
        frame.setContentPane(container);
        frame.setVisible(true);

    }
    private void newApplication(){
        container.removeAll();
        container.setLayout(new GridLayout());
        JPanel alignmentPanel = new JPanel(new VerticalLayout());
        JPanel researchPanel = new JPanel(new FlowLayout());
        JPanel organisationPanel = new JPanel(new FlowLayout());

        JTextField fio = new JTextField(45),
                date = new JTextField(15),
                amount = new JTextField(3),
                theme = new JTextField(45),
                reqSum = new JTextField(45),
                email = new JTextField(64),
                classifier = new JTextField(3),
                organisation = new JTextField(3);

        JLabel fioLabel = new JLabel("ФИО руководителя");
        JLabel dateLabel = new JLabel("Дата подачи заявки");
        JLabel amountLabel = new JLabel("Количество участников проекта");
        JLabel themeLabel = new JLabel("Тема проекта");
        JLabel reqSumLabel = new JLabel("Запрашиваемая сумма");
        JLabel mailLabel = new JLabel("Email");
        JLabel classifierLabel = new JLabel("Классификатор исследования");
        JLabel organisationLabel = new JLabel("Организация");

        fio.setToolTipText("ФИО руководителя");
        date.setToolTipText("Дата подачи заявки");
        amount.setToolTipText("Количество участников проекта");
        theme.setToolTipText("Тема проекта");
        reqSum.setToolTipText("Запрашиваемая сумма");
        email.setToolTipText("Email");
        classifier.setToolTipText("Классификатор исследования");
        organisation.setToolTipText("Организация");

        JComboBox<String> researchBox = new JComboBox<String>();
        JComboBox<String> organisationBox = new JComboBox<String>();
        researchPanel.add(classifier);
        researchPanel.add(researchBox);
        organisationPanel.add(organisation);
        organisationPanel.add(organisationBox);

        alignmentPanel.add(dateLabel);
        alignmentPanel.add(date);
        alignmentPanel.add(fioLabel);
        alignmentPanel.add(fio);
        alignmentPanel.add(amountLabel);
        alignmentPanel.add(amount);
        alignmentPanel.add(reqSumLabel);
        alignmentPanel.add(reqSum);
        alignmentPanel.add(themeLabel);
        alignmentPanel.add(theme);
        alignmentPanel.add(mailLabel);
        alignmentPanel.add(email);
        alignmentPanel.add(classifierLabel);
        alignmentPanel.add(researchPanel);
        alignmentPanel.add(organisationLabel);
        alignmentPanel.add(organisationPanel);

        Application application = new Application();
        application.classifier = -1;
        application.organisation = -1;

        date.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.util.Date date1 = new SimpleDateFormat("dd.MM.yyyy").parse(date.getText());
                    application.date =  new java.sql.Date(date1.getTime());
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });
        fio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                    application.fio = fio.getText();
            }
        });
        amount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.amount = Integer.parseInt(amount.getText());
            }
        });
        reqSum.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.reqSum = Integer.parseInt(reqSum.getText());
            }
        });
        theme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.theme = theme.getText();
            }
        });
        email.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                application.email = email.getText();
            }
        });
        Clazz[] org = new Clazz[10];
        Clazz[] res = new Clazz[10];

        organisationBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String)organisationBox.getSelectedItem();
                for (Clazz i: org){
                    if(i.s.equals(item)){
                        application.organisation = i.n;
                        break;
                    };
                }
            }
        });
        researchBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String)researchBox.getSelectedItem();
                for (Clazz i: res){
                    if(i.s.equals(item)){
                        application.classifier = i.n;
                        break;
                    };
                }
            }
        });
        classifier.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                researchBox.removeAllItems();
                String input = classifier.getText();
                Clazz[] searchResults = db.getResearches(input);

                for (int i = 0; i < 10; i++){
                    if (searchResults[i] == null) break;
                    res[i] = searchResults[i];
                    researchBox.addItem(searchResults[i].s);
                    System.out.println(searchResults[i]);
                }
                researchBox.revalidate();
                researchBox.repaint();
                container.revalidate();
                container.repaint();
            }
        });

        organisation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                organisationBox.removeAllItems();
                String input = organisation.getText();
                Clazz[] searchResults = db.getOrganisations(input);

                for (int i = 0; i < 10; i++){
                    if (searchResults[i] == null) break;
                    org[i] = searchResults[i];
                    organisationBox.addItem(searchResults[i].s);
                    System.out.println(searchResults[i]);
                }
                organisationBox.revalidate();
                organisationBox.repaint();
                container.revalidate();
                container.repaint();
            }
        });
        JButton ok = new JButton("Подтвердить");
        JButton mainMenu = new JButton("Главное меню");
        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                application.fio = fio.getText();
                //java.util.Date javaDate = new java.util.Date();
                //application.date =  new java.sql.Date(javaDate.getTime());
                if (application.organisation == -1 || application.classifier == -1)
                    JOptionPane.showMessageDialog(frame, "Выберете классификатор и организацию", "Классификатор и организация", JOptionPane.INFORMATION_MESSAGE);
                java.util.Date date1 = null;
                try {
                    date1 = new SimpleDateFormat("dd.MM.yyyy").parse(date.getText());
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Неверно введена дата!", "Дата", JOptionPane.INFORMATION_MESSAGE);
                }
                assert date1 != null;
                application.date =  new java.sql.Date(date1.getTime());

                try {
                    application.amount = Integer.parseInt(amount.getText());
                    application.reqSum = Integer.parseInt(reqSum.getText());
                } catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(frame, "Ошибка ввода", "Добавление заявки", JOptionPane.INFORMATION_MESSAGE);
                }
                application.theme = theme.getText();
                application.email = email.getText();

                //application.classifier = Integer.parseInt(classifier.getText());
                //application.organisation = Integer.parseInt(organisation.getText());
                String message = "";
                String title = "Добавление заявки";
                switch (db.addApplication(application)){
                    case OK -> message = "Добавлено!";
                    case AFTER_DEADLINE -> message = "Заявка подана слишком поздно!";
                    case FAILED_IN_PAST -> message = "Руководитель имеет проваленные проекты!";
                    case SQL_ERROR -> message = "Ошибка добавления";
                }
                JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
                mainMenu();
            }
        });
        alignmentPanel.add(ok);
        alignmentPanel.add(mainMenu);
        alignmentPanel.setBorder(BorderFactory.createTitledBorder("Данные заявки"));
        container.add(alignmentPanel);
        frame.setContentPane(container);
    }
    private void addTitle(JPanel title){
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

        id.setText("id");
        amount.setText("amount");
        fio.setText("fio");
        theme.setText("theme");
        requestedSum.setText("requested sum");
        classifier.setText("classifier");

        title.add(id);
        title.add(amount);
        title.add(fio);
        title.add(theme);
        title.add(requestedSum);
        title.add(classifier);
        title.setBackground(Color.PINK);
        title.setSize(new Dimension(500, 20));
    }
    private void addTitleFin(JPanel title){
        JTextField id = new JTextField(4);
        JTextField amount = new JTextField(4);
        JTextField fio = new JTextField(16);
        JTextField theme = new JTextField(16);
        JTextField requestedSum = new JTextField(8);
        JTextField allottedSum = new JTextField(8);
        JTextField classifier = new JTextField(8);

        id.setEditable(false);
        amount.setEditable(false);
        fio.setEditable(false);
        theme.setEditable(false);
        requestedSum.setEditable(false);
        allottedSum.setEditable(false);
        classifier.setEditable(false);

        id.setText("id");
        amount.setText("amount");
        fio.setText("fio");
        theme.setText("theme");
        requestedSum.setText("requested sum");
        allottedSum.setText("allotted sum");
        classifier.setText("classifier");

        title.add(id);
        title.add(amount);
        title.add(fio);
        title.add(theme);
        title.add(requestedSum);
        title.add(allottedSum);
        title.add(classifier);
        title.setBackground(Color.PINK);
        title.setSize(new Dimension(500, 20));
    }
    private void clear(){
        db.start = 0;
        db.end = 10;

        container.removeAll();
        container.setLayout(new BorderLayout());
    }
    private void consider(){
        clear();

        JPanel p = new JPanel(new GridLayout(2, 1));
        JButton mainMenu = new JButton("Главное меню");
        p.add(mainMenu);
        JButton prev = new JButton("<");
        JButton accept = new JButton("Принять заявку");
        JButton deny = new JButton("Отклонить заявку");
        JButton next = new JButton(">");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel title = new JPanel(new FlowLayout());

        addTitle(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel b = new JPanel(new FlowLayout());
        b.add(prev);
        b.add(accept);
        b.add(deny);
        b.add(next);

        Color c = Color.PINK;
        p.setBackground(c);
        p.setSize(new Dimension(500, 20));
        panel.setSize(new Dimension(300, 700));
        //panel.setBackground(Color.PINK);
        p.add(b);
        container.add(p, BorderLayout.NORTH);
        container.add(panel, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();

        JPanel applications = new JPanel(new GridLayout(10, 1));

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });
        accept.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String strID = JOptionPane.showInputDialog("Введите id");
                if (strID == null){
                    System.out.println("cancel");
                    return;
                }
                int id = 0;
                try {
                    id = Integer.parseInt(strID);
                } catch (NumberFormatException ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Ошибка при вводе id", "id", JOptionPane.INFORMATION_MESSAGE);
                }
                String message = "";
                String title = "Рассмотрение заявки";
                switch (db.accept(id)){
                    case OK -> message = "Заявка принята!";
                    case SQL_ERROR -> message = "Ошибка ввода!";
                    case APPLICATION_OF_HEAD_ALREADY_APPLIED -> message = "Руководителю уже одобрили другую заявку!";
                }
                JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deny.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String strID = JOptionPane.showInputDialog("Введите id");
                if (strID == null){
                    System.out.println("cancel");
                    return;
                }
                int id = 0;
                try {
                    id = Integer.parseInt(strID);
                } catch (NumberFormatException ex){
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Ошибка при вводе id", "id", JOptionPane.INFORMATION_MESSAGE);
                }
                String message = "";
                String title = "Рассмотрение заявки";
                switch (db.deny(id)){
                    case OK -> message = "Заявка Отклонена!";
                    case SQL_ERROR ->  message = "Ошибка ввода!";
                }
                JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //db.start += 10;
                    //db.end += 10;
                    db.getPendingApplications(applications);
                    panel.add(applications, BorderLayout.CENTER);

                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (db.start == 0) return;
                    db.start -= 20;
                    db.end -= 20;
                    if (db.start < 0) {
                        db.start += 10;
                        db.end += 10;
                        return;
                    }
                    db.getPendingApplications(applications);
                    panel.add(applications, BorderLayout.CENTER);
                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

    }
    private void financial(){
        if (db.financial())
            JOptionPane.showMessageDialog(frame, "Отчет создан", "Финансовый отчет", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, "Ошибка при создании отчета", "Финансовый отчет", JOptionPane.INFORMATION_MESSAGE);
        /*clear();

        JButton mainMenu = new JButton("Главное меню");

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(mainMenu);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JLabel label = new JLabel();
        label.setText("Финансовый отчет");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel title = new JPanel(new FlowLayout());

        addTitleFin(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel b = new JPanel(new FlowLayout());
        b.add(prev);
        b.add(label);
        b.add(next);

        p.setSize(new Dimension(500, 20));
        panel.setSize(new Dimension(300, 700));
        p.add(b);
        container.add(p, BorderLayout.NORTH);
        container.add(panel, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();

        JPanel applications = new JPanel(new GridLayout(10, 1));

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });*/

        /*prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    db.start -= 10;
                    db.end -= 10;
                    if (db.start < 0) {
                        db.start += 10;
                        db.end += 10;
                        return;
                    }
                    db.getFinancialReport(applications);
                    panel.add(applications, BorderLayout.CENTER);
                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    db.start += 10;
                    db.end += 10;
                    db.getFinancialReport(applications);
                    panel.add(applications, BorderLayout.CENTER);

                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });*/

    }
    private void accepted(){
        clear();

        JButton mainMenu = new JButton("Главное меню");

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(mainMenu);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JLabel label = new JLabel();
        label.setText("Принятые заявки");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel title = new JPanel(new FlowLayout());

        addTitle(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel b = new JPanel(new FlowLayout());
        b.add(prev);
        b.add(label);
        b.add(next);

        p.setSize(new Dimension(500, 20));
        panel.setSize(new Dimension(300, 700));
        p.add(b);
        container.add(p, BorderLayout.NORTH);
        container.add(panel, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();

        JPanel applications = new JPanel(new GridLayout(10, 1));

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });
        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    db.start -= 10;
                    db.end -= 10;
                    if (db.start < 0) {
                        db.start += 10;
                        db.end += 10;
                        return;
                    }
                    db.getAcceptedApplications(applications);
                    panel.add(applications, BorderLayout.CENTER);
                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    db.getAcceptedApplications(applications);
                    panel.add(applications, BorderLayout.CENTER);

                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });

        //JPanel panel = new JPanel(new FlowLayout());
    }
    private void declined(){
        clear();

        JButton mainMenu = new JButton("Главное меню");

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(mainMenu);
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JLabel label = new JLabel();
        label.setText("Отклоненный заявки");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel title = new JPanel(new FlowLayout());

        addTitle(title);

        panel.add(title, BorderLayout.NORTH);

        JPanel b = new JPanel(new FlowLayout());
        b.add(prev);
        b.add(label);
        b.add(next);

        p.setSize(new Dimension(500, 20));
        panel.setSize(new Dimension(300, 700));
        p.add(b);
        container.add(p, BorderLayout.NORTH);
        container.add(panel, BorderLayout.CENTER);

        container.revalidate();
        container.repaint();

        JPanel applications = new JPanel(new GridLayout(11, 1));

        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });
        mainMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenu();
            }
        });
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (db.start == 0) return;
                try {
                    if (db.start8 > 0){ db.start8 -= 20; db.end8 -= 20;}
                    else if (db.start7 > 0) {db.start7 -= 20; db.end7 -= 20;}
                    else  if (db.start6 > 0){db.start6 -= 20; db.end6 -= 20;}
                    else {db.start -= 20; db.end -= 20;}

                    if (db.start < 0) {
                        db.start += 10; db.end += 10;
                        //return;
                    }
                    if (db.start6 < 0) {
                        db.start6 += 10; db.end6 += 10;
                        //return;
                    }
                    if (db.start7 < 0) {
                        db.start7 += 10; db.end7 += 10;
                        //return;
                    }
                    if (db.start8 < 0) {
                        db.start8 += 10; db.end8 += 10;
                        //return;
                    }

                    db.getDeniedApplications(applications);
                    panel.add(applications, BorderLayout.CENTER);
                    container.revalidate();
                    container.repaint();
                    System.out.println(db.start + " " + db.end);
                    System.out.println(db.start6 + " " + db.end6);
                    System.out.println(db.start7 + " " + db.end7);
                    System.out.println(db.start8 + " " + db.end8 + "\n");
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    db.getDeniedApplications(applications);
                    panel.add(applications, BorderLayout.CENTER);

                    container.revalidate();
                    container.repaint();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                System.out.println(db.start + " " + db.end);
                System.out.println(db.start6 + " " + db.end6);
                System.out.println(db.start7 + " " + db.end7);
                System.out.println(db.start8 + " " + db.end8 + "\n");
            }
        });
    }
    private void reports(){
        db.start = 0;
        db.end = 10;

        container.removeAll();
        container.setLayout(new FlowLayout());

        JButton financial = new JButton("Финансовый отчет");
        JButton accepted = new JButton("Принятые заявки");
        JButton denied = new JButton("Отклоненные заявки");

        container.add(financial);
        container.add(accepted);
        container.add(denied);

        container.revalidate();
        container.repaint();


        financial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                financial();
            }
        });
        accepted.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accepted();
            }
        });
        denied.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                declined();
            }
        });
    }
}

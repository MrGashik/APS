package org.example;

import org.example.Tools.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private static final HashMap<Integer, Source> list_source = new HashMap<>();
    private static final HashMap<Integer, Device> list_device = new HashMap<>();
    private static final DefaultTableModel tableModel_source = new DefaultTableModel();
    private static final DefaultTableModel tableModel_buffer = new DefaultTableModel();
    private static final DefaultTableModel tableModel_device = new DefaultTableModel();
    private static final Point screen = new Point(1920, 1080);
    private static Generator gen;
    private static ProductionManager pdt_mng;
    private static Buffer buff;
    private static SelectionManager slt_mng;
    private static boolean mode = false;

    public static void main(String[] args) {
        //Таблица источников
        JFrame frame_source = new JFrame("Источники");
        frame_source.setBounds(0, 0, screen.x / 2, screen.y / 2);
        JTable table_source = new JTable(tableModel_source);
        tableModel_source.addColumn("Источники");
        tableModel_source.addColumn("Заявка");
        tableModel_source.addColumn("Кол-во сгенерированных заявок");
        tableModel_source.addColumn("Кол-во отклоненных заявок");
        tableModel_source.addColumn("Эффективность");
        JScrollPane pane_source = new JScrollPane(table_source);
        frame_source.add(pane_source);

        //Таблица буфера
        JFrame frame_buffer = new JFrame("Буфер");
        frame_buffer.setBounds(0, screen.y / 2, screen.x / 2, screen.y / 2);
        JTable table_buffer = new JTable(tableModel_buffer);
        tableModel_buffer.addColumn("Заявки");
        JScrollPane pane_buffer = new JScrollPane(table_buffer);
        frame_buffer.add(pane_buffer);

        //Таблица приборов
        JFrame frame_device = new JFrame("Приборы");
        frame_device.setBounds(screen.x / 2, 0, screen.x / 2, screen.y / 2);
        JTable table_device = new JTable(tableModel_device);
        tableModel_device.addColumn("Приборы");
        tableModel_device.addColumn("Заявки");
        tableModel_device.addColumn("Кол-во обработанных заявок");
        JScrollPane pane_device = new JScrollPane(table_device);
        frame_device.add(pane_device);

        //График
        JFrame graph = new JFrame("График");
        graph.setBounds(screen.x / 2, screen.y / 2, screen.x / 2, screen.y / 2);
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries s = new XYSeries("Сгенерированные заявки");
        XYSeries s2 = new XYSeries("Отклоненные заявки");
        XYSeries s3 = new XYSeries("Буфер");
        dataset.addSeries(s);
        dataset.addSeries(s2);
        dataset.addSeries(s3);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Количество заявок в тик",
                "",
                "Количество заявок",
                dataset,
                true,
                true,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.DARK_GRAY);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        NumberAxis numberAxis = new NumberAxis();
        numberAxis.setTickUnit(new NumberTickUnit(5));
        plot.setDomainAxis(numberAxis);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer renderer) {
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
            renderer.setDrawSeriesLineAsPath(true);
        }
        chart.setBackgroundPaint(Color.WHITE);
        chart.setPadding(new RectangleInsets(4, 8, 2, 2));
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(600, 300));
        graph.add(panel);

        //Показать экраны
        frame_source.setVisible(true);
        frame_buffer.setVisible(true);
        frame_device.setVisible(true);
        graph.setVisible(true);

        //Настройка параметров
        int size_buff = 0;
        double probability = 0.0;
        int count_source = 0;
        int count_device = 0;
        int time_device = 0;
        File file = new File("src/main/resources/Settings.txt");
        try (FileReader fr = new FileReader(file)) {
            BufferedReader reader = new BufferedReader(fr);
            String line;
            for (int i = 0; i < 6; i++) {
                line = reader.readLine();
                line = line.substring(line.indexOf('=') + 2);
                switch (i) {
                    case 0 -> {
                        if (line.equals("рука")) {
                            mode = true;
                        }
                    }
                    case 1 -> size_buff = Integer.parseInt(line);
                    case 2 -> probability = Double.parseDouble(line);
                    case 3 -> count_source = Integer.parseInt(line);
                    case 4 -> count_device = Integer.parseInt(line);
                    case 5 -> time_device = Integer.parseInt(line);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Кнопка для пошаговой работы
        JButton button = new JButton();
        if (mode) {
            button = createButton();
        }

        //Инициализация инструментов
        gen = new Generator(probability, count_source, time_device, s, s2, s3, mode, button);
        buff = new Buffer(size_buff, tableModel_buffer);
        pdt_mng = new ProductionManager(buff);
        slt_mng = new SelectionManager(buff, gen, mode);
        new Thread(slt_mng::run).start();
        for (int i = 0; i < count_device; i++) {
            createDevice();
        }
        for (int i = 0; i < count_source; i++) {
            createSource();
        }

        //Итог системы
        double sum_app = 0.0;
        double sum_fail = 0.0;
        for (int key : list_source.keySet()) {
            Source source = list_source.get(key);
            while (!source.getEnd()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            sum_app = sum_app + source.getCount_application();
            sum_fail = sum_fail + source.getCount_failure();
        }
        String str = "Всего отправлено: " + sum_app + "\n" +
                "Отказов: " + sum_fail + "\n" +
                "Процент эффективности системы: " + Math.round((1 - sum_fail / sum_app) * 100) + "%";
        JOptionPane.showMessageDialog(new JFrame(), str);

        //Эффективность каждого источника
        for (int key : list_source.keySet()) {
            tableModel_source.setValueAt(
                    Math.round(
                            (1 - Double.parseDouble(tableModel_source.getValueAt(key,
                            tableModel_source.findColumn(
                                    "Кол-во отклоненных заявок")).toString()) / Double.parseDouble(
                                            tableModel_source.getValueAt(key,
                                                    tableModel_source.findColumn(
                                                            "Кол-во сгенерированных заявок")).toString())) * 100) + "%",
                    key, tableModel_source.findColumn("Эффективность")
            );
        }
    }

    private static void createSource() {
        Source source = new Source(list_source.size(), pdt_mng, gen, tableModel_source, mode);
        list_source.put(source.getId(), source);
        String[] data = {
                String.valueOf(source.getId()),
                "null",
                String.valueOf(source.getCount_application()),
                String.valueOf(0),
                ""
        };
        tableModel_source.addRow(data);
        new Thread(source::run).start();
    }

    private static void createDevice() {
        Device device = new Device(list_device.size(), slt_mng, gen, tableModel_device, mode);
        list_device.put(device.getId(), device);
        slt_mng.addDevice(device);
        String[] data = {
                String.valueOf(device.getId()),
                "null",
                String.valueOf(device.getCount_application())
        };
        tableModel_device.addRow(data);
    }

    private static JButton createButton() {
        JFrame frame = new JFrame();
        frame.setBounds(screen.x * 3 / 8, screen.y * 7 / 16, screen.x / 4, screen.y / 8);
        JButton button = new JButton("NEXT");
        button.setBounds(screen.x * 3 / 8, screen.y * 7 / 16, screen.x / 4, screen.y  / 8);
        button.addActionListener(e -> {
            button.setEnabled(false);
            gen.invertStop();
        });
        frame.add(button);
        frame.setUndecorated(false);
        frame.setVisible(true);
        return button;
    }

    public static int[] countAppStep() {
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<Integer> list_f = new ArrayList<>();
        for (Integer key : list_source.keySet()) {
            Source source = list_source.get(key);
            int[] count = source.getCount();
            list.add(count[0]);
            list_f.add(count[1]);
        }
        int[] sum = {0, 0, 0};
        sum[0] = list.stream().reduce(sum[0], Integer::sum);
        sum[1] = list_f.stream().reduce(sum[1], Integer::sum);
        sum[2] = buff.countApp();
        return sum;
    }
}

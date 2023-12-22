package org.example.Tools;

import org.example.Server;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.LinkedList;

public class Buffer {
    private final Integer size;
    private final LinkedList<Application> list_application = new LinkedList<>();
    private final ArrayList<Application> package_application = new ArrayList<>();
    private final Object mutex = new Object();
    private static DefaultTableModel tableModel;

    public Buffer(Integer size, DefaultTableModel tM) {
        this.size = size;
        tableModel = tM;
    }

    public Integer statusBuff() {
        synchronized (mutex) {
            int count_application = this.list_application.size();
            if (count_application == 0) {
                return 0;
            } else if (count_application == this.size) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    public void addApplication(Application application) {
        synchronized (mutex) {
            this.list_application.add(application);
            String[] str = {application.toString()};
            tableModel.addRow(str);
        }
    }

    public ArrayList<Application> assemblyPackage() {
        synchronized (mutex) {
            if (this.package_application.size() != 0) {
                this.package_application.clear();
            }
            int current_id = (this.list_application.get(0)).getIdSource();
            for (int i = 0; i < list_application.size(); i++) {
                Application app = list_application.get(i);
                if (app.getIdSource() == current_id) {
                    this.package_application.add(app);
                    this.list_application.remove(app);
                    tableModel.removeRow(i);
                    Long end_time = System.currentTimeMillis();
                    Server.addTime_app_o(app.getIdSource(), end_time - app.getCreate_time());
                }
            }
            return this.package_application;
        }
    }

    public int countApp() {
        synchronized (mutex) {
            return list_application.size();
        }
    }
}

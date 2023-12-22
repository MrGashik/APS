package org.example.Tools;

import org.example.Server;

import javax.swing.table.DefaultTableModel;

public class ProductionManager {
    private static Buffer buffer;
    private static final Object mutex = new Object();

    public ProductionManager(Buffer buff) {
        buffer = buff;
    }

    public Boolean run(Application app, DefaultTableModel tM) {
        synchronized (mutex) {
            Integer stat_buff = buffer.statusBuff();
            if (stat_buff != 2) {
                buffer.addApplication(app);
                return true;
            } else {
                failure(app, tM);
                return false;
            }
        }
    }

    private void failure(Application app, DefaultTableModel tM) {
        tM.setValueAt("Отказ", app.getIdSource(), tM.findColumn("Кол-во сгенерированных заявок"));
        Long end_time = System.currentTimeMillis();
        Server.addTime_app(app.getIdSource(), end_time - app.getCreate_time());
    }
}

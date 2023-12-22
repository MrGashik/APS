package org.example.Tools;

import org.example.Server;

import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Device {
    private static SelectionManager slt_mng;
    private final Integer id_device;
    private final Integer time;
    private static Generator gen;
    private final Object mutex = new Object();
    private static DefaultTableModel tableModel;
    private final AtomicInteger count_application = new AtomicInteger();
    private final boolean mode;
    private Long work_time = 0L;
    private Boolean end = false;

    public void run(Application application) {
        Long start_time = System.currentTimeMillis();
        workApp(application);
        Long end_time = System.currentTimeMillis();
        slt_mng.markEndWork(this.id_device, Thread.currentThread());
        Server.addTime_app(application.getIdSource(), end_time - application.getCreate_time());
        work_time += end_time - start_time;
        tableModel.setValueAt("null", this.id_device, tableModel.findColumn("Заявки"));
    }

    public Device(Integer id, SelectionManager sm, Generator g, DefaultTableModel tM, Boolean m) {
        this.id_device = id;
        if (slt_mng == null) {
            slt_mng = sm;
        }
        if (tableModel == null) {
            tableModel = tM;
        }
        gen = g;
        this.time = gen.generetedTimeDevice();
        this.count_application.set(0);
        mode = m;
    }

    public Integer getId() {
        return this.id_device;
    }

    public Integer getCount_application() {
        return this.count_application.get();
    }

    private void workApp(Application app) {
        synchronized (mutex) {
            try {
                int iter = 0;
                tableModel.setValueAt(app.toString(), this.id_device, tableModel.findColumn("Заявки"));
                tableModel.setValueAt(String.valueOf(count_application.incrementAndGet()), this.id_device, tableModel.findColumn("Кол-во обработанных заявок"));
                if (mode) {
                    while (iter < time) {
                        if (gen.getStop()) {
                            while (gen.getStop()) {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            iter++;
                        }
                    }
                } else {
                    Thread.sleep(time * 1000);
                }
                tableModel.setValueAt("", this.id_device, tableModel.findColumn("Заявки"));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Long getWork_time() {
        return work_time;
    }

    public boolean getEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public Object getMutex() {
        return mutex;
    }
}

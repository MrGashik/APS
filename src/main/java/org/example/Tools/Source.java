package org.example.Tools;

import javax.swing.table.DefaultTableModel;
import java.util.concurrent.atomic.AtomicInteger;

public class Source {
    private static ProductionManager pdt_mng;
    private final Integer id_source;
    private final AtomicInteger count_application = new AtomicInteger();
    private final AtomicInteger count_failure = new AtomicInteger();
    private static Generator gen;
    private static DefaultTableModel tableModel;
    private Boolean end = false;
    private final boolean mode;

    public Source(Integer id, ProductionManager pm, Generator g, DefaultTableModel tM, boolean mode) {
        this.id_source = id;
        if (pdt_mng == null) {
            pdt_mng = pm;
        }
        if (gen == null) {
            gen = g;
        }
        if (tableModel == null) {
            tableModel = tM;
        }
        this.count_application.set(0);
        this.count_failure.set(0);
        this.mode = mode;
    }

    public void run() {
        while (true) {
            double rand_p = Math.random();
            Double gen_p = gen.generetedProbability();
            try {
                if (gen.getStep() == gen.getCount_Source()) {
                    break;
                } else if (rand_p <= gen_p) {
                    Application app = generateApplication("Source #" + this.id_source + " work!");
                    tableModel.setValueAt(app, this.id_source, tableModel.findColumn("Заявка"));
                    if (!pdt_mng.run(app, tableModel)) {
                        tableModel.setValueAt(String.valueOf(count_failure.incrementAndGet()), this.id_source,
                                tableModel.findColumn("Кол-во отклоненных заявок"));
                    }
                    Thread.sleep(1000);
                    tableModel.setValueAt(String.valueOf(count_application.incrementAndGet()), this.id_source,
                            tableModel.findColumn("Кол-во сгенерированных заявок"));
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (mode) {
                while (gen.getStop()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        this.end = true;
    }

    public Integer getId() {
        return this.id_source;
    }

    public Integer getCount_application() {
        return count_application.get();
    }

    public Integer getCount_failure() {
        return count_failure.get();
    }

    public int[] getCount() {
        return new int[]{this.count_application.get(), this.count_failure.get()};
    }

    public Boolean getEnd() {
        return end;
    }

    private Application generateApplication(String content) {
        return new Application(this.id_source, this.count_application.get(), content);
    }
}

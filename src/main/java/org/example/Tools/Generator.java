package org.example.Tools;

import org.example.Server;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.*;

public class Generator {
    private final Integer count_source;
    private final Double lambda;
    private final Integer time_device;
    private final AtomicInteger step = new AtomicInteger(0);
    private final AtomicInteger count = new AtomicInteger(0);
    private Double probability;
    private final XYSeries series;
    private final XYSeries series_f;
    private final XYSeries series_b;
    private final int[] count_prev_step = {0, 0};
    private static boolean mode;
    private static JButton jButton;
    private static final AtomicBoolean stop = new AtomicBoolean(true);

    public Generator(Double proba, Integer count, Integer time_d, XYSeries s, XYSeries s_f, XYSeries s_b, boolean m, JButton jB) {
        this.count_source = count;
        this.lambda = this.count_source * proba;
        this.time_device = time_d;
        this.probability = 1 / exp(lambda);
        this.series = s;
        this.series_f = s_f;
        this.series_b = s_b;
        mode = m;
        jButton = jB;
    }

    public Double generetedProbability() {
        if (count.incrementAndGet() == count_source) {
            int[] count_cur_step = Server.countAppStep();
            series.add(step.get(), count_cur_step[0] - this.count_prev_step[0]);
            this.count_prev_step[0] = count_cur_step[0];
            series_f.add(step.get(), count_cur_step[1] - this.count_prev_step[1]);
            this.count_prev_step[1] = count_cur_step[1];
            series_b.add(step.get(), count_cur_step[2]);
            if (mode) {
                jButton.setEnabled(true);
                invertStop();
                while (stop.get()) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            step.incrementAndGet();
            this.probability *= lambda / step.get();
            count.set(0);
        }
        return this.probability;
    }

    public int generetedTimeDevice() {
        return time_device;
    }

    public int getStep() {
        return step.get();
    }

    public int getCount_Source() {
        return count_source;
    }

    public Boolean getStop() {
        return stop.get();
    }

    public void invertStop() {
        stop.set(!stop.get());
    }
}

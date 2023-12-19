package org.example.Tools;

import org.example.Server;

import java.awt.desktop.AppForegroundListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectionManager {
    private static Buffer buffer;
    private static Generator gen;
    private static HashMap< Integer, AtomicBoolean > list_status_device;
    private static HashMap< Integer, Device> list_device;
    private static HashMap< Integer, ArrayList <Application> > packages_device;
    private final Object mutex = new Object();
    private final boolean mode;

    public void run() {
        try {
            while (true) {
                if (buffer.statusBuff() != 0) {
                    AtomicInteger index_free_device = new AtomicInteger(checkStatusDevice());
                    if (index_free_device.get() != -1) {
                        synchronized (mutex) {
                            Thread.sleep(10);
                            list_status_device.get(index_free_device.get()).set(true);
                            ArrayList< Application > temp_app = buffer.assemblyPackage();
                            for (Application app: temp_app) {
                                packages_device.get(index_free_device.get()).add(app);
                            }
                            ArrayList< Application > pack = new ArrayList<>(packages_device.get(index_free_device.get()));
                            new Thread(() -> list_device.get(index_free_device.get()).run(pack.get(0))).start();
                            packages_device.get(index_free_device.get()).remove(packages_device.get(index_free_device.get()).get(0));
                        }
                    }
                } else {
                    Thread.sleep(10);
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public SelectionManager(Buffer buff, Generator g, boolean mode) {
        buffer = buff;
        gen = g;
        list_status_device = new HashMap<>();
        list_device = new HashMap<>();
        packages_device = new HashMap<>();
        this.mode = mode;
    }

    public void addDevice(Device device) {
        list_device.put(device.getId(), device);
        list_status_device.put(device.getId(), new AtomicBoolean(false));
        packages_device.put(device.getId(), new ArrayList<>());
    }

    public Integer checkStatusDevice() {
        synchronized (mutex) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < list_status_device.size(); i++) {
                if (!list_status_device.get(i).get()) {
                    return i;
                }
            }
            return -1;
        }
    }

    public void markEndWork(Integer id_device, Thread thread) {
        synchronized (mutex) {
            try {
                Thread.sleep(10);
                ArrayList< Application > pack = new ArrayList<>(packages_device.get(id_device));
                if (pack.size() == 0) {
                    list_status_device.get(id_device).set(false);
                    thread.interrupt();
                } else {
                    thread = new Thread(() -> list_device.get(id_device).run(pack.get(0)));
                    packages_device.get(id_device).remove(packages_device.get(id_device).get(0));
                    thread.start();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

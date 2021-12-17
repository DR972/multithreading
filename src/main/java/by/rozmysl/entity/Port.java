package by.rozmysl.entity;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.IntStream;

public class Port {
    private static final Logger logger = LogManager.getLogger(Port.class);
    private int capacity;
    private int workload;
    private final Deque<Berth> availableBerths = new ArrayDeque<>();
    private static final Properties PROPERTIES = new Properties();

    private Port() {
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream("port/port.properties")) {
            if (inputStream != null) PROPERTIES.load(inputStream);
            capacity = Integer.parseInt(PROPERTIES.getProperty("port.capacity"));
            workload = Integer.parseInt(PROPERTIES.getProperty("port.workload"));
            IntStream.rangeClosed(1, Integer.parseInt(PROPERTIES.getProperty("numberBerths")))
                    .mapToObj(b -> new Berth(PROPERTIES.getProperty("b" + b + ".name"), Integer.parseInt(PROPERTIES.getProperty("b" + b + ".time"))))
                    .forEachOrdered(availableBerths::add);
            logger.log(Level.INFO, "Port initialized successfully.");
        } catch (IOException | NumberFormatException e) {
            logger.log(Level.ERROR, "File has not found: {}", "port/port.properties");
        }
    }

    private static class LazyHolder {
        static final Port INSTANCE = new Port();
    }

    public static Port getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int getWorkload() {
        return workload;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }

    public Deque<Berth> getAvailableBerths() {
        return availableBerths;
    }

    public Berth getFreeBerth() {
        Berth berth = null;
        if (!availableBerths.isEmpty()) {
            berth = availableBerths.removeFirst();
        }
        return berth;
    }

    public void returnBerth(Berth berth) {
        availableBerths.addLast(berth);
    }
}

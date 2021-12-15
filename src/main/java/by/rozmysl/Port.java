package by.rozmysl;

import by.rozmysl.entity.Berth;
import by.rozmysl.entity.Ship;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class Port extends Thread {
    private static final Logger logger = LogManager.getLogger(Port.class);
    private int capacity;
    private int workload;

    private final Lock locker = new ReentrantLock(true);
    private final Deque<Berth> availableBerths = new ArrayDeque<>();
    private final List<Condition> queueCondition = new ArrayList<>();

    private static List<Ship> queueShips = new ArrayList<>();
    private static final Properties properties = new Properties();

    private Port() {
        try {
            properties.load(new FileInputStream("src/main/resources/port/port.properties"));
        } catch (IOException e) {
            logger.log(Level.ERROR, "File has not found: {}", "src/main/resources/port/port.properties");
        }
        initializePort();
    }

    private static class LazyHolder {
        static final Port INSTANCE = new Port();
    }

    public static Port getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void initializePort() {
        capacity = Integer.parseInt(properties.getProperty("port.capacity"));
        workload = Integer.parseInt(properties.getProperty("port.workload"));
        IntStream.rangeClosed(1, Integer.parseInt(properties.getProperty("numberBerths")))
                .mapToObj(b -> new Berth(properties.getProperty("b" + b + ".name"),
                        Integer.parseInt(properties.getProperty("b" + b + ".time")))).forEachOrdered(availableBerths::add);
        logger.log(Level.INFO, "Port initialized successfully.");
    }

    public int getWorkload() {
        return workload;
    }

    public int getCapacity() {
        return capacity;
    }

    public static void add(List<Ship> ships) {
        queueShips = ships;
    }

    public Berth getFreeBerth(Ship ship) {
        Berth berth = null;
        try {
            locker.lock();
            TimeUnit.MILLISECONDS.sleep(200);
            Condition condition = locker.newCondition();

            while (true) {
                if (availableBerths.isEmpty() || ((ship.isPurposeUnloading() && (capacity - workload) < ship.getShipWorkload())
                        || (!ship.isPurposeUnloading() && workload < ship.getShipWorkload()))) {
                    queueCondition.add(condition);
                    condition.await();
                } else break;
            }

            workload += ship.isPurposeUnloading() ? ship.getShipWorkload() : -ship.getShipWorkload();
            queueShips.remove(ship);
            berth = availableBerths.removeFirst();
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Caught an exception: ", e);
        } finally {
            locker.unlock();
        }
        return berth;
    }

    public void returnBerth(Berth berth) {
        try {
            locker.lock();
            availableBerths.addLast(berth);
            for (int i = 0; i < queueShips.size(); i++) {
                if ((queueShips.get(i).isPurposeUnloading() && (capacity - workload) >= queueShips.get(i).getShipWorkload())
                        || (!queueShips.get(i).isPurposeUnloading() && workload >= queueShips.get(i).getShipWorkload())) {
                    Condition condition = queueCondition.get(i);
                    queueCondition.remove(i);
                    queueShips.remove(i);
                    condition.signal();
                    break;
                } else {
                    logger.log(Level.INFO, "{}'s ship's cargo ({} containers) won't fit.", queueShips.get(i).getShipId(), queueShips.get(i).getShipWorkload());
                }
            }
        } finally {
            locker.unlock();
        }
    }
}

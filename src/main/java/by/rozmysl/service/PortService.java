package by.rozmysl.service;

import by.rozmysl.entity.Port;
import by.rozmysl.entity.Berth;
import by.rozmysl.entity.Ship;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class PortService {
    private static final Logger logger = LogManager.getLogger(PortService.class);
    private final Port port;
    private boolean possibilityServicingShip;

    private final Lock locker = new ReentrantLock(true);
    private final List<Condition> queueCondition = new ArrayList<>();

    private static List<Ship> queueShips = new ArrayList<>();
    private static PortService service;

    public static PortService getInstance() {
        if (service == null) service = new PortService();
        return service;
    }

    private PortService() {
        port = Port.getInstance();
        possibilityServicingShip = true;
    }

    public Port getPort() {
        return port;
    }

    public static void add(List<Ship> ships) {
        queueShips = ships;
    }

    public void startWorking(Ship ship) {
        Berth berth = null;
        try {
            locker.lock();
            TimeUnit.MILLISECONDS.sleep(200);
            Condition condition = locker.newCondition();

            while (!queueShips.isEmpty()) {
                if (port.getAvailableBerths().isEmpty() || !checkPossibilityServicingShip(port, ship)) {
                    queueCondition.add(condition);
                    condition.await();
                } else break;
            }

            if (possibilityServicingShip) {
                port.setWorkload(ship.isPurposeUnloading() ? port.getWorkload() + ship.getShipWorkload() : port.getWorkload() - ship.getShipWorkload());
                queueShips.remove(ship);
                berth = port.getFreeBerth();
            }

        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Caught an exception: ", e);
        } finally {
            locker.unlock();
        }
        if (possibilityServicingShip && berth != null) {
            serviceShip(ship, berth);
            releaseBerth(berth);
            findNextShip();
        }
    }

    private boolean checkPossibilityServicingShip(Port port, Ship ship) {
        return (ship.isPurposeUnloading() && (port.getCapacity() - port.getWorkload()) >= ship.getShipWorkload())
                || (!ship.isPurposeUnloading() && port.getWorkload() >= ship.getShipWorkload());
    }

    private void serviceShip(Ship ship, Berth berth) {
        ship.setShipState(Ship.State.PROCESSING);
        logger.log(Level.INFO, "The {} processed at the {} of the port.", ship.getShipId(), berth.getName());
        try {
            TimeUnit.MILLISECONDS.sleep((long) ship.getShipWorkload() * berth.getTimePerContainer());
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Caught an exception: ", e);
        }
        ship.setShipState(Ship.State.FINISHED);
        logger.log(Level.INFO, "{}`s has finished processing at the {} of the port. The warehouse has space for {} containers.",
                ship.getShipId(), berth.getName(), port.getCapacity() - port.getWorkload());
    }

    public void releaseBerth(Berth berth) {
        try {
            locker.lock();
            port.returnBerth(berth);
        } finally {
            locker.unlock();
        }
    }

    public void findNextShip() {
        try {
            locker.lock();
            for (int i = 0; i < queueShips.size(); i++) {
                if (checkPossibilityServicingShip(port, queueShips.get(i))) {
                    queueCondition.get(i).signal();
                    queueCondition.remove(i);
                    queueShips.remove(i);
                    break;
                } else {
                    logger.log(Level.INFO, "{}'s ship's cargo ({} containers) won't fit.", queueShips.get(i).getShipId(), queueShips.get(i).getShipWorkload());
                    if (i == queueShips.size() - 1) {
                        IntStream.range(0, queueShips.size()).forEachOrdered(j -> {
                            logger.log(Level.INFO, "{}'s cannot be serviced.", queueShips.get(j).getShipId());
                            queueCondition.get(j).signal();
                        });
                        queueShips.clear();
                        queueCondition.clear();
                        possibilityServicingShip = false;
                    }
                }
            }
        } finally {
            locker.unlock();
        }
    }
}

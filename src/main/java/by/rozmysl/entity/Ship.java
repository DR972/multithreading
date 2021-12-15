package by.rozmysl.entity;

import by.rozmysl.Port;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class Ship extends Thread {
    private static final Logger logger = LogManager.getLogger(Ship.class);
    private final String shipId;
    private final boolean purposeUnloading;
    private final int shipWorkload;
    private State shipState;

    public Ship(String shipId, boolean purposeUnloading, int shipWorkload) {
        this.shipId = shipId;
        this.purposeUnloading = purposeUnloading;
        this.shipWorkload = shipWorkload;
        this.shipState = State.NEW;
    }

    public String getShipId() {
        return shipId;
    }


    public boolean isPurposeUnloading() {
        return purposeUnloading;
    }


    public int getShipWorkload() {
        return shipWorkload;
    }

    public enum State {
        NEW, PROCESSING, FINISHED
    }

    public void run() {
        Port port = Port.getInstance();
        Berth berth = port.getFreeBerth(this);
        shipState = Ship.State.PROCESSING;
        logger.log(Level.INFO, "The {} processed at the {} of the port.", shipId, berth.getName());
        try {
            TimeUnit.MILLISECONDS.sleep((long) shipWorkload * berth.getTimePerContainer());
        } catch (InterruptedException e) {
            logger.log(Level.ERROR, "Caught an exception: ", e);
        }
        shipState = Ship.State.FINISHED;
        logger.log(Level.INFO, "{}`s has finished processing at the {} of the port. The warehouse has space for {} containers.",
                shipId, berth.getName(), port.getCapacity() - port.getWorkload());
        port.returnBerth(berth);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Ship.class.getSimpleName() + "[", "]")
                .add("shipId='" + shipId + "'")
                .add("purposeUnloading=" + purposeUnloading)
                .add("shipload=" + shipWorkload)
                .toString();
    }
}

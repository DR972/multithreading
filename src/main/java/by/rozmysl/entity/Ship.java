package by.rozmysl.entity;

import by.rozmysl.service.PortService;

import java.util.StringJoiner;

public class Ship extends Thread {
    public enum State {
        NEW, PROCESSING, FINISHED
    }

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

    public void setShipState(State shipState) {
        this.shipState = shipState;
    }

    public void run() {
        PortService service = PortService.getInstance();
        if (service.getPort().getCapacity() != 0) service.startWorking(this);
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

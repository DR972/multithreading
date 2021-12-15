package by.rozmysl.entity;

public class Berth {
    private final String name;
    private final int timePerContainer;

    public Berth(String name, int timePerContainer) {
        this.name = name;
        this.timePerContainer = timePerContainer;
    }

    public String getName() {
        return name;
    }

    public int getTimePerContainer() {
        return timePerContainer;
    }
}

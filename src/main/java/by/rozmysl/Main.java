package by.rozmysl;

import by.rozmysl.entity.Ship;
import by.rozmysl.parser.WordParser;
import by.rozmysl.reader.TextReader;
import by.rozmysl.service.PortService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        String[] lines = new TextReader().read("port/queue.txt");
        List<Ship> queueShips = new WordParser().parse(lines);
        PortService.add(queueShips);

        ExecutorService executor = Executors.newCachedThreadPool();
        for (Ship queueShip : queueShips) {
            executor.execute(queueShip);
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                logger.log(Level.ERROR, "Caught an exception: ", e);
            }
        }
        executor.shutdown();
    }
}

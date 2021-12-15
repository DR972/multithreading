package by.rozmysl;

import by.rozmysl.entity.Ship;
import by.rozmysl.parser.WordParser;
import by.rozmysl.reader.TextReader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        String[] lines = new TextReader().read("src/main/resources/port/queue.txt");
        List<Ship> queueShips = new WordParser().parse(lines);

        Port.getInstance();
        Port.add(queueShips);

        ExecutorService executor = Executors.newCachedThreadPool();
        queueShips.forEach(executor::execute);
        executor.shutdown();
    }
}

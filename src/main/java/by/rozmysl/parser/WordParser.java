package by.rozmysl.parser;

import by.rozmysl.entity.Ship;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WordParser {

    public List<Ship> parse(String[] lines) {
        return Arrays.stream(lines).map(l -> {
            String[] ship = l.replace(" ", "").split(",");
            return new Ship(ship[0], Boolean.parseBoolean(ship[1]), Integer.parseInt(ship[2]));
        }).collect(Collectors.toList());
    }
}

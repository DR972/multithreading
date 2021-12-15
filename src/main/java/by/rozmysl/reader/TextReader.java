package by.rozmysl.reader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextReader {
    private static final Logger logger = LogManager.getLogger(TextReader.class);

    public String[] read(String filename) {
        String[] str = new String[0];
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filename))) {
            str = br.lines().toArray(String[]::new);
        } catch (IOException e) {
            logger.log(Level.ERROR, "File has not found: {}", filename);
        }
        logger.log(Level.INFO, "Read file {} is successfully", filename);
        return str;
    }
}

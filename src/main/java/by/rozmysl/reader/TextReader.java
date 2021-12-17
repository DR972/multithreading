package by.rozmysl.reader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Objects;

public class TextReader {
    private static final Logger logger = LogManager.getLogger(TextReader.class);

    public String[] read(String filename) {
        String[] str = new String[0];
        try (BufferedReader br = new BufferedReader(
                new FileReader(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(filename)).getFile()))) {
            str = br.lines().toArray(String[]::new);
        } catch (IOException | NullPointerException e) {
            logger.log(Level.ERROR, "File has not found: {}", filename);
        }
        logger.log(Level.INFO, "Read file {} is successfully", filename);
        return str;
    }
}

package de.dasbrand.viewspotfindercc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.function.Function;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ViewSpotFinderCcApplication {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected call format is \"java -jar ViewSpotFinderCC.jar <mesh file> <number of view spots>\"");
            return;
        }

        Integer limit = null;
        try {
            limit = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Cannot parse limit " + args[1]);
            return;
        }

        Path inputFilePath = Paths.get(args[0]);
        ViewSpotFinder testFinder = null;
        try {
            testFinder = new ViewSpotFinder(inputFilePath.toFile());
        } catch (IOException e) {
            System.out.println("Error during mesh file parse: " + e);
            return;
        }
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<ElementWithValue> viewsSpots = testFinder.findViewSpots(limit, elemsWithValue);
        System.out.println(testFinder.formatViewSpots(viewsSpots));
    }

    @Bean
    public Function<String, String> findViewSpots() {
        return json -> {
            ViewSpotFinder testFinder = null;
            try {
                testFinder = new ViewSpotFinder(json);
            } catch (Exception e) {
                return "Error during json parse: " + e;
            }
            TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
            ArrayList<ElementWithValue> viewsSpots = testFinder.findViewSpots(0, elemsWithValue);
            return testFinder.formatViewSpots(viewsSpots);
        };
    }
}

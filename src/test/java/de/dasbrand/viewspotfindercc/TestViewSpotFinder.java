package de.dasbrand.viewspotfindercc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import de.dasbrand.viewspotfindercc.json.Input;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import static org.assertj.core.api.Assertions.assertThat;

public class TestViewSpotFinder {

    @Test
    public void testJsonParse() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_simple.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        Input parsedInput = testFinder.getInput();
        Assertions.assertThat(parsedInput.getNodes()).hasSize(121);
        assertThat(parsedInput.getElements()).hasSize(200);
        assertThat(parsedInput.getValues()).hasSize(200);
    }

    @Test
    public void testViewSpotFinderSimple() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_simple.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<Integer> viewsSpots = testFinder.findViewSpots(5000, elemsWithValue);

        assertCorrectness(elemsWithValue, viewsSpots);
    }

    @Test
    public void testViewSpotFinder10k() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_10000.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<Integer> viewsSpots = testFinder.findViewSpots(10000, elemsWithValue);

        assertCorrectness(elemsWithValue, viewsSpots);
    }

    @Test
    public void testViewSpotFinder20k() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_20000.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<Integer> viewsSpots = testFinder.findViewSpots(20000, elemsWithValue);

        assertCorrectness(elemsWithValue, viewsSpots);
    }

    private void assertCorrectness(TreeSet<ElementWithValue> elemsWithValue, ArrayList<Integer> viewsSpots) {
        // assert correctness using naive full iterations ignoring performance
        for (ElementWithValue elem : elemsWithValue) {
            double elemValue = elem.getValue();
            if (viewsSpots.contains(elem.getId())) {
                // if an element is a viewspot, it may not share a node with an element that has a higher value
                assertThat(elemsWithValue).allSatisfy(otherElem -> {
                    for (Integer nodeId : elem.getNodes()) {
                        if (otherElem.getNodes().contains(nodeId)) {
                            assertThat(elemValue).isGreaterThanOrEqualTo(otherElem.getValue());
                        }
                    }
                });
            } else {
                // if an element is not a viewspot, it must share a node with at least one element with higher value
                boolean higherValueFound = false;
                for (ElementWithValue otherElem : elemsWithValue) {
                    if (otherElem.getId() != elem.getId()) {
                        for (Integer nodeId : elem.getNodes()) {
                            if (otherElem.getNodes().contains(nodeId)) {
                                if (otherElem.getValue() > elemValue) {
                                    higherValueFound = true;
                                }
                            }
                        }
                    }
                }
                assertThat(higherValueFound).isTrue();
            }
        }
    }

    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .threads(1)
                .measurementIterations(3)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkViewSpotFinderSimple() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_simple.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<Integer> viewsSpots = testFinder.findViewSpots(5000, elemsWithValue);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkViewSpotFinder10k() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_10000.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<Integer> viewsSpots = testFinder.findViewSpots(10000, elemsWithValue);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkViewSpotFinder20k() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_20000.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        TreeSet<ElementWithValue> elemsWithValue = testFinder.createElemsWithValueOrderedSet();
        ArrayList<Integer> viewsSpots = testFinder.findViewSpots(20000, elemsWithValue);
    }
}

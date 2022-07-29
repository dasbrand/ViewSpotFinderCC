package de.dasbrand.viewspotfindercc;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        testFinder.findViewSpots(5000);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkViewSpotFinder10k() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_10000.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        testFinder.findViewSpots(10000);
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkViewSpotFinder20k() throws IOException {
        Path inputFilePath = Paths.get("src", "test", "resources", "mesh_20000.json");
        ViewSpotFinder testFinder = new ViewSpotFinder(inputFilePath.toFile());
        testFinder.findViewSpots(20000);
    }
}

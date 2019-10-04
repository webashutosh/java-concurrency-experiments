package in.acode;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class JmhBenchmarks {

    @Param({"1000"})
    private int NUM_OF_ITEMS;

    private List<String> DATA_FOR_TESTING;

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(JmhBenchmarks.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        DATA_FOR_TESTING = createTestData();
    }

    @Benchmark
    public void forLoop(Blackhole bh) {
        for (int i = 0; i < DATA_FOR_TESTING.size(); i++) {
            String s = DATA_FOR_TESTING.get(i);
            bh.consume(s);
        }
    }

    @Benchmark
    public void whileLoop(Blackhole bh) {
        int i = 0;
        while (i < DATA_FOR_TESTING.size()) {
            String s = DATA_FOR_TESTING.get(i);
            bh.consume(s);
            i++;
        }
    }

    private List<String> createTestData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < NUM_OF_ITEMS; i++) {
            data.add("Number : " + i);
        }
        return data;
    }

}

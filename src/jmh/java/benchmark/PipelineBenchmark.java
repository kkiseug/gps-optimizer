package benchmark;

import core.*;
import core.common.Algorithm;
import core.common.CleaningResult;
import core.common.Coordinate;
import core.common.GpsTrack;
import core.outlier.Threshold;
import core.simplifier.Tolerance;
import org.openjdk.jmh.annotations.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PipelineBenchmark {

    private GpsTrack track;

    @Setup
    public void setup() {
        List<Coordinate> coords = new ArrayList<>();
        Instant start = Instant.now();
        for (int i = 0; i < 100000; i++) {
            coords.add(new Coordinate(127.0 + i * 0.0001, 37.0 + i * 0.0001, start.plusSeconds(i)));
        }
        track = new GpsTrack(coords);
    }

    @Benchmark
    public CleaningResult fullPipeline() {
        return TrackCleaner.of(track)
                .removeOutliers(Threshold.ofKmPerHour(30))
                .removeOutliers(Threshold.ofMeters(100))
                .smooth(Algorithm.KALMAN)
                .simplify(Tolerance.ofMeters(1))
                .clean();
    }
}

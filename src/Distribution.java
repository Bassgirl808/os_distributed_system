package osdistributedsystem;

import java.util.Random;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Distribution<X> {
    List<X> items = new ArrayList<X>();
    List<Double> probabilities = new ArrayList<Double>();
    double probabilityTotal;
    Random random = new Random();

    Distribution(List<Pair<X, Double>> sampleSet) {
        for (Pair<X, Double> item : sampleSet) {
            this.probabilityTotal += item.y;

            this.items.add(item.x);
            this.probabilities.add(item.y);
        }
    }

    public X sample() {
        X value;
        double probability = this.random.nextDouble() * this.probabilityTotal;
        int i;

        for (i = 0; probability > 0; i++) {
            probability -= this.probabilities.get(i);
        }

        return items.get(i - 1);
    }
}
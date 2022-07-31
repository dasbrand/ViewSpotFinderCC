package de.dasbrand.viewspotfindercc;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ElementWithValue implements Comparable<ElementWithValue> {

    private double value;
    private int id;
    private List<Integer> nodes;

    @Override
    public int compareTo(ElementWithValue o) {
        double subtracted = this.value - o.value;
        if (subtracted >= 0) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "{element_id: " + id + ", value: " + value + "}";
    }
}

package de.dasbrand.viewspotfindercc.json;

import java.util.List;

import lombok.Getter;

@Getter
public class Input {
    private List<Node> nodes;
    private List<Element> elements;
    private List<Value> values;
}

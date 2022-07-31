package de.dasbrand.viewspotfindercc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dasbrand.viewspotfindercc.json.Element;
import de.dasbrand.viewspotfindercc.json.Input;
import de.dasbrand.viewspotfindercc.json.Value;
import lombok.Getter;

public class ViewSpotFinder {

    @Getter
    private final Input input;

    public ViewSpotFinder(File jsonFile) throws IOException {
        this.input = parseJson(jsonFile);
    }

    public ViewSpotFinder(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        this.input = mapper.readValue(json, Input.class);
    }

    public Input parseJson(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonFile, Input.class);
    }

    public ArrayList<ElementWithValue> findViewSpots(int limit, TreeSet<ElementWithValue> elemsWithValue) {
        ArrayList<ElementWithValue> viewSpots = new ArrayList<>(limit);
        HashSet<Integer> visitedNodeIds = new HashSet<>();

        Iterator<ElementWithValue> descIterator = elemsWithValue.descendingIterator();
        while (descIterator.hasNext()) {
            ElementWithValue elem = descIterator.next();
            boolean isViewSpot = true;
            // iterate through the nodes of the current element and mark them as visited
            for (Integer nodeId : elem.getNodes()) {
                // An element is a view spot if none of its nodes have already been marked (since having a marked node would
                // imply a neighbor element with greater or equal value).
                // Even if an element is not a view spot, it marks all of its nodes as visited (=> disqualified for a view spot),
                // because it still disqualifies neighbors from being a view spot solely by being a neighbor with a greater value.
                if (!visitedNodeIds.add(nodeId)) {
                    isViewSpot = false;
                }
            }
            if (isViewSpot) {
                viewSpots.add(elem);
            }
            if (limit != 0 && viewSpots.size() == limit) {
                return viewSpots;
            }
        }

        return viewSpots;
    }

    public TreeSet<ElementWithValue> createElemsWithValueOrderedSet() {
        Map<Integer, Double> elementIdToValueMap = createElementIdToValueMap();
        List<Element> elems = input.getElements();
        TreeSet<ElementWithValue> ret = new TreeSet<>();

        for (Element elem : elems) {
            int elemId = elem.getId();
            ret.add(new ElementWithValue(elementIdToValueMap.get(elemId), elemId, elem.getNodes()));
        }
        return ret;
    }

    private Map<Integer, Double> createElementIdToValueMap() {
        List<Value> values = input.getValues();
        Map<Integer, Double> ret = new HashMap<>(values.size());
        for (Value val : values) {
            ret.put(val.getElement_id(), val.getValue());
        }
        return ret;
    }

    public String formatViewSpots(ArrayList<ElementWithValue> viewsSpots) {
        StringBuilder sb = new StringBuilder("[\n");
        for (ElementWithValue vs : viewsSpots) {
            sb.append("\t").append(vs.toString());
            if (viewsSpots.indexOf(vs) < viewsSpots.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append("\n");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

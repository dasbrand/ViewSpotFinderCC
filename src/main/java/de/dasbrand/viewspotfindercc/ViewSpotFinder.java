package de.dasbrand.viewspotfindercc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dasbrand.viewspotfindercc.json.Element;
import de.dasbrand.viewspotfindercc.json.Input;
import de.dasbrand.viewspotfindercc.json.Value;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
        HashMap<Integer, Double> visitedNodeToValueMap = new HashMap<>();

        Iterator<ElementWithValue> descIterator = elemsWithValue.descendingIterator();
        while (descIterator.hasNext()) {
            ElementWithValue elem = descIterator.next();
            boolean isViewSpot = true;
            // Rationale:
            // An element is a view spot if none of its nodes have already been marked (since having a marked node would
            // imply a neighbor element with greater or equal value).
            // Even if an element is not a view spot, it marks all of its nodes as visited (=> disqualified for a view spot),
            // because it still disqualifies neighbors from being a view spot solely by being a neighbor with a greater value.
            double elemValue = elem.getValue();
            for (Integer nodeId : elem.getNodes()) {
                // For most cases, the rationale above could be realised with a set. However, for the edge case that:
                //  - there are multiple elements with the same value
                //  - one of them is a view spot
                //  - the elements of the same value share a node
                //  - and (most importantly) the view spot is not the first element (of the same value) we iterate through
                // we would miss the view spot. To avoid this error, we use a Map which not only notes the visited node ids
                // as keys but also notes the corresponding element values as values. If we do find a view spot, we
                // update all the node ids connected to the element in the map and add 1 (or another arbitrary number > 0)
                // to the entry's value. Accordingly, once we check whether a node id is already in the map and thus
                // might disqualify the current node from being a view spot, it only actually disqualifies it if the
                // value from the map is greater than the current element's value (since in that case it is either an
                // "actual" greater element value or the value of an element that has been given a bonus value of 1
                // to block all equal values from qualifying as view spot according to the requirement that there
                // may only be 1 view spot of the same value).
                if (visitedNodeToValueMap.containsKey(nodeId)) {
                    if (visitedNodeToValueMap.get(nodeId) > elemValue) {
                        isViewSpot = false;
                    }
                } else {
                    visitedNodeToValueMap.put(nodeId, elemValue);
                }
            }
            if (isViewSpot) {
                viewSpots.add(elem);
                for (Integer nodeId : elem.getNodes()) {
                    visitedNodeToValueMap.put(nodeId, elemValue + 1);
                }
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

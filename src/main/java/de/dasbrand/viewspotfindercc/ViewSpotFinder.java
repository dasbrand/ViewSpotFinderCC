package de.dasbrand.viewspotfindercc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    public Input parseJson(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonFile, Input.class);
    }

    public ArrayList<Integer> findViewSpots(int limit) {
        ArrayList<Integer> viewSpots = new ArrayList<>(limit);

        Map<Integer, Double> elementIdToValueMap = createElementIdToValueMap();

        TreeSet<ElementWithValue> elemsWithValue = createElemsWithValueOrderedSet(elementIdToValueMap);

        HashSet<ElementWithValue> visitedElements = new HashSet<>();

        Iterator<ElementWithValue> descIterator = elemsWithValue.descendingIterator();
        while (descIterator.hasNext()) {
            ElementWithValue elem = descIterator.next();
            // since the set we iterate through has been ordered by value, we only need to check if an element does not neighbor any of the already visited elements
            // for it to qualify as a view spot (because any unvisited element would have a smaller or equal value compared to this element)
            if (doesNotNeighborVisitedElement(elem, visitedElements)) {
                viewSpots.add(elem.getId());
            }

            visitedElements.add(elem);
            if (viewSpots.size() == limit) {
                return viewSpots;
            }
        }

        return viewSpots;
    }

    private boolean doesNotNeighborVisitedElement(ElementWithValue elem, Set<ElementWithValue> visitedElements) {
        for (ElementWithValue visitedElem : visitedElements) {
            if (isNeighbor(elem, visitedElem)) {
                return false;
            }
        }
        return true;
    }

    private boolean isNeighbor(ElementWithValue elem, ElementWithValue visitedElem) {
        List<Integer> visitedElemNodeIds = visitedElem.getNodes();
        for (Integer id : elem.getNodes()) {
            if (visitedElemNodeIds.contains(id)) {
                return true;
            }
        }
        return false;
    }

    private TreeSet<ElementWithValue> createElemsWithValueOrderedSet(Map<Integer, Double> elementIdToValueMap) {
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
}

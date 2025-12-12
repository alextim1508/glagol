package com.alextim.glagol.frontend.widget.graphs;


import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class SimpleGraph extends AbstractGraph {

    public DoubleDataSet scoresDataSet;

    public SimpleGraph(SimpleStringProperty title,
                       SimpleStringProperty progressTitle) {
        super(title, null, progressTitle, null, null, null);
        scoresDataSet = new DoubleDataSet(title.get());
    }

    public void addPoint(int index, long x, double y, String label) {
        scoresDataSet.add(x, y);
        scoresDataSet.addDataLabel(index, String.format("%s. %s", title.getValue(), label));
    }

    public void remove(int index) {
        scoresDataSet.remove(index);
    }

    public void clear() {
        scoresDataSet.clearData();
    }

    @Override
    public List<DoubleDataSet> getDataSetList() {
        List<DoubleDataSet> list = new ArrayList<>();
        list.add(scoresDataSet);
        return list;
    }

    @Override
    public void reset() {
    }

    @Override
    public int size() {
        return scoresDataSet.getDataCount();
    }
}

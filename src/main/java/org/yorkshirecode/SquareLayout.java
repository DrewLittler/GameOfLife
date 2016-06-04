package org.yorkshirecode;

import java.awt.*;

public class SquareLayout implements LayoutManager {

    private int rows = 1;
    private int cols = 1;
    private int gap = 0;

    @Override
    public void addLayoutComponent(String name, Component comp) {

    }

    @Override
    public void removeLayoutComponent(Component comp) {

    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(cols, rows);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(cols, rows);
    }

    @Override
    public void layoutContainer(Container parent) {
        int w = parent.getWidth();
        int h = parent.getHeight();

        int desiredWidthPerItem = w / cols;
        int desiredHeightPerItem = h / rows;

        int itemSize = Math.min(desiredHeightPerItem, desiredWidthPerItem);
        itemSize -= gap;

        int x = 0;
        int y = 0;

        for (int i=0; i<parent.getComponentCount(); i++) {
            Component comp = parent.getComponent(i);
            comp.setBounds(x, y, itemSize, itemSize);

            if ((i+1) % cols == 0) {
                y += gap;
                y += itemSize;
                x = 0;
            } else {
                x += gap;
                x += itemSize;
            }
        }
    }


    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }
}

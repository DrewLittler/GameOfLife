package org.yorkshirecode;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {

    private Grid grid = null;

    public GridPanel(Grid grid) {
        this.grid = grid;

        setOpaque(true);
        setBackground(Color.white);

        int w = grid.width();
        int h = grid.height();
        setPreferredSize(new Dimension(w*10, h*10));
    }

    @Override public void paintComponent(Graphics g) {

        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();

        int cols = grid.width();
        int rows = grid.height();

        int colWidth = w/cols;
        int rowHeight = h/rows;

        g.setColor(Color.gray);

        g.drawRect(0, 0, w, h);

        for (int row=1; row<rows; row++) {
            int y = row*rowHeight;
            g.drawLine(0, y, w, y);
        }
        for (int col = 1; col < cols; col++) {
            int x = col*colWidth;
            g.drawLine(x, 0, x, h);
        }

        g.setColor(Color.black);

        for (int row=0; row<rows; row++) {
            for (int col=0; col<cols; col++) {

                if (grid.isOccupied(col, row)) {
                    int x = col*colWidth;
                    int y = row*rowHeight;

                    g.fillRect(x, y, colWidth, rowHeight);
                }

            }
        }
    }

    public Grid getGrid() {
        return grid;
    }

    public String getSeedCode() {
        return grid.getSeedCode();
    }

    @Override public int hashCode() {
        return grid.hashCode();
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof GridPanel)) {
            return false;
        }
        GridPanel other = (GridPanel)o;
        return other.getGrid().equals(grid);
    }

}

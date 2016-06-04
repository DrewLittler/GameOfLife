package org.yorkshirecode;

import org.apache.commons.codec.binary.Hex;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.*;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import java.io.IOException;

public class Grid {

    private boolean[][] cells = null;
    private String seedCode = null;

    public Grid(int width, int height) {
        this(width, height, null);
    }

    public Grid(int width, int height, Grid source) {
        cells = new boolean[width][height];

        if (source != null) {

            for (int col=0; col<width; col++) {
                for (int row=0; row<height; row++) {
                    cells[col][row] = source.isOccupied(col, row);
                }
            }

        }
    }

    public Grid(String seedCode) throws Exception {

        byte[] bytes = Hex.decodeHex(seedCode.toCharArray());

        int cols = bytes[0];
        int rows = bytes[1];
        cells = new boolean[cols][rows];

        int col = 0;
        int row = 0;

        for (int i=0; i<cols*rows; i++) {

            int byteIndex = 2 + (i / 8);
            int bitIndex = i % 8;

            if ((bytes[byteIndex] >> bitIndex & 1) == 1) {
                occupy(col, row, true);
            }

            col ++;
            if (col == cols) {
                row ++;
                col = 0;
            }

        }
    }


    public int width() {
        return cells.length;
    }
    public int height() {
        return cells[0].length;
    }

    public Grid evolve() {
        Grid ret = new Grid(width(), height());

        for (int row=0; row<height(); row++) {
            for (int col=0; col<width(); col++) {

                int neighbours = countNeighbours(col, row);

                if (!isOccupied(col, row)) {

                    if (neighbours == 3) {
                        ret.occupy(col,row, true);
                    }

                } else {

                    if (neighbours == 2 || neighbours == 3) {
                        ret.occupy(col,row, true);
                    }
                }

            }
        }

        /**
         *
         Any live cell with fewer than two live neighbours dies, as if caused by under-population.
         Any live cell with two or three live neighbours lives on to the next generation.
         Any live cell with more than three live neighbours dies, as if by over-population.
         Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
         */

        return ret;
    }

    public void occupy(int col, int row, boolean occupy) {
        cells[col][row] = occupy;
        seedCode = null;
    }

    private int countNeighbours(int col, int row) {
        int ret = 0;

        for (int testCol=col-1; testCol<=col+1; testCol++) {
            for (int testRow=row-1; testRow<=row+1; testRow++) {

                if (testRow == row && testCol == col) {
                    continue;
                }

                if (isOccupied(testCol, testRow)) {
                    ret ++;
                }
            }
        }

        return ret;
    }
    public boolean isOccupied(int col, int row) {
        if (col < 0 || col >= width()) {
            return false;
        } else if (row < 0 || row >= height()) {
            return false;
        } else {
            return cells[col][row];
        }
    }

    public boolean isDead() {
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                if (isOccupied(col, row)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Grid)) {
            return false;
        }
        Grid other = (Grid)o;
        String otherSeed = other.getSeedCode();
        return getSeedCode().equals(otherSeed);
    }
    @Override public int hashCode() {
        return getSeedCode().hashCode();
    }

    public String getSeedCode() {

        if (seedCode == null) {

            int w = width();
            int h = height();

            int numBytes = 2 + ((w * h) / 8);
            if ((w * h) % 8 > 0) {
                numBytes++;
            }

            byte[] bytes = new byte[numBytes];

            bytes[0] = (byte)w;
            bytes[1] = (byte)h;

            int currentByte = 1;
            int currentBit = 0;

            for (int row = 0; row < h; row++) {
                for (int col = 0; col < w; col++) {
                    if (isOccupied(col, row)) {
                        bytes[currentByte] = (byte) (bytes[currentByte] | (1 << currentBit));
                    }

                    currentBit++;
                    if (currentBit == 8) {
                        currentBit = 0;
                        currentByte++;
                    }
                }
            }

            this.seedCode = Hex.encodeHexString(bytes);
        }
        return seedCode;
    }

    public void clear() {
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                occupy(col, row, false);
            }
        }
    }
    public void all() {
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                occupy(col, row, true);
            }
        }
    }
    public void invert() {
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                boolean b = isOccupied(col, row);
                occupy(col, row, !b);
            }
        }
    }

    public void nudge(Direction direction) {

        boolean[][] copyCells = new boolean[width()][height()];

        for (int col=0; col<width(); col++) {
            for (int row=0; row<height(); row++) {

                boolean b = false;

                switch (direction) {
                    case Up:
                        b = isOccupied(col, row+1);
                        break;
                    case Down:
                        b = isOccupied(col, row-1);
                        break;
                    case Left:
                        b = isOccupied(col+1, row);
                        break;
                    case Right:
                        b = isOccupied(col-1, row);
                        break;
                }

                copyCells[col][row] = b;
            }
        }

        this.cells = copyCells;
    }

    public void writeToFile(File f) throws IOException {

        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        int pxPerCell = 50;
        int w = width() * pxPerCell;
        int h = height() * pxPerCell;

        for (int row=0; row<=height(); row++) {
            int y = row*pxPerCell;
            svgGenerator.drawLine(0, y, w, y);
        }
        for (int col=0; col<=width(); col++) {
            int x = col*pxPerCell;
            svgGenerator.drawLine(x, 0, x, h);
        }

        for (int row=0; row<height(); row++) {
            for (int col=0; col<width(); col++) {

                if (isOccupied(col, row)) {
                    int x = col*pxPerCell;
                    int y = row*pxPerCell;

                    svgGenerator.fillRect(x, y, pxPerCell, pxPerCell);
                }

            }
        }

        // Ask the test to render into the SVG Graphics2D implementation.
      //  svgGenerator.drawLine(0, 0, 100, 100);
        /*TestSVGGen test = new TestSVGGen();
        test.paint(svgGenerator);*/

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes

        FileWriter out = new FileWriter(f);

        //Writer out = new OutputStreamWriter(System.out, "UTF-8");
        svgGenerator.stream(out, useCSS);

        out.close();
    }
}

package org.yorkshirecode;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Frame extends JFrame implements ChangeListener, ActionListener {
    private static final Logger LOG = LoggerFactory.getLogger(Frame.class);

    private JPanel seedGridPanel = null;
    private JSpinner spinnerWidth = null;
    private JSpinner spinnerHeight = null;
    private JPanel outputPanel = null;
    private JTextField seedCode = null;
    JFileChooser fileChooser = null;
    private Thread thread = null;

    private ArrayList<GridPanel> series = new ArrayList<>();
    private HashMap<String, GridPanel> seriesSet = new HashMap<>();
    private Grid grid = null;

    public Frame() {

        this.setTitle("Game of Life");
        this.setSize(new Dimension(1000, 1000));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(10);
        splitPane.setDividerLocation(0.5d);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        splitPane.add(left, JSplitPane.LEFT);

        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        splitPane.add(right, JSplitPane.RIGHT);

        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        topPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JLabel l = new JLabel();
        l.setText("Grid size");
        topPanel.add(l);

        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMinimum(1);
        model.setMaximum(127);
        spinnerWidth = new JSpinner();
        spinnerWidth.setModel(model);
        spinnerWidth.setValue(new Integer(10));
        spinnerWidth.addChangeListener(this);
        topPanel.add(spinnerWidth);

        l = new JLabel();
        l.setText("x");
        topPanel.add(l);

        model = new SpinnerNumberModel();
        model.setMinimum(1);
        model.setMaximum(127);
        spinnerHeight = new JSpinner();
        spinnerHeight.setModel(model);
        spinnerHeight.setValue(new Integer(10));
        spinnerHeight.addChangeListener(this);
        topPanel.add(spinnerHeight);

        JButton b = new JButton("All");
        b.addActionListener(this);
        topPanel.add(b);

        b = new JButton("Invert");
        b.addActionListener(this);
        topPanel.add(b);

        b = new JButton("Clear");
        b.addActionListener(this);
        topPanel.add(b);

        b = new JButton();
        b.setIcon(new ImageIcon(Resources.getResourceAsURLObject("Chevron Left-96.png")));
        b.setActionCommand("Left");
        b.setPreferredSize(new Dimension(30,30));
        b.addActionListener(this);
        topPanel.add(b);

        b = new JButton();
        b.setIcon(new ImageIcon(Resources.getResourceAsURLObject("Chevron Up-96.png")));
        b.setActionCommand("Up");
        b.setPreferredSize(new Dimension(30,30));
        b.addActionListener(this);
        topPanel.add(b);

        b = new JButton();
        b.setIcon(new ImageIcon(Resources.getResourceAsURLObject("Chevron Down-96.png")));
        b.setActionCommand("Down");
        b.setPreferredSize(new Dimension(30,30));
        b.addActionListener(this);
        topPanel.add(b);

        b = new JButton();
        b.setIcon(new ImageIcon(Resources.getResourceAsURLObject("Chevron Right-96.png")));
        b.setActionCommand("Right");
        b.setPreferredSize(new Dimension(30,30));
        b.addActionListener(this);
        topPanel.add(b);

        left.add(topPanel, BorderLayout.NORTH);

        seedGridPanel = new JPanel();
        left.add(seedGridPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        left.add(bottomPanel, BorderLayout.SOUTH);

        l = new JLabel();
        l.setText("Seed code");
        bottomPanel.add(l);


        seedCode = new JTextField();
        seedCode.setColumns(20);
        seedCode.addActionListener(this);
        bottomPanel.add(seedCode);

        b = new JButton("Copy");
        b.addActionListener(this);
        bottomPanel.add(b);

        b = new JButton("Save");
        b.addActionListener(this);
        bottomPanel.add(b);

        b = new JButton("Load");
        b.addActionListener(this);
        bottomPanel.add(b);

        WrapLayout outputLayout = new WrapLayout();
        outputLayout.setAlignment(FlowLayout.LEADING);

        outputPanel = new JPanel();
        outputPanel.setLayout(outputLayout);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(outputPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(0, 500));
        right.add(scrollPane, BorderLayout.CENTER);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    createOutputInThread();
                } catch (InterruptedException e) {
                    LOG.error("", e);
                }
            }
        };
        this.thread = new Thread(r);
        this.thread.start();

        resizeGrid();
    }

    public void stateChanged(ChangeEvent e) {
        resizeGrid();
    }

    private void resizeGrid() {

        int cols = ((Integer) spinnerWidth.getValue()).intValue();
        int rows = ((Integer) spinnerHeight.getValue()).intValue();

        grid = new Grid(cols, rows, grid);

        SquareLayout layout = new SquareLayout();
        layout.setRows(rows);
        layout.setCols(cols);

        seedGridPanel.removeAll();
        seedGridPanel.setLayout(layout);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                JToggleButton b = new JToggleButton();
                b.setActionCommand("" + col + "," + row);
                //b.addChangeListener(this);
                b.addActionListener(this);
                b.setSelected(grid.isOccupied(col, row));

                seedGridPanel.add(b);
            }
        }

        seedGridPanel.invalidate();
        seedGridPanel.revalidate();
        seedGridPanel.validate();
        seedGridPanel.repaint();

        updateOutput();
    }

    private void buttonPressed(int col, int row, boolean isSelected) {
        grid.occupy(col, row, isSelected);

        updateOutput();
    }


    private void updateOutput() {

        String s = grid.getSeedCode();
        seedCode.setText(s);

        synchronized (series) {

            series.clear();
            seriesSet.clear();

            GridPanel gridPanel = new GridPanel(grid);

            series.add(gridPanel);
            seriesSet.put(gridPanel.getSeedCode(), gridPanel);

            outputPanel.removeAll();
            addGridPanel(gridPanel);

        }

    }

    private void addGridPanel(GridPanel gridPanel) {
        //gridPanel.setPreferredSize(new Dimension(100, 100));
        outputPanel.add(gridPanel);

        if (gridPanel.getGrid().isDead()) {
            JLabel l = new JLabel();
            l.setText("DEAD");
            outputPanel.add(l);
        }

        outputPanel.repaint();
        outputPanel.invalidate();
        outputPanel.validate();
        outputPanel.revalidate();
    }

    private void createOutputInThread() throws InterruptedException {

        while (true) {

            Thread.sleep(100);

            GridPanel lastPanel = null;
            synchronized (series) {
                if (series.isEmpty()) {
                    continue;
                }
                lastPanel = series.get(series.size()-1);
            }
            Grid lastGrid = lastPanel.getGrid();
            //LOG.debug("updating in thread " + lastGrid.getSeedCode());

            if (lastGrid.isDead()) {
                //LOG.debug("is dead " + lastGrid.getSeedCode());
                continue;
            }

            Grid nextGrid = lastGrid.evolve();
            final GridPanel nextPanel = new GridPanel(nextGrid);

            synchronized (series) {

                if (seriesSet.containsKey(nextGrid.getSeedCode())) {

                    final GridPanel fFirstPanel = seriesSet.get(nextGrid.getSeedCode());
                    final GridPanel fLastPanel = lastPanel;

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fFirstPanel.setBackground(Color.green);
                            fLastPanel.setBackground(Color.cyan);
                        }
                    });

                } else {
                    series.add(nextPanel);
                    seriesSet.put(nextPanel.getSeedCode(), nextPanel);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            addGridPanel(nextPanel);
                        }
                    });
                }
            }

        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JToggleButton) {

            JToggleButton b = (JToggleButton) e.getSource();
            String ac = b.getActionCommand();
            String[] arr = ac.split(",");
            int col = Integer.parseInt(arr[0]);
            int row = Integer.parseInt(arr[1]);
            buttonPressed(col, row, b.isSelected());

        } else if (e.getSource() == seedCode) {

            seedCodeEntered(seedCode.getText());

        } else {

            String ac = e.getActionCommand();
            if (ac.equals("All")) {
                grid.all();
                updateButtonsFromGrid();
            } else if (ac.equals("Clear")) {
                grid.clear();
                updateButtonsFromGrid();
            } else if (ac.equals("Invert")) {
                grid.invert();
                updateButtonsFromGrid();
            } else if (ac.equals("Copy")) {
                copySeedCodeToClipboard();
            } else if (ac.equals("Up")) {
                nudge(Direction.Up);
            } else if (ac.equals("Down")) {
                nudge(Direction.Down);
            } else if (ac.equals("Left")) {
                nudge(Direction.Left);
            } else if (ac.equals("Right")) {
                nudge(Direction.Right);
            } else if (ac.equals("Save")) {
                saveGrid();
            } else if (ac.equals("Load")) {
                loadGrid();
            } else {
                LOG.error("Unknown button [" + ac + "]");
            }
        }
    }

    private JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        return fileChooser;
    }

    private void saveGrid() {

        JFileChooser fc = getFileChooser();
        int ret = fc.showDialog(this, "Select");
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try{
            File parentDir = fileChooser.getSelectedFile();
            File newDir = new File(parentDir, seedCode.getText());
            newDir.mkdir();

            for (int i=0; i<series.size(); i++) {

                File f = new File(newDir, "" + (i+1) + ".svg");
                GridPanel gridPanel = series.get(i);
                Grid grid = gridPanel.getGrid();
                grid.writeToFile(f);
            }
        } catch (Exception e) {
            String err = "Error saving:\n" + e.getMessage();
            JOptionPane.showMessageDialog(this, err);
        }


    }
    private void loadGrid() {

        JFileChooser fc = getFileChooser();
        int ret = fc.showDialog(this, "Select");
        if (ret != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dir = fileChooser.getSelectedFile();
        String name = dir.getName();

        if (seedCodeEntered(name)) {
            seedCode.setText(name);
        }
    }

    private void nudge(Direction direction) {
        grid.nudge(direction);
        updateButtonsFromGrid();
    }

    private void copySeedCodeToClipboard() {
        String s = seedCode.getText();
        if (Strings.isNullOrEmpty(s)) {
            return;
        }
        StringSelection stringSelection = new StringSelection(s);
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        clpbrd.setContents(stringSelection, null);
    }

    private boolean seedCodeEntered(String s) {
        try {
            Grid g = new Grid(s);
            this.grid = g;

            spinnerWidth.setValue(new Integer(grid.width()));
            spinnerHeight.setValue(new Integer(grid.height()));

            updateButtonsFromGrid();
            return true;
        } catch (Exception e) {
            String err = "Error reading seed:\n" + e.getMessage();
            JOptionPane.showMessageDialog(this, err);
            return false;
        }


    }

    private void updateButtonsFromGrid() {
        int w = grid.width();
        int h = grid.height();
        int buttons = seedGridPanel.getComponentCount();
        if (buttons != (w * h)) {
            resizeGrid();
        }

        for (Component comp: seedGridPanel.getComponents()) {
            JToggleButton b = (JToggleButton)comp;
            String ac = b.getActionCommand();
            String[] arr = ac.split(",");
            int col = Integer.parseInt(arr[0]);
            int row = Integer.parseInt(arr[1]);
            boolean occupied = grid.isOccupied(col, row);
            b.setSelected(occupied);
        }

        updateOutput();
    }

}

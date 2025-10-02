package paint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener {

    public enum Tool {PENCIL, LINE, RECT, OVAL, POLYGON}

    private BufferedImage canvas;
    private Point startPoint, lastPoint;
    private Color currentColor = Color.BLACK;
    private Tool currentTool = Tool.PENCIL;
    private int currentThickness = 1;

    // Polygon
    private List<Point> polygonPoints = new ArrayList<>();
    private boolean drawingPolygon = false;

    public DrawingPanel() {
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
    }

    // -------------------- Settery --------------------
    public void setTool(Tool t) { this.currentTool = t; }
    public void setThickness(int t) { this.currentThickness = Math.max(1, t); }
    public void setColor(Color c) { if (c != null) this.currentColor = c; }
    public Color getColor() { return currentColor; }

    public void clearCanvas() {
        ensureCanvas();
        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.dispose();
        repaint();
    }

    private void ensureCanvas() {
        if (canvas == null) {
            int w = Math.max(1, getWidth());
            int h = Math.max(1, getHeight());
            canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = canvas.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.dispose();
        }
    }

    // -------------------- Paint --------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ensureCanvas();
        g.drawImage(canvas, 0, 0, null);

        // LINE, RECT, OVAL podgląd
        if (startPoint != null && lastPoint != null && currentTool != Tool.PENCIL && currentTool != Tool.POLYGON) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setStroke(new BasicStroke(currentThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(currentColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

            int x = Math.min(startPoint.x, lastPoint.x);
            int y = Math.min(startPoint.y, lastPoint.y);
            int w = Math.abs(startPoint.x - lastPoint.x);
            int h = Math.abs(startPoint.y - lastPoint.y);

            switch (currentTool) {
                case LINE -> g2.drawLine(startPoint.x, startPoint.y, lastPoint.x, lastPoint.y);
                case RECT -> g2.drawRect(x, y, w, h);
                case OVAL -> g2.drawOval(x, y, w, h);
                default -> {}
            }
            g2.dispose();
        }

        // -------------------- Polygon podgląd --------------------
        if (currentTool == Tool.POLYGON && drawingPolygon && polygonPoints.size() > 0) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setStroke(new BasicStroke(currentThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(currentColor);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));

            // linie między punktami
            for (int i = 0; i < polygonPoints.size() - 1; i++) {
                Point p1 = polygonPoints.get(i);
                Point p2 = polygonPoints.get(i + 1);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            // linia od ostatniego punktu do kursora myszy
            if (lastPoint != null) {
                Point pLast = polygonPoints.get(polygonPoints.size() - 1);
                g2.drawLine(pLast.x, pLast.y, lastPoint.x, lastPoint.y);
            }

            g2.dispose();
        }
    }

    // -------------------- Rysowanie na stałe --------------------
    private void drawFinalShape(Point a, Point b) {
        ensureCanvas();
        Graphics2D g = canvas.createGraphics();
        g.setStroke(new BasicStroke(currentThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(currentColor);

        switch (currentTool) {
            case LINE -> g.drawLine(a.x, a.y, b.x, b.y);
            case RECT -> g.drawRect(Math.min(a.x, b.x), Math.min(a.y, b.y),
                    Math.abs(a.x - b.x), Math.abs(a.y - b.y));
            case OVAL -> g.drawOval(Math.min(a.x, b.x), Math.min(a.y, b.y),
                    Math.abs(a.x - b.x), Math.abs(a.y - b.y));
            default -> {}
        }
        g.dispose();
    }

    private void drawFinalPolygon() {
        ensureCanvas();
        Graphics2D g = canvas.createGraphics();
        g.setStroke(new BasicStroke(currentThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(currentColor);

        int n = polygonPoints.size();
        for (int i = 0; i < n - 1; i++) {
            Point p1 = polygonPoints.get(i);
            Point p2 = polygonPoints.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        g.dispose();
        repaint();
    }

    // -------------------- Mouse --------------------
    @Override
    public void mousePressed(MouseEvent e) {
        ensureCanvas();
        startPoint = e.getPoint();
        lastPoint = startPoint;
        if (currentTool == Tool.PENCIL) {
            Graphics2D g = canvas.createGraphics();
            g.setStroke(new BasicStroke(currentThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(currentColor);
            g.drawLine(startPoint.x, startPoint.y, startPoint.x, startPoint.y);
            g.dispose();
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        if (currentTool == Tool.PENCIL) {
            Graphics2D g = canvas.createGraphics();
            g.setStroke(new BasicStroke(currentThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(currentColor);
            g.drawLine(lastPoint.x, lastPoint.y, p.x, p.y);
            g.dispose();
            lastPoint = p;
        } else if (currentTool != Tool.POLYGON) {
            lastPoint = p;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentTool != Tool.PENCIL && currentTool != Tool.POLYGON &&
                startPoint != null && lastPoint != null) {
            drawFinalShape(startPoint, e.getPoint());
        }
        startPoint = null;
        lastPoint = null;
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (currentTool == Tool.POLYGON) {
            Point p = e.getPoint();

            if (!drawingPolygon) {
                polygonPoints.clear();
                polygonPoints.add(p);
                drawingPolygon = true;
            } else {
                Point first = polygonPoints.get(0);
                if (p.distance(first) < 10 && polygonPoints.size() > 2) {
                    polygonPoints.add(first);
                    drawFinalPolygon();
                    polygonPoints.clear();
                    drawingPolygon = false;
                } else {
                    polygonPoints.add(p);
                }
            }
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentTool == Tool.POLYGON && drawingPolygon) {
            lastPoint = e.getPoint();
            repaint();
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // -------------------- Component resize --------------------
    @Override
    public void componentResized(ComponentEvent e) {
        if (canvas == null) { ensureCanvas(); return; }
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        // jeśli nowe okno mniejsze – zostaw stary canvas
        if (w <= canvas.getWidth() && h <= canvas.getHeight()) {
            repaint();
            return;
        }

        BufferedImage newCanvas = new BufferedImage(Math.max(w, canvas.getWidth()),
                Math.max(h, canvas.getHeight()), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newCanvas.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,newCanvas.getWidth(), newCanvas.getHeight());
        g.drawImage(canvas, 0, 0, null);
        g.dispose();

        canvas = newCanvas;
        repaint();
    }

    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}
}

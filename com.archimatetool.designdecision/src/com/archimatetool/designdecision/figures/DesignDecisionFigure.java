package com.archimatetool.designdecision.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;

import com.archimatetool.editor.diagram.figures.AbstractTextControlContainerFigure;
import com.archimatetool.editor.diagram.figures.IFigureDelegate;
import com.archimatetool.editor.diagram.figures.RoundedRectangleFigureDelegate;
import com.archimatetool.editor.diagram.figures.elements.IArchimateFigure;
import com.archimatetool.editor.ui.IIconDelegate;

public class DesignDecisionFigure extends AbstractTextControlContainerFigure implements IArchimateFigure {

    private IFigureDelegate roundedRectangleDelegate;

    public DesignDecisionFigure() {
        super(TEXT_FLOW_CONTROL);
        roundedRectangleDelegate = new RoundedRectangleFigureDelegate(this);
    }

    @Override
    protected void drawFigure(Graphics graphics) {
        if(getFigureDelegate() != null) {
            getFigureDelegate().drawFigure(graphics);
            drawIcon(graphics);
            return;
        }

        graphics.pushState();

        Rectangle rect = getBounds().getCopy();
        rect.resize(-1, -1);

        setLineWidth(graphics, rect);

        Rectangle imageBounds = rect.getCopy();
        setFigurePositionFromTextPosition(rect);

        graphics.setAlpha(getAlpha());
        graphics.setBackgroundColor(getFillColor());
        Pattern gradient = applyGradientPattern(graphics, rect);

        // Draw a diamond shape to represent a decision
        Path path = new Path(null);
        int cx = rect.x + rect.width / 2;
        int cy = rect.y + rect.height / 2;
        path.moveTo(cx, rect.y);                        // top
        path.lineTo(rect.x + rect.width, cy);           // right
        path.lineTo(cx, rect.y + rect.height);          // bottom
        path.lineTo(rect.x, cy);                        // left
        path.close();

        graphics.fillPath(path);
        disposeGradientPattern(graphics, gradient);

        graphics.setAlpha(getLineAlpha());
        graphics.setForegroundColor(getLineColor());
        graphics.drawPath(path);

        path.dispose();

        drawIconImage(graphics, imageBounds, 0, 0, 0, 0);

        graphics.popState();
    }

    private void drawIcon(Graphics graphics) {
        if(isIconVisible()) {
            getIconDelegate().drawIcon(graphics, getIconColor(), null, getIconOrigin());
        }
    }

    // Small diamond icon drawn in the top-right corner when in icon mode
    private static IIconDelegate iconDelegate = new IIconDelegate() {
        @Override
        public void drawIcon(Graphics graphics, Color foregroundColor, Color backgroundColor, Point pt) {
            graphics.pushState();
            graphics.setAntialias(SWT.ON);
            graphics.setLineWidth(1);

            if(foregroundColor != null) {
                graphics.setForegroundColor(foregroundColor);
            }

            // Small diamond: 12x12
            int cx = pt.x + 6;
            int cy = pt.y + 6;
            Path path = new Path(null);
            path.moveTo(cx, pt.y);
            path.lineTo(pt.x + 12, cy);
            path.lineTo(cx, pt.y + 12);
            path.lineTo(pt.x, cy);
            path.close();
            graphics.drawPath(path);
            path.dispose();

            graphics.popState();
        }
    };

    public static IIconDelegate getIconDelegate() {
        return iconDelegate;
    }

    private Point getIconOrigin() {
        Rectangle rect = getBounds();
        return new Point(rect.getRight().x - 16 - getLineWidth(), rect.y + 5);
    }

    @Override
    public int getIconOffset() {
        return getDiagramModelArchimateObject().getType() == 0 ? 19 : 0;
    }

    @Override
    protected int getTextControlMarginHeight() {
        return getDiagramModelArchimateObject().getType() == 0 ? super.getTextControlMarginHeight() : 0;
    }

    @Override
    public IFigureDelegate getFigureDelegate() {
        return getDiagramModelArchimateObject().getType() == 0 ? roundedRectangleDelegate : null;
    }
}
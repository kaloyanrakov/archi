package com.archimatetool.editor.diagram.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import com.archimatetool.model.IArchimateElement;
import com.archimatetool.model.IDiagramModelArchimateObject;
import com.archimatetool.model.IProperty;

/**
 * Draws a small level badge (e.g. "L1") in the top-left corner
 * of an ArchiMate element figure when a "Model Level" property is set.
 */
public class LevelBadge {

    private static final String MODEL_LEVEL_KEY = "Model Level"; //$NON-NLS-1$

    private static String getBadgeLabel(String value) {
        return switch(value) {
            case "Level 1" -> "L1";
            case "Level 2" -> "L2";
            case "Level 3" -> "L3";
            default -> null;
        };
    }

    public static void draw(AbstractDiagramModelObjectFigure figure, Graphics graphics) {
        if(!(figure.getDiagramModelObject() instanceof IDiagramModelArchimateObject dmao)) {
            return;
        }

        IArchimateElement element = dmao.getArchimateElement();
        if(element == null) return;

        String levelValue = null;
        for(IProperty p : element.getProperties()) {
            if(MODEL_LEVEL_KEY.equals(p.getKey())) {
                levelValue = p.getValue();
                break;
            }
        }

        String label = (levelValue != null) ? getBadgeLabel(levelValue) : null;
        if(label == null) return;

        graphics.pushState();

        Rectangle bounds = figure.getBounds();
        int margin = 3;
        int badgeWidth = 18;
        int badgeHeight = 14;

        Rectangle badgeRect = new Rectangle(
            bounds.x + margin,
            bounds.y + margin,
            badgeWidth,
            badgeHeight
        );

        Color badgeBg = new Color(30, 90, 180);
        graphics.setBackgroundColor(badgeBg);
        graphics.setAlpha(200);
        graphics.fillRoundRectangle(badgeRect, 4, 4);

        Color white = new Color(255, 255, 255);
        graphics.setForegroundColor(white);
        graphics.setAlpha(255);

        FontData[] fontData = graphics.getFont().getFontData();
        fontData[0].setHeight(7);
        fontData[0].setStyle(SWT.BOLD);
        Font badgeFont = new Font(null, fontData);
        graphics.setFont(badgeFont);

        graphics.drawText(label, badgeRect.x + 2, badgeRect.y + 2);

        badgeFont.dispose();
        badgeBg.dispose();
        white.dispose();

        graphics.popState();
    }
}
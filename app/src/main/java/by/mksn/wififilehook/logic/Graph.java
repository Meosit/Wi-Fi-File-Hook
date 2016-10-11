package by.mksn.wififilehook.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;

import by.mksn.wififilehook.R;

public final class Graph {

    private static final int TEXT_SIZE_DEFAULT = 14;
    private static final int TEXT_OFFSET_X = -20;
    private static final int TEXT_OFFSET_Y = -15;
    private static final int OVERVIEW_GRAPH_DRAW_WIDTH = 964;
    private static final int OVERVIEW_GRAPH_DRAW_HEIGHT = 377;
    private static final int OVERVIEW_GRAPH_DRAW_OFFSET_TOP = 12;
    private static final int OVERVIEW_GRAPH_DRAW_OFFSET_LEFT = 35;
    private static final int CONCRETE_GRAPH_DRAW_OFFSET_TOP = 13;
    private static final int CONCRETE_GRAPH_DRAW_OFFSET_LEFT = 41;
    private static final int CONCRETE_GRAPH_DRAW_WIDTH = 822;
    private static final int CONCRETE_GRAPH_DRAW_HEIGHT = 581;
    private static int textDefaultColor = Color.WHITE;
    private static int drawDefaultColor = Color.WHITE;
    private final Context context;
    private Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Canvas canvas;
    private Bitmap resultBitmap;
    private int drawOffsetTop;
    private int drawOffsetLeft;
    private int drawWidth;
    private int drawHeight;

    public Graph(Context context, @DrawableRes int id) {
        this.context = context;
        Bitmap graphBackground = BitmapFactory.decodeResource(context.getResources(), R.drawable.overview);
        switch (id) {
            case R.drawable.overview:
                drawOffsetTop = OVERVIEW_GRAPH_DRAW_OFFSET_TOP;
                drawOffsetLeft = OVERVIEW_GRAPH_DRAW_OFFSET_LEFT;
                drawWidth = OVERVIEW_GRAPH_DRAW_WIDTH;
                drawHeight = OVERVIEW_GRAPH_DRAW_HEIGHT;
                break;
            case R.drawable.concrete:
                drawOffsetTop = CONCRETE_GRAPH_DRAW_OFFSET_TOP;
                drawOffsetLeft = CONCRETE_GRAPH_DRAW_OFFSET_LEFT;
                drawWidth = CONCRETE_GRAPH_DRAW_WIDTH;
                drawHeight = CONCRETE_GRAPH_DRAW_HEIGHT;
                break;
            default:
                drawOffsetTop = 0;
                drawOffsetLeft = 0;
                drawWidth = graphBackground.getWidth();
                drawHeight = graphBackground.getHeight();
                break;
        }
        resultBitmap = Bitmap.createBitmap(graphBackground.getWidth(),
                graphBackground.getHeight(), Bitmap.Config.RGB_565);
        canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(graphBackground, 0, 0, null);
    }

    public static int getTextDefaultColor() {
        return textDefaultColor;
    }

    public static void setTextDefaultColor(int textDefaultColor) {
        Graph.textDefaultColor = textDefaultColor;
    }

    public static int getDrawDefaultColor() {
        return drawDefaultColor;
    }

    public static void setDrawDefaultColor(int drawDefaultColor) {
        Graph.drawDefaultColor = drawDefaultColor;
    }

    private int getNormalizedX(int x) {
        return x + drawOffsetLeft;
    }

    private int getNormalizedY(int y) {
        return drawOffsetTop + drawHeight - y;
    }

    public int getDrawColor() {
        return drawPaint.getColor();
    }

    public void setDrawColor(int color) {
        drawPaint.setColor(color);
    }

    public int getTextColor() {
        return textPaint.getColor();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
    }

    public void drawCircle(int x, int y, int radius) {
        canvas.drawCircle(getNormalizedX(x), getNormalizedY(y), radius, drawPaint);
    }

    public void drawLine(int xStart, int yStart, int xEnd, int yEnd, int width) {
        drawPaint.setStrokeWidth(width);
        canvas.drawLine(getNormalizedX(xStart), getNormalizedY(yStart),
                getNormalizedX(xEnd), getNormalizedY(yEnd), drawPaint);
    }

    public void drawText(int startX, int bottomY, String text) {
        canvas.drawText(text, getNormalizedX(startX), getNormalizedY(bottomY), textPaint);
    }

    public Bitmap getResultBitmap() {
        return resultBitmap;
    }

    public BitmapDrawable getResultBitmapDrawable() {
        return new BitmapDrawable(context.getResources(), resultBitmap);
    }


}

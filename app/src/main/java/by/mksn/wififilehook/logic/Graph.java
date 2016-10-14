package by.mksn.wififilehook.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;

import java.util.Locale;

import by.mksn.wififilehook.R;

public final class Graph {

    private static final int TEXT_OFFSET_X = -5;
    private static final int TEXT_OFFSET_Y = -30;
    private static final int OVERVIEW_VERTICAL_LINES_COUNT = 12;
    private static final int DEFAULT_COLUMN_WIDTH = 10;
    private static final int OVERVIEW_WIDTH = 1009;
    private static final int OVERVIEW_HEIGHT = 409;
    private static final int OVERVIEW_GRAPH_DRAW_WIDTH = 964;
    private static final int OVERVIEW_GRAPH_DRAW_HEIGHT = 377;
    private static final int OVERVIEW_GRAPH_DRAW_OFFSET_TOP = 12;
    private static final int OVERVIEW_GRAPH_DRAW_OFFSET_LEFT = 35;
    private static final int CONCRETE_WIDTH = 881;
    private static final int CONCRETE_HEIGHT = 625;
    private static final int CONCRETE_GRAPH_DRAW_OFFSET_TOP = 13;
    private static final int CONCRETE_GRAPH_DRAW_OFFSET_LEFT = 41;
    private static final int CONCRETE_GRAPH_DRAW_WIDTH = 822;
    private static final int CONCRETE_GRAPH_DRAW_HEIGHT = 581;
    private static int textSizeDefault = 30;
    private static int textDefaultColor = Color.WHITE;
    private static int drawDefaultColor = Color.WHITE;
    private final Context context;
    private Paint drawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Canvas canvas;
    private Bitmap resultBitmap;
    private float drawOffsetTop;
    private float drawOffsetLeft;
    private float drawWidth;
    private float drawHeight;

    public Graph(Context context, @DrawableRes int id) {
        this.context = context;
        Bitmap graphBackground = BitmapFactory.decodeResource(context.getResources(), id);
        switch (id) {
            case R.drawable.overview:
                float heightRatio = graphBackground.getHeight() / OVERVIEW_HEIGHT;
                float widthRatio = graphBackground.getWidth() / OVERVIEW_WIDTH;
                drawOffsetTop = heightRatio * OVERVIEW_GRAPH_DRAW_OFFSET_TOP;
                drawOffsetLeft = widthRatio * OVERVIEW_GRAPH_DRAW_OFFSET_LEFT;
                drawWidth = widthRatio * OVERVIEW_GRAPH_DRAW_WIDTH;
                drawHeight = heightRatio * OVERVIEW_GRAPH_DRAW_HEIGHT;
                break;
            case R.drawable.concrete:
                heightRatio = graphBackground.getHeight() / CONCRETE_HEIGHT;
                widthRatio = graphBackground.getWidth() / CONCRETE_WIDTH;
                drawOffsetTop = heightRatio * CONCRETE_GRAPH_DRAW_OFFSET_TOP;
                drawOffsetLeft = widthRatio * CONCRETE_GRAPH_DRAW_OFFSET_LEFT;
                drawWidth = widthRatio * CONCRETE_GRAPH_DRAW_WIDTH;
                drawHeight = heightRatio * CONCRETE_GRAPH_DRAW_HEIGHT;
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
        drawPaint.setColor(drawDefaultColor);
        textPaint.setColor(textDefaultColor);
        textPaint.setTextSize(textSizeDefault);
    }

    public static void setTextDefaultColor(int textDefaultColor) {
        Graph.textDefaultColor = textDefaultColor;
    }

    public static void setDrawDefaultColor(int drawDefaultColor) {
        Graph.drawDefaultColor = drawDefaultColor;
    }

    public static void setTextSizeDefault(int textSizeDefault) {
        Graph.textSizeDefault = textSizeDefault;
    }

    private float getNormalizedX(float x) {
        return x + drawOffsetLeft;
    }

    private float getNormalizedY(float y) {
        return drawOffsetTop + drawHeight - y;
    }

    private int calculateMaxScaleBorder(int[] values) {
        int maxScaleBorder = Integer.MIN_VALUE;
        for (int i = 0; i < FurnacesStats.TEMPERATURE_SENSOR_COUNT; i++) {
            if (maxScaleBorder <= values[i]) {
                maxScaleBorder = values[i];
            }
        }
        return ((maxScaleBorder + 99) / 100) * 100;
    }

    private void drawLine(float xStart, float yStart, float xEnd, float yEnd, float width) {
        drawPaint.setStrokeWidth(width);
        canvas.drawLine(getNormalizedX(xStart), getNormalizedY(yStart),
                getNormalizedX(xEnd), getNormalizedY(yEnd), drawPaint);
    }

    private void drawText(float startX, float bottomY, String text) {
        canvas.drawText(text, getNormalizedX(startX), getNormalizedY(bottomY), textPaint);
    }

    public void drawConcreteGraph(int[] values) {
        //float scaleRatio = drawHeight / (maxScaleBorder - minScaleBorder);
        float valueHorizontalOffset = drawWidth / (values.length + 2);
    }

    public void drawOverviewGraph(FurnacesStats.ValuesTimestamp timestamp) {
        float width = drawWidth / OVERVIEW_WIDTH * DEFAULT_COLUMN_WIDTH;
        float textMargin = textPaint.measureText("0") / 2;
        int minScaleBorder = 0;
        int maxScaleBorder = calculateMaxScaleBorder(timestamp.getValues());

        float scaleRatio = drawHeight / (maxScaleBorder - minScaleBorder);
        float valueHorizontalOffset = drawWidth / (FurnacesStats.TEMPERATURE_SENSOR_COUNT + 1);
        float offsetX = valueHorizontalOffset;

        for (int i = 0; i < FurnacesStats.TEMPERATURE_SENSOR_COUNT; i++, offsetX += valueHorizontalOffset) {
            float valueY = timestamp.getValue(i) * scaleRatio;
            drawLine(offsetX, minScaleBorder, offsetX, valueY, width);
            drawText(offsetX - (textPaint.measureText(String.valueOf(i + 1)) / 2),
                    0 - textSizeDefault, String.valueOf(i + 1));
            drawText(offsetX - (textPaint.measureText(String.valueOf(timestamp.getValue(i))) / 2),
                    valueY + textMargin, String.valueOf(timestamp.getValue(i)));
        }
        textPaint.setTextAlign(Paint.Align.RIGHT);
        float yBorderMarkingOffset = drawHeight / OVERVIEW_VERTICAL_LINES_COUNT;
        float yBorderMarking = 0;
        for (int i = 0; i <= OVERVIEW_VERTICAL_LINES_COUNT; i++, yBorderMarking += yBorderMarkingOffset) {
            drawText(TEXT_OFFSET_X, yBorderMarking - (textSizeDefault / 2),
                    String.format(Locale.getDefault(), "%.0f", yBorderMarking / scaleRatio));
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
        drawText(textMargin, drawHeight - textSizeDefault, context.getString(R.string.graph_overview_label_time, timestamp.time));
    }

    public Bitmap getResultBitmap() {
        return resultBitmap;
    }

    public BitmapDrawable getResultBitmapDrawable() {
        return new BitmapDrawable(context.getResources(), resultBitmap);
    }


}

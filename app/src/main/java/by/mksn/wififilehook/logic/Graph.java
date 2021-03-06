package by.mksn.wififilehook.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.DrawableRes;

import java.util.Arrays;
import java.util.Locale;

import by.mksn.wififilehook.R;

public final class Graph {

    private static final int OVERVIEW_HORIZONTAL_LINES_COUNT = 12;
    private static final int OVERVIEW_WIDTH = 1009;
    private static final int OVERVIEW_HEIGHT = 409;
    private static final int OVERVIEW_GRAPH_DRAW_WIDTH = 964;
    private static final int OVERVIEW_GRAPH_DRAW_HEIGHT = 377;
    private static final int OVERVIEW_GRAPH_DRAW_OFFSET_TOP = 12;
    private static final int OVERVIEW_GRAPH_DRAW_OFFSET_LEFT = 35;
    private static final int CONCRETE_HORIZONTAL_LINES_COUNT = 48;
    private static final int CONCRETE_WIDTH = 881;
    private static final int CONCRETE_HEIGHT = 625;
    private static final int CONCRETE_GRAPH_DRAW_OFFSET_TOP = 13;
    private static final int CONCRETE_GRAPH_DRAW_OFFSET_LEFT = 41;
    private static final int CONCRETE_GRAPH_DRAW_WIDTH = 822;
    private static final int CONCRETE_GRAPH_DRAW_HEIGHT = 581;
    private static int defaultColumnWidth = 10;
    private static int defaultDotRadius = 8;
    private static int defaultLineWidth = 4;
    private static int overviewTextSizeDefault = 30;
    private static int overviewTextDefaultColor = Color.YELLOW;
    private static int overviewDrawDefaultColor = Color.WHITE;
    private static int concreteTextSizeDefault = 30;
    private static int concreteTextDefaultColor = Color.YELLOW;
    private static int concreteDrawDefaultColor = Color.WHITE;

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
                drawPaint.setColor(overviewDrawDefaultColor);
                textPaint.setColor(overviewTextDefaultColor);
                textPaint.setTextSize(overviewTextSizeDefault);
                break;
            case R.drawable.concrete:
                heightRatio = graphBackground.getHeight() / CONCRETE_HEIGHT;
                widthRatio = graphBackground.getWidth() / CONCRETE_WIDTH;
                drawOffsetTop = heightRatio * CONCRETE_GRAPH_DRAW_OFFSET_TOP;
                drawOffsetLeft = widthRatio * CONCRETE_GRAPH_DRAW_OFFSET_LEFT;
                drawWidth = widthRatio * CONCRETE_GRAPH_DRAW_WIDTH;
                drawHeight = heightRatio * CONCRETE_GRAPH_DRAW_HEIGHT;
                drawPaint.setColor(concreteDrawDefaultColor);
                textPaint.setColor(concreteTextDefaultColor);
                textPaint.setTextSize(concreteTextSizeDefault);
                break;
            default:
                drawOffsetTop = 0;
                drawOffsetLeft = 0;
                drawWidth = graphBackground.getWidth();
                drawHeight = graphBackground.getHeight();
                drawPaint.setColor(overviewDrawDefaultColor);
                textPaint.setColor(overviewTextDefaultColor);
                textPaint.setTextSize(overviewTextSizeDefault);
                break;
        }
        resultBitmap = Bitmap.createBitmap(graphBackground.getWidth(),
                graphBackground.getHeight(), Bitmap.Config.RGB_565);
        canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(graphBackground, 0, 0, null);
    }

    public static void setOverviewTextDefaultColor(int overviewTextDefaultColor) {
        Graph.overviewTextDefaultColor = overviewTextDefaultColor;
    }

    public static void setOverviewDrawDefaultColor(int overviewDrawDefaultColor) {
        Graph.overviewDrawDefaultColor = overviewDrawDefaultColor;
    }

    public static void setOverviewTextSizeDefault(int overviewTextSizeDefault) {
        Graph.overviewTextSizeDefault = overviewTextSizeDefault;
    }

    public static void setConcreteTextDefaultColor(int concreteTextDefaultColor) {
        Graph.concreteTextDefaultColor = concreteTextDefaultColor;
    }

    public static void setConcreteDrawDefaultColor(int concreteDrawDefaultColor) {
        Graph.concreteDrawDefaultColor = concreteDrawDefaultColor;
    }

    public static void setConcreteTextSizeDefault(int concreteTextSizeDefault) {
        Graph.concreteTextSizeDefault = concreteTextSizeDefault;
    }

    public static void setDefaultColumnWidth(int defaultColumnWidth) {
        Graph.defaultColumnWidth = defaultColumnWidth;
    }

    public static void setDefaultDotRadius(int defaultDotRadius) {
        Graph.defaultDotRadius = defaultDotRadius;
    }

    public static void setDefaultLineWidth(int defaultLineWidth) {
        Graph.defaultLineWidth = defaultLineWidth;
    }

    private float getNormalizedX(float x) {
        return x + drawOffsetLeft;
    }

    private float getNormalizedY(float y) {
        return drawOffsetTop + drawHeight - y;
    }

    private float calculateMaxConcreteValue(float[] values) {
        float maxScaleBorder = Float.MIN_VALUE;
        for (float value : values) {
            if (maxScaleBorder <= value) {
                maxScaleBorder = value;
            }
        }
        return ((maxScaleBorder + 9) / 10) * 10;
    }

    private float calculateMinConcreteValue(float[] values) {
        float minScaleBorder = Float.MAX_VALUE;
        for (float value : values) {
            if (minScaleBorder >= value) {
                minScaleBorder = value;
            }
        }
        return (Math.abs(minScaleBorder % 10) < 0.001) ? (minScaleBorder - 10) : ((minScaleBorder / 10) * 10);
    }

    private float calculateMaxOverviewValue(float[] values) {
        float maxScaleBorder = Float.MIN_VALUE;
        for (float value : values) {
            if (maxScaleBorder <= value) {
                maxScaleBorder = value;
            }
        }
        return ((maxScaleBorder + 99) / 100) * 100;
    }

    private float calculateMinOverviewValue(float[] values) {
        float minScaleBorder = Float.MAX_VALUE;
        for (float value : values) {
            if (minScaleBorder >= value) {
                minScaleBorder = value;
            }
        }
        return (Math.abs(minScaleBorder % 10) < 0.001) ? (minScaleBorder - 10) : ((minScaleBorder / 10) * 10);
    }

    private void drawLine(float xStart, float yStart, float xEnd, float yEnd, float width) {
        drawPaint.setStrokeWidth(width);
        canvas.drawLine(getNormalizedX(xStart), getNormalizedY(yStart),
                getNormalizedX(xEnd), getNormalizedY(yEnd), drawPaint);
    }

    private void drawCircle(float x, float y, float radius) {
        canvas.drawCircle(getNormalizedX(x), getNormalizedY(y), radius, drawPaint);
    }

    private void drawText(float startX, float bottomY, String text) {
        canvas.drawText(text, getNormalizedX(startX), getNormalizedY(bottomY), textPaint);
    }

    public void drawConcreteGraph(SensorsStats.TimeValue[] timeValues, int minHour, int maxHour) {
        String minTime = String.format(Locale.ROOT, "%02d:00:00", minHour);
        String maxTime = String.format(Locale.ROOT, "%02d:00:00", maxHour);
        int minIndex = 0, maxIndex = timeValues.length - 1;
        for (int i = timeValues.length - 1; i >= 0; i--) {
            if (timeValues[i].time.compareTo(minTime) <= 0) {
                minIndex = i;
                break;
            }
        }
        for (int i = 0; i < timeValues.length; i++) {
            if (timeValues[i].time.compareTo(maxTime) >= 0) {
                maxIndex = i;
                break;
            }
        }

        timeValues = Arrays.copyOfRange(timeValues, minIndex, maxIndex);
        if (timeValues.length == 0) {
            return;
        }

        float[] values = new float[timeValues.length];
        String[] times = new String[timeValues.length];
        for (int i = 0; i < timeValues.length; i++) {
            SensorsStats.TimeValue timeValue = timeValues[i];
            times[i] = timeValue.time;
            values[i] = timeValue.value;
        }

        float dotRadius = drawWidth / CONCRETE_WIDTH * (defaultDotRadius / 4);
        float lineWidth = drawWidth / CONCRETE_WIDTH * (defaultLineWidth / 4);
        float textMargin = textPaint.measureText("0") / 2;
        float minValue = calculateMinConcreteValue(values);
        float maxValue = calculateMaxConcreteValue(values);

        float scaleRatio = drawHeight / (maxValue - minValue);

        int timeRangeInSeconds = (maxHour - minHour) * 3600;
        int minTimeInSeconds = minHour * 3600;
        float secondInPixels = drawWidth / (timeRangeInSeconds);
        float oldValueY = (values[0] - minValue) * scaleRatio;
        float oldValueX = (SensorsStats.timeToSeconds(times[0]) - minTimeInSeconds) * secondInPixels;
        for (int i = 0; i < values.length; i++) {
            int timeInSeconds = SensorsStats.timeToSeconds(times[i]) - minTimeInSeconds;
            float valueY = (values[i] - minValue) * scaleRatio;
            float valueX = timeInSeconds * secondInPixels;
            if (((valueX - oldValueX) / secondInPixels) <= SensorsStats.getGraphBreakSecondRange()) {
                drawLine(oldValueX, oldValueY, valueX, valueY, lineWidth);
            }
            drawCircle(valueX, valueY, dotRadius);
            oldValueY = valueY;
            oldValueX = valueX;
        }

        textPaint.setTextAlign(Paint.Align.RIGHT);
        float yBorderMarkingOffset = drawHeight / (CONCRETE_HORIZONTAL_LINES_COUNT / 2);
        float yBorderMarking = 0;
        for (int i = 0; i <= CONCRETE_HORIZONTAL_LINES_COUNT; i++, yBorderMarking += yBorderMarkingOffset) {
            drawText(-textMargin, yBorderMarking - (concreteTextSizeDefault / 2),
                    String.format(Locale.getDefault(), "%.0f", yBorderMarking / scaleRatio + minValue));
        }
        textPaint.setTextAlign(Paint.Align.LEFT);

        for (int i = minHour; i <= maxHour; i++) {
            float offsetX = ((i - minHour) * 3600) * secondInPixels;
            drawText(offsetX, 0 - (concreteTextSizeDefault) - textMargin,
                    String.format(Locale.getDefault(), "%02d:00", (i) % 24));
        }
    }

    public void drawOverviewGraph(SensorsStats.ValuesTimestamp timestamp) {
        int actualSensorCount = (SensorsStats.getTemperatureSensorCount() > timestamp.getValueCount()) ?
                timestamp.getValueCount() :
                SensorsStats.getTemperatureSensorCount();
        float columnWidth = drawWidth / OVERVIEW_WIDTH * defaultColumnWidth;
        float textMargin = textPaint.measureText("0") / 2;
        float minValue = calculateMinOverviewValue(Arrays.copyOf(timestamp.getValues(), actualSensorCount));
        if (minValue >= 0) {
            minValue = 0;
        }
        float maxValue = calculateMaxOverviewValue(Arrays.copyOf(timestamp.getValues(), actualSensorCount));

        float scaleRatio = drawHeight / (maxValue - minValue);
        float valueHorizontalOffset = drawWidth / (actualSensorCount + 1);
        float offsetX = valueHorizontalOffset;

        for (int i = 0; i < actualSensorCount; i++, offsetX += valueHorizontalOffset) {
            float valueY = (timestamp.getValue(i) - minValue) * scaleRatio;
            drawLine(offsetX, 0, offsetX, valueY, columnWidth);
            drawText(offsetX - (textPaint.measureText(String.valueOf(i + 1)) / 2),
                    0 - overviewTextSizeDefault, String.valueOf(i + 1));
            drawText(offsetX - (textPaint.measureText(String.valueOf(timestamp.getValue(i))) / 2),
                    valueY + textMargin, String.format(Locale.getDefault(), "%.1f", timestamp.getValue(i)));
        }
        textPaint.setTextAlign(Paint.Align.RIGHT);
        float yBorderMarkingOffset = drawHeight / OVERVIEW_HORIZONTAL_LINES_COUNT;
        float yBorderMarking = 0;
        for (int i = 0; i <= OVERVIEW_HORIZONTAL_LINES_COUNT; i++, yBorderMarking += yBorderMarkingOffset) {
            drawText(-textMargin, yBorderMarking - (overviewTextSizeDefault / 2),
                    String.format(Locale.getDefault(), "%.0f", yBorderMarking / scaleRatio + minValue));
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    public Bitmap getResultBitmap() {
        return resultBitmap;
    }

    public BitmapDrawable getResultBitmapDrawable() {
        return new BitmapDrawable(context.getResources(), resultBitmap);
    }


}

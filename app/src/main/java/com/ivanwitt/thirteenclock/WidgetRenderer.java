package com.ivanwitt.thirteenclock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.time.ZonedDateTime;

public final class WidgetRenderer {
    private static final int SIZE = 360;

    private WidgetRenderer() {}

    public static Bitmap render(ZonedDateTime now) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float cx = SIZE / 2f;
        float cy = SIZE / 2f;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(17, 17, 20));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, 174f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.rgb(235, 235, 240));
        canvas.drawCircle(cx, cy, 166f, paint);

        // 20 minute divisions around the outer scale.
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = 0; i < 20; i++) {
            double a = Math.toRadians(i * 360.0 / 20.0 - 90.0);
            float x1 = cx + (float) Math.cos(a) * 158f;
            float y1 = cy + (float) Math.sin(a) * 158f;
            float x2 = cx + (float) Math.cos(a) * 151f;
            float y2 = cy + (float) Math.sin(a) * 151f;
            paint.setStrokeWidth(2f);
            paint.setColor(Color.rgb(145, 145, 155));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        // 13 hour marks and numerals; 13 occupies the top position like 12 on a normal clock.
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(24f);
        for (int i = 0; i < 13; i++) {
            double a = Math.toRadians(i * 360.0 / 13.0 - 90.0);
            float x1 = cx + (float) Math.cos(a) * 160f;
            float y1 = cy + (float) Math.sin(a) * 160f;
            float x2 = cx + (float) Math.cos(a) * 145f;
            float y2 = cy + (float) Math.sin(a) * 145f;
            paint.setStrokeWidth(4f);
            paint.setColor(Color.rgb(235, 235, 240));
            canvas.drawLine(x1, y1, x2, y2, paint);

            String label = i == 0 ? "13" : Integer.toString(i);
            float tx = cx + (float) Math.cos(a) * 124f;
            float ty = cy + (float) Math.sin(a) * 124f;
            Rect bounds = new Rect();
            paint.getTextBounds(label, 0, label.length(), bounds);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(label, tx, ty - bounds.exactCenterY(), paint);
            paint.setStyle(Paint.Style.STROKE);
        }

        ThirteenTime.Result t = ThirteenTime.from(now);

        drawHand(canvas, cx, cy, t.hourAngle - 90.0, 92f, 8f, Color.rgb(239, 239, 244));
        drawHand(canvas, cx, cy, t.minuteAngle - 90.0, 132f, 4f, Color.rgb(232, 198, 106));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(232, 198, 106));
        canvas.drawCircle(cx, cy, 7f, paint);

        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        paint.setTextSize(22f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.rgb(232, 198, 106));
        canvas.drawText(t.digital(), cx, cy + 64f, paint);

        return bitmap;
    }

    private static void drawHand(Canvas canvas, float cx, float cy, double angleDeg,
                                 float length, float width, int color) {
        double a = Math.toRadians(angleDeg);
        float x = cx + (float) Math.cos(a) * length;
        float y = cy + (float) Math.sin(a) * length;
        Paint hand = new Paint(Paint.ANTI_ALIAS_FLAG);
        hand.setStyle(Paint.Style.STROKE);
        hand.setStrokeCap(Paint.Cap.ROUND);
        hand.setStrokeWidth(width);
        hand.setColor(color);
        canvas.drawLine(cx, cy, x, y, hand);
    }
}

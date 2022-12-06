package fr.antoinectx.roomview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class CompassView extends android.view.View {
    private final Paint paint = new Paint();
    private final Rect dimensions = new Rect();
    private float radians = 0;

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeWidth(10);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dimensions.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(android.graphics.Canvas canvas) {
        super.onDraw(canvas);

        // compass background
        paint.setColor(Color.BLACK);
        float radius = Math.min(dimensions.width(), dimensions.height()) / 3f;
        canvas.drawCircle(dimensions.width() / 2f, dimensions.height() / 2f, radius, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(dimensions.width() / 2f, dimensions.height() / 2f, radius - 4, paint);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(dimensions.width() / 2f, dimensions.height() / 2f, radius / 8, paint);

        // compass target
        paint.setColor(Color.LTGRAY);
        canvas.drawCircle(dimensions.width() / 2f, dimensions.height() / 2f - radius, radius / 4, paint);

        // compass needle
        if (radians > Math.PI / 4 && radians < Math.PI * 3 / 4) {
            paint.setColor(Color.GREEN);
        } else {
            paint.setColor(Color.RED);
        }
        int x = (int) (dimensions.width() / 2 + radius * Math.cos(radians));
        int y = (int) (dimensions.height() / 2 - radius * Math.sin(radians));
        canvas.drawCircle(x, y, radius / 3, paint);
        canvas.drawLine(dimensions.width() / 2f, dimensions.height() / 2f, x, y, paint);
    }

    public void setRadians(float rad) {
        radians = rad + (float) Math.PI / 2f;
        this.invalidate();
    }
}

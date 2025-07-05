package com.nhom11.innowave;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {
    private Paint outsidePaint;
    private Paint borderPaint;
    private RectF rect;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Paint cho vùng tối
        outsidePaint = new Paint();
        outsidePaint.setColor(0x88000000); // Đen mờ

        // Paint cho viền trắng
        borderPaint = new Paint();
        borderPaint.setColor(0xFFFFFFFF);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(8f); // Độ dày viền
        borderPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Tính toán vùng chọn (ở giữa màn hình, tỉ lệ 4:3 hoặc tuỳ ý)
        int width = getWidth();
        int height = getHeight();
        int rectWidth = (int) (width * 0.8);
        int rectHeight = (int) (height * 0.5);
        int left = (width - rectWidth) / 2;
        int top = (height - rectHeight) / 2;
        int right = left + rectWidth;
        int bottom = top + rectHeight;
        rect = new RectF(left, top, right, bottom);

        // Vẽ vùng tối bên ngoài
        canvas.drawRect(0, 0, width, top, outsidePaint); // trên
        canvas.drawRect(0, bottom, width, height, outsidePaint); // dưới
        canvas.drawRect(0, top, left, bottom, outsidePaint); // trái
        canvas.drawRect(right, top, width, bottom, outsidePaint); // phải

        // Vẽ viền trắng
        canvas.drawRect(rect, borderPaint);
    }
}
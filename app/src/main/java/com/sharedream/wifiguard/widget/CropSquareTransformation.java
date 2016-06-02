package com.sharedream.wifiguard.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.sharedream.wifiguard.app.AppContext;
import com.sharedream.wifiguard.utils.DisplayUtils;
import com.squareup.picasso.Transformation;

public class CropSquareTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;
        Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
        if (result != source) {
            source.recycle();
        }
        Bitmap output = Bitmap.createBitmap(result.getWidth(),
                result.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, result.getWidth(),
                result.getHeight());
        final RectF rectF = new RectF(new Rect(0, 0, result.getWidth(),
                result.getHeight()));
        final float roundPx = DisplayUtils.dip2px(AppContext.getContext(),5);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        final Rect src = new Rect(0, 0, result.getWidth(),
                result.getHeight());

        canvas.drawBitmap(result, src, rect, paint);
        result.recycle();

        return output;
    }

    @Override
    public String key() {
        return "square()";
    }
}



package cheng.app.cnbeta.ui;

import cheng.app.cnbeta.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class CircularImageView extends ImageView {
    private Bitmap bitmap;
    private float borderWidth = 2.0f;
    private float center;
    private boolean drawBorder = true;
    private int height;
    private Paint paint;
    private Paint paintBorder;
    private BitmapShader shader;
    private int width;

    public CircularImageView(Context context) {
        super(context);
        setup();
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setShader() {
        if ((bitmap != null) && (width > 0) && (height > 0)) {
            shader = new BitmapShader(Bitmap.createScaledBitmap(bitmap, width,
                    height, false), TileMode.CLAMP, TileMode.CLAMP);
            paint.setShader(shader);
        }
    }

    private void setup() {
        Resources res = getResources();
        borderWidth = res.getDimensionPixelSize(R.dimen.circular_image_border);
        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setColor(res.getColor(R.color.circular_image_border_color));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(borderWidth);
        paintBorder.setAntiAlias(true);
    }

    public void onDraw(Canvas canvas) {
        if ((bitmap != null) && (shader != null)) {
            float f1 = center - 2 * (int) borderWidth;
            float f2 = center - ((int) borderWidth >> 1);
            canvas.drawCircle(center, center, f1, paint);
            if (drawBorder)
                canvas.drawCircle(center, center, f2 - borderWidth,
                        paintBorder);
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        center = (width >> 1);
        setShader();
    }

    public void setDrawBorder(boolean drawBorder) {
        this.drawBorder = drawBorder;
        invalidate();
    }

    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if ((drawable instanceof BitmapDrawable)) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            setShader();
        } else {
            shader = null;
        }
        invalidate();
    }
}

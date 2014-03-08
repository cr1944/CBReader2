
package cheng.app.cnbeta.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class ImageUtil {
    private static final String TAG = "ImageUtil";

    public static boolean hasSdcard() {
        try {
            return Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getScreenshotName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    public static String saveScreenshot(Bitmap bitmap) {
        String result = "";
        if (bitmap != null) {
            String fileName = getScreenshotName();
            File dir = new File(Configs.SCREENSHOT_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(Configs.SCREENSHOT_PATH, fileName);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(f);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            result = Configs.SCREENSHOT_PATH + '/' + fileName;
        }
        return result;
    }

    public static String getFileName(String url) {
        String path = url.toLowerCase();
        StringBuilder sb = new StringBuilder();
        path = path.replaceAll("[^\\w]+", "_");
        sb.append(path);
        if (path.endsWith("png")) {
            sb.append(".png");
        } else if (path.endsWith("gif")) {
            sb.append(".gif");
        } else {
            sb.append(".jpg");
        }
        Log.d(TAG,"getFileName: "+sb.toString());
        return sb.toString();
    }

    public static void removeImage(String url, SQLiteDatabase db) {
        Log.d(TAG, "removeImage: " + url);
        String path = Configs.IMAGE_PATH + "/" + getFileName(url);
        File file = new File(path);
        if (!file.exists()) {
            Log.e(TAG, "removeImage: file not exists");
            return;
        }
        file.delete();
    }

    public static void storeImage(byte[] data, String url, SQLiteDatabase db) {
        Log.d(TAG, "storeImage: " + url);
        OutputStream outputStream = null;
        String name = getFileName(url);

        File dir = new File(Configs.IMAGE_PATH);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(Configs.IMAGE_PATH, name);
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();

//            ContentValues values = new ContentValues();
//            values.put(Columns.DATA_URL, url);
//            values.put(Columns.PATH, Configs.IMAGE_PATH + "/" + name);
//            db.insert(TABLES.IMAGE, null, values);
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
        } catch (IOException ex) {
            Log.w(TAG, ex);
        }
    }

    public static byte[] readFromStore(String url, SQLiteDatabase db) {
        Log.d(TAG, "readFromStore: " + url);
        String path = Configs.IMAGE_PATH + "/" + getFileName(url);
        try {
            File file = new File(path);
            if (!file.exists()) {
                Log.e(TAG, "readFromStore: image not found in store");
                return null;
            } else {
                FileInputStream f = new FileInputStream(path);
                int length = f.available();
                byte[] buffer = new byte[length];
                f.read(buffer);
                f.close();
                return buffer;
            }
        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
            return null;
        } catch (IOException ex) {
            Log.w(TAG, ex);
            return null;
        }
    }

    public static InputStream getRequest(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        if (conn.getResponseCode() == 200) {
            return conn.getInputStream();
        }
        return null;
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

    public static Drawable loadImageFromUrl(String url) throws IOException {
        URL m;
        InputStream i = null;
        m = new URL(url);
        i = (InputStream) m.getContent();
        Drawable d = Drawable.createFromStream(i, "src");
        return d;
    }

    public static Drawable getDrawableFromUrl(String url) throws Exception {
        return Drawable.createFromStream(getRequest(url), null);
    }

    public static Bitmap getBitmapFromUrl(String url) throws Exception {
        byte[] bytes = getBytesFromUrl(url);
        return byteToBitmap(bytes);
    }

    public static Bitmap getRoundBitmapFromUrl(String url, int pixels) throws Exception {
        byte[] bytes = getBytesFromUrl(url);
        Bitmap bitmap = byteToBitmap(bytes);
        return toRoundCorner(bitmap, pixels);
    }

    public static Drawable geRoundDrawableFromUrl(String url, int pixels) throws Exception {
        byte[] bytes = getBytesFromUrl(url);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) byteToDrawable(bytes);
        return toRoundCorner(bitmapDrawable, pixels);
    }

    public static byte[] getBytesFromUrl(String url) throws Exception {
        return readInputStream(getRequest(url));
    }

    public static Bitmap byteToBitmap(byte[] byteArray) {
        if (byteArray.length != 0) {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else {
            return null;
        }
    }

    public static Drawable byteToDrawable(byte[] byteArray) {
        ByteArrayInputStream ins = new ByteArrayInputStream(byteArray);
        return Drawable.createFromStream(ins, null);
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable
                .getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal, int pixels) {
        return toRoundCorner(toGrayscale(bmpOriginal), pixels);
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap
                .createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    public static Bitmap toRound(Bitmap bitmap) {

        Bitmap output = Bitmap
                .createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        float cx = bitmap.getWidth() / 2;
        float cy = bitmap.getHeight() / 2;
        float radius = bitmap.getWidth() / 2;
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static BitmapDrawable toRoundCorner(BitmapDrawable bitmapDrawable, int pixels) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmapDrawable = new BitmapDrawable(toRoundCorner(bitmap, pixels));
        return bitmapDrawable;
    }

}

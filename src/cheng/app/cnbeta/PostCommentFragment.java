package cheng.app.cnbeta;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;

import cheng.app.cnbeta.util.Configs;
import cheng.app.cnbeta.util.HttpUtil;
import cheng.app.cnbeta.util.ImageUtil;

public class PostCommentFragment extends DialogFragment {
    class LoadValidateTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            byte[] b = HttpUtil.getInstance().httpGetByte(Configs.VALIDATE_URL);
            if (b != null)
                return ImageUtil.byteToBitmap(b);
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
        }
    }

}

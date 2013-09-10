package cheng.app.cnbeta;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import cheng.app.cnbeta.util.Configs;
import cheng.app.cnbeta.util.HttpUtil;
import cheng.app.cnbeta.util.ImageUtil;

public class PostCommentFragment extends DialogFragment implements OnClickListener {
    private static final String TAG = "PostCommentFragment";
    ProgressBar mProgressBar;
    View mContentLauout;
    ImageView mSecurityImage;
    Button mRefreshButton;
    Button mOkButton;
    Button mCancelButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cmt_post_layout, container, false);
        mProgressBar = (ProgressBar) v.findViewById(R.id.layout_progress);
        mContentLauout = v.findViewById(R.id.layout_content);
        mSecurityImage = (ImageView) v.findViewById(R.id.security_img);
        mRefreshButton = (Button) v.findViewById(R.id.refresh_button);
        mOkButton = (Button) v.findViewById(android.R.id.button1);
        mCancelButton = (Button) v.findViewById(android.R.id.button2);
        mRefreshButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh_button:
                refresh();
                break;
            case android.R.id.button1:
                break;
            case android.R.id.button2:
                dismiss();
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        refresh();
    }

    private void refresh() {
        setContentShown(false);
        new LoadValidateTask((PageDetailActivity)getActivity()).execute();
    }

    public void setContentShown(boolean show) {
        mProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        mContentLauout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setSecurityImage(Bitmap bm) {
        if (bm == null)
            mSecurityImage.setImageResource(R.drawable.ic_error_gold_40);
        else
            mSecurityImage.setImageBitmap(bm);
    }

    static class LoadValidateTask extends AsyncTask<Void, Void, Bitmap> {
        WeakReference<PageDetailActivity> mActivity;

        public LoadValidateTask(PageDetailActivity a) {
            mActivity = new WeakReference<PageDetailActivity>(a);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            String s = HttpUtil.getInstance().httpGet(Configs.VALIDATE_URL);
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject jo = new JSONObject(s);
                    String url = jo.getString("url");
                    if (!TextUtils.isEmpty(url)) {
                        byte[] b = HttpUtil.getInstance().httpGetByte(Configs.ROOT_URL + url);
                        if (b != null)
                            return ImageUtil.byteToBitmap(b);
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            PageDetailActivity a = mActivity.get();
            if (a != null && !a.isFinishing()) {
                PostCommentFragment f = (PostCommentFragment) a.getSupportFragmentManager()
                        .findFragmentByTag(PageDetailActivity.TAG_CMT_FRAGMENT);
                if (f != null && f.isResumed()) {
                    f.setSecurityImage(result);
                    f.setContentShown(true);
                }
            }
        }
    }

}

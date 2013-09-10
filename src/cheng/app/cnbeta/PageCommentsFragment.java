package cheng.app.cnbeta;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import cheng.app.cnbeta.util.Configs;
import cheng.app.cnbeta.util.DataUtil;
import cheng.app.cnbeta.util.HttpUtil;
import cheng.app.cnbeta.util.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class PageCommentsFragment extends ListFragment {
    private List<CBComment> mData = new LinkedList<CBComment>();
    private CommentListAdapter mAdapter;
    private boolean mIsLoading;

    private OnClickListener mSupportListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            CBComment cmt = (CBComment) v.getTag();
            if (cmt != null) {
                new ActionTask(PageCommentsFragment.this, ActionTask.TYPE_SUPPORT, cmt.tid).execute();
            }
        }
    };

    private OnClickListener mAgainstListener = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            CBComment cmt = (CBComment) v.getTag();
            if (cmt != null) {
                new ActionTask(PageCommentsFragment.this, ActionTask.TYPE_AGAINST, cmt.tid).execute();
            }
        }
    };

    public void support(long tid) {
        if (!mData.isEmpty()) {
            int size = mData.size();
            for (int i = 0; i < size; i++) {
                CBComment item = mData.get(i);
                if (item.tid == tid) {
                    item.support += 1;
                    mData.set(i, item);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void aggainst(long tid) {
        if (!mData.isEmpty()) {
            int size = mData.size();
            for (int i = 0; i < size; i++) {
                CBComment item = mData.get(i);
                if (item.tid == tid) {
                    item.against += 1;
                    mData.set(i, item);
                    break;
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lv = getListView();
        lv.setItemsCanFocus(true);
        lv.setDrawSelectorOnTop(true);
        lv.setDivider(view.getResources().getDrawable(android.R.color.transparent));
        lv.setDividerHeight(view.getResources().getDimensionPixelSize(R.dimen.multipane_padding));
        setEmptyText(getString(R.string.cmt_empty_text));
        mAdapter = new CommentListAdapter(getActivity(), mData);
        lv.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    public void LoadData(long articleId) {
        if (mIsLoading) return;
        if (mData.isEmpty()) {
            setListShown(false);
            mIsLoading = true;
            new LoadTask(this).execute(articleId);
        } else {
            setListShown(true);
            mAdapter.notifyDataSetChanged();
        }
    }

    class CommentListAdapter extends ArrayAdapter<CBComment> {
        final LayoutInflater mInflater;

        public CommentListAdapter(Context context, List<CBComment> list) {
            super(context, 0, list);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CommentViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.cmt_list_item, null);
            }
            Object tag = convertView.getTag();
            if (tag instanceof CommentViewHolder) {
                holder = (CommentViewHolder) tag;
            }
            CBComment item = getItem(position);
            if (holder == null) {
                holder = new CommentViewHolder();
                convertView.setTag(holder);
                holder.name = (TextView) convertView.findViewById(R.id.cmt_list_name);
                holder.time = (TextView) convertView.findViewById(R.id.cmt_list_time);
                holder.comment = (TextView) convertView.findViewById(R.id.cmt_list_text);
                holder.support = (Button) convertView.findViewById(R.id.cmt_list_support);
                holder.against = (Button) convertView.findViewById(R.id.cmt_list_against);
                holder.support.setOnClickListener(mSupportListener);
                holder.against.setOnClickListener(mAgainstListener);
            }
            holder.support.setTag(item);
            holder.against.setTag(item);
            if (TextUtils.isEmpty(item.name))
                holder.name.setText(R.string.no_name);
            else
                holder.name.setText(item.name);
            holder.comment.setText(item.comment);
            holder.time.setText(TimeUtil.formatTime(getContext(), item.date));
            holder.support.setText(String.valueOf(item.support));
            holder.against.setText(String.valueOf(item.against));
            return convertView;
        }

    }

    static class CommentViewHolder {
        TextView name;
        TextView time;
        TextView comment;
        Button support;
        Button against;
    }

    static class ActionTask extends AsyncTask<Void, Void, String> {
        private WeakReference<PageCommentsFragment> mFragment;
        static final int TYPE_SUPPORT = 0;
        static final int TYPE_AGAINST = 1;
        static final int TYPE_REPORT = 2;
        int mType;
        long mTid;
        ActionTask(PageCommentsFragment f, int type, long tid) {
            mFragment = new WeakReference<PageCommentsFragment>(f);
            mType = type;
            mTid = tid;
        }
        @Override
        protected String doInBackground(Void... params) {
            String result = null;
            switch (mType) {
                case TYPE_SUPPORT:
                    result = HttpUtil.getInstance().httpGet(Configs.SUPPORT_URL + mTid);
                    break;
                case TYPE_AGAINST:
                    result = HttpUtil.getInstance().httpGet(Configs.AGGAINST_URL + mTid);
                    break;
                case TYPE_REPORT:
                    result = HttpUtil.getInstance().httpGet(Configs.REPORT_URL + mTid);
                    break;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            PageCommentsFragment f = mFragment.get();
            if (f == null || !f.isResumed()) {
                return;
            }
            Activity a = f.getActivity();
            if (a == null || a.isFinishing()) {
                return;
            }
            if (!TextUtils.isEmpty(result)) {
                switch (mType) {
                    case TYPE_SUPPORT:
                        if (result.substring(0, 1).equals("0")) {
                            f.support(mTid);
                            Toast.makeText(a, R.string.vote_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(a, R.string.vote_fail, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case TYPE_AGAINST:
                        if (result.substring(0, 1).equals("0")) {
                            f.aggainst(mTid);
                            Toast.makeText(a, R.string.vote_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(a, R.string.vote_fail, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case TYPE_REPORT:
                        if (result.substring(0, 1).equals("0")) {
                            Toast.makeText(a, R.string.report_success, Toast.LENGTH_SHORT).show();
                        } else if (result.substring(0, 1).equals("1")) {
                            Toast.makeText(a, R.string.report_fail_1, Toast.LENGTH_SHORT).show();
                        } else if (result.substring(0, 1).equals("2")) {
                            Toast.makeText(a, R.string.report_fail_2, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(a, R.string.report_fail_3, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

    private static class LoadTask extends AsyncTask<Long, Void, List<CBComment>> {
        private WeakReference<PageCommentsFragment> mFragment;

        public LoadTask(PageCommentsFragment f) {
            mFragment = new WeakReference<PageCommentsFragment>(f);
        }

        @Override
        protected List<CBComment> doInBackground(Long... params) {
            long articleId = params[0];
            return DataUtil.readComments(articleId);
        }

        @Override
        protected void onPostExecute(List<CBComment> result) {
            PageCommentsFragment f = mFragment.get();
            if (f != null && f.isResumed()) {
                if (result != null) {
                    f.mData.clear();
                    f.mData.addAll(result);
                }
                f.mAdapter.notifyDataSetChanged();
                f.mIsLoading = false;
                f.setListShown(true);
            }
        }
    }
}

package cheng.app.cnbeta;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import cheng.app.cnbeta.util.DataUtil;
import cheng.app.cnbeta.util.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class PageCommentsFragment extends ListFragment {
    private List<CBComment> mData = new LinkedList<CBComment>();
    private CommentListAdapter mAdapter;
    private boolean mIsLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lv = getListView();
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
            if (holder == null) {
                holder = new CommentViewHolder();
                convertView.setTag(holder);
                holder.name = (TextView) convertView.findViewById(R.id.cmt_list_name);
                holder.time = (TextView) convertView.findViewById(R.id.cmt_list_time);
                holder.comment = (TextView) convertView.findViewById(R.id.cmt_list_text);
                holder.support = (Button) convertView.findViewById(R.id.cmt_list_support);
                holder.against = (Button) convertView.findViewById(R.id.cmt_list_against);
            }
            CBComment item = getItem(position);
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

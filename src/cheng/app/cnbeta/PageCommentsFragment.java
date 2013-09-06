package cheng.app.cnbeta;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import cheng.app.cnbeta.util.DataUtil;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

public class PageCommentsFragment extends ListFragment {
    private List<CBComment> mData = new LinkedList<CBComment>();
    private CommentListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getString(R.string.cmt_empty_text));
        mAdapter = new CommentListAdapter(getActivity(), mData);
        getListView().setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    public void LoadData(long articleId) {
        if (mData.isEmpty()) {
            setListShown(false);
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
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.cmt_list_item, null);
            }
            TextView text = (TextView) convertView.findViewById(R.id.cmt_list_text);
            CBComment item = getItem(position);
            text.setText(item.comment);
            return convertView;
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
                f.setListShown(true);
            }
        }
    }
}

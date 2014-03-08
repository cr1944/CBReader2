package cheng.app.cnbeta;

import com.squareup.picasso.Picasso;

import cheng.app.cnbeta.data.CBContract.HmColumns;
import cheng.app.cnbeta.data.CBContract.NewsColumns;
import cheng.app.cnbeta.util.HttpUtil;
import cheng.app.cnbeta.util.TimeUtil;
import cheng.app.cnbeta.util.Utils;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PageListAdapter extends CursorAdapter {
    private int mPageId;
    private static int mTextSize = Integer.parseInt(Utils.PREFERENCE_FONT_SIZE_DEFAULT);

    public PageListAdapter(Context context, int pageId) {
        super(context, null, 0);
        mPageId = pageId;
    }

    public void setFontSize(int fontSize) {
        if (mTextSize != fontSize) {
            mTextSize = fontSize;
            notifyDataSetChanged();
        }
    }

    @Override
    public void bindView(View arg0, Context arg1, Cursor arg2) {
        if (mPageId == PageListFragment.PAGE_HM)
            bindHMView(arg0, arg1, arg2);
        else
            bindNewsListView(arg0, arg1, arg2);
    }

    private void bindHMView(View arg0, Context arg1, Cursor arg2) {
        final String hm = arg2.getString(arg2.getColumnIndex(HmColumns.COMMENT));
        //final String name = arg2.getString(arg2.getColumnIndex(HmColumns.NAME));
        final String title = arg2.getString(arg2.getColumnIndex(HmColumns.TITLE));
        final TextView hmView = (TextView) arg0.findViewById(R.id.page_list_hm);
        final TextView titleView = (TextView) arg0.findViewById(R.id.page_list_hm_title);
        hmView.setText(hm);
        hmView.setTextSize(mTextSize);
        titleView.setText(title);
    }

    private void bindNewsListView(View arg0, Context arg1, Cursor arg2) {
        NewsViewHolder holder = null;
        Object tag = arg0.getTag();
        if (tag instanceof NewsViewHolder) {
            holder = (NewsViewHolder) tag;
        }
        if (holder == null) {
            holder = new NewsViewHolder();
            arg0.setTag(holder);
            holder.title = (TextView) arg0.findViewById(R.id.page_list_title);
            holder.time = (TextView) arg0.findViewById(R.id.page_list_time);
            holder.comment = (TextView) arg0.findViewById(R.id.page_list_comment);
            holder.text = (TextView) arg0.findViewById(R.id.page_list_text);
            holder.icon = (ImageView) arg0.findViewById(R.id.page_list_image);
        }
        final String title = arg2.getString(arg2.getColumnIndex(NewsColumns.TITLE));
        final String time = arg2.getString(arg2.getColumnIndex(NewsColumns.PUBTIME));
        final int cmtClosed = arg2.getInt(arg2.getColumnIndex(NewsColumns.CMT_CLOSED));
        final int cmtNumber = arg2.getInt(arg2.getColumnIndex(NewsColumns.CMT_NUMBER));
        final String logo = arg2.getString(arg2.getColumnIndex(NewsColumns.TOPIC_LOGO));
        final String text = HttpUtil.unescape(arg2.getString(arg2.getColumnIndex(NewsColumns.SUMMARY)));
        holder.title.setText(title);
        holder.time.setText(TimeUtil.formatTime(mContext, time));
        if (cmtClosed == 0) {
            holder.comment.setText(mContext.getString(R.string.cmt_number, cmtNumber));
        } else {
            holder.comment.setText(R.string.cmt_closed);
        }
        holder.text.setText(Html.fromHtml(text));
        holder.text.setTextSize(mTextSize);
        Picasso.with(mContext).load(logo).fit().placeholder(R.drawable.ic_launcher)
            .into(holder.icon);
    }

    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        if (mPageId == PageListFragment.PAGE_HM) {
            return LayoutInflater.from(arg0).inflate(R.layout.page_list_item_hm, arg2, false);
        } else {
            return LayoutInflater.from(arg0).inflate(R.layout.page_list_item, arg2, false);
        }
    }

    static class NewsViewHolder {
        TextView title;
        TextView time;
        TextView comment;
        TextView text;
        ImageView icon;
    }
}

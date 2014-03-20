package com.mjpeg.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

import com.mjpeg.activity.R;
import com.mjpeg.activity.R.dimen;
import com.mjpeg.activity.R.id;
import com.mjpeg.activity.R.layout;

public class PopMenu {
    private ArrayList<String> itemList;
    private Context context;
    private PopupWindow popupWindow ;
    private ListView listView;

    public PopMenu(Context context) {
        this.context = context;

        itemList = new ArrayList<String>();

        View view = LayoutInflater.from(context).inflate(R.layout.popmenu, null);

        listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(new PopAdapter());

        popupWindow = new PopupWindow(view,
                context.getResources().getDimensionPixelSize(R.dimen.popmenu_width),
                LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        listView.setOnItemClickListener(listener);
    }

    public void addItems(String[] items) {
        for (String s : items)
            itemList.add(s);
    }

    public void addItem(String item) {
        itemList.add(item);
    }

    public void showAsDropDown(View parent) {
        popupWindow.showAsDropDown(parent, 0, 0);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.update();
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    private final class PopAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.pomenu_item, null);
                holder = new ViewHolder();

                convertView.setTag(holder);

                holder.groupItem = (TextView) convertView.findViewById(R.id.textView);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.groupItem.setText(itemList.get(position));

            return convertView;
        }

        private final class ViewHolder {
            TextView groupItem;
        }
    }
}
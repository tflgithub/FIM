package com.cn.tfl.fim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cn.tfl.fim.R;
import com.cn.tfl.fim.model.MainPageItem;

import java.util.List;

/**
 * Created by Administrator on 2016/5/20.
 */
public class MainPageAdapter extends BaseAdapter {
    private Context context;

    private List<MainPageItem> list;
    private LayoutInflater
            mInflater;

    public MainPageAdapter(Context c) {
        super();
        this.context = c;
    }

    public void setList(List<MainPageItem> list) {
        this.list = list;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int index) {

        return list.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        convertView = mInflater.inflate(R.layout.main_page_item, null);
        ImageView appImage = (ImageView) convertView
                .findViewById(R.id.itemImage);
        TextView appName = (TextView) convertView.findViewById(R.id.itemText);
        MainPageItem info = list.get(index);
        if (info != null) {
            appName.setText(info.getName());
            appImage.setImageResource(info.getImage());
        }
        return convertView;
    }
}

package com.lee.woosuk.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lee.woosuk.DTOs.ChatDTO;
import com.lee.woosuk.R;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    private ArrayList<ChatDTO> listViewItemList = new ArrayList<ChatDTO>();

    public ChatAdapter() {

    }

    // 어뎁터에서 사용되는 데이터 개수 리턴
    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView icon = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView userName = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView message = (TextView) convertView.findViewById(R.id.textView2) ;
        TextView time = (TextView) convertView.findViewById(R.id.textView3);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ChatDTO listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        icon.setImageDrawable(listViewItem.getIcon());
        userName.setText(listViewItem.getUserName());
        message.setText(listViewItem.getMessage());
        time.setText(listViewItem.getTime());


        return convertView;
    }
    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    public void addItem(Drawable icon, String userName, String message, String time) {
        ChatDTO item = new ChatDTO();

        item.setIcon(icon);
        item.setUserName(userName);
        item.setMessage(message);
        item.setTime(time);

        listViewItemList.add(item);
    }
}

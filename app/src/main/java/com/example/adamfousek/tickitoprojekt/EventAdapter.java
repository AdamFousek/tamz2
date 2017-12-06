package com.example.adamfousek.tickitoprojekt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by adamfousek on 28.11.17.
 */

public class EventAdapter extends ArrayAdapter<Event> {

    Context context;
    int layoutResourceId;
    List<Event> data = null;

    public EventAdapter(Context context, int layoutResourceId, List<Event> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        EntryHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new EntryHolder();
            holder.txtEventName = (TextView)row.findViewById(R.id.txtEventName);

            row.setTag(holder);
        }
        else
        {
            holder = (EntryHolder)row.getTag();
        }

        Event entry = data.get(position);
        holder.txtEventName.setText(entry.getName() );

        return row;
    }

    static class EntryHolder
    {
        TextView txtEventName;
    }

}

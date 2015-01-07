package com.ranger.szybkamapa.app.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ranger.szybkamapa.app.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michal on 2014-12-07.
 */
public class MapListAdapter extends ArrayAdapter<Map> {

    private Context mContext;
    ArrayList<Map> maps;

    public MapListAdapter(Context context, int single_row, ArrayList<Map> maps)
    {
        super(context, single_row, maps);
        mContext=context;
        this.maps=maps;

        Log.v("MAPS","size " + this.maps.size());
    }

    @Override
    public View getView(int position,  View view, ViewGroup parent)
    {
        // inflate the layout for each item of listView
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.single_row, null);

        String nazwa = maps.get(position).name;
        Log.v("LIST ADAPTER ITERATE","o:" + position + " / " + nazwa);

        // get the reference of textViews
        TextView textViewNazwa = (TextView)view.findViewById(R.id.nazwa);
        TextView textViewPozycja = (TextView)view.findViewById(R.id.zakres);

        // Set the Sender number and smsBody to respective TextViews
        textViewNazwa.setText(nazwa);
        textViewPozycja.setText("brak danych");

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return maps.size();
    }

    public Map getItem(int position) {
        return maps.get(position);
    }

    public long getItemId(int position) {
        //maps.get(position);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }
}



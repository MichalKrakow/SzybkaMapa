package com.ranger.szybkamapa.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ranger.szybkamapa.app.Map.Map;
import com.ranger.szybkamapa.app.Map.MapListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapSelectActivity extends Activity {

    private String lat,lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_select);

        Intent intent = getIntent();
        lat = intent.getStringExtra(MainActivity.SEND_LAT);
        lon = intent.getStringExtra(MainActivity.SEND_LON);

        Log.v("LAT/LON", lat + " / " + lon);


        final ListView listview = (ListView) findViewById(R.id.listView);

        String path = Environment.getExternalStorageDirectory().toString()+"/mapy";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();

        final ArrayList<Map> maps = new ArrayList<Map>();

        for (int i=0; i < file.length; i++)
        {
            if (file[i].isFile() && file[i].getName().endsWith(".map")) {
                Log.d("Files", "FileName:" + file[i].getName());
                maps.add(new Map(file[i]));
            }
        }

        //final StableArrayAdapter adapter = new StableArrayAdapter(this, R.layout.single_row, maps);
        final MapListAdapter adapter = new MapListAdapter(this, R.layout.single_row, maps);

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("filename",maps.get(position).name);
                setResult(RESULT_OK, intent);
                finish();
                //Toast.makeText(getApplicationContext(), "Click ListItem Number " + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

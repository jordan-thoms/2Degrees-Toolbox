/*******************************************************************************
 * Copyright (c) 2011 Jordan Thoms.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package biz.shadowservices.DegreesToolbox.Preferences;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListActivity;

import de.quist.app.errorreporter.ReportingListActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.data.Values;
import biz.shadowservices.DegreesToolbox.widgets.AbstractWidgetUpdater;
import biz.shadowservices.DegreesToolbox.widgets.WidgetInstance;

public class WidgetListActivity extends SherlockListActivity {
	private ArrayAdapter<WidgetInstance> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        List<WidgetInstance> widgets = new ArrayList<WidgetInstance>();
		for (AbstractWidgetUpdater updater : Values.widgetUpdaters) {
			widgets.addAll(updater.getWidgets(this));
		}
		arrayAdapter = new ArrayAdapter<WidgetInstance>(this, R.layout.list_item, widgets);
        setListAdapter(arrayAdapter);
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	WidgetInstance widget = arrayAdapter.getItem(position);
            	Intent openWidgetPreferences = new Intent(view.getContext(), WidgetPreferencesActivity.class);
            	openWidgetPreferences.putExtra("widget", widget);
            	startActivity(openWidgetPreferences);
            }
          });

    }
}

package biz.shadowservices.DegreesToolbox.Preferences;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.UpdateWidgetService;
import biz.shadowservices.DegreesToolbox.Values;
import biz.shadowservices.DegreesToolbox.WidgetInstance;
import de.quist.app.errorreporter.ReportingActivity;

public class WidgetPreferencesActivity extends ReportingActivity {
	private static String TAG = "2DegreesPreferencesActivity";
	private WidgetInstance widget;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_preferences);

        Button save = (Button) findViewById(R.id.widgetPrefsSave);
        save.setOnClickListener(saveListener);
        
        Button cancel = (Button) findViewById(R.id.widgetPrefsCancel);
        cancel.setOnClickListener(cancelListener);

        SeekBar s = (SeekBar) findViewById(R.id.transparencySeekBar);
        s.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		TextView transparencyTextView = (TextView) ((View) seekBar.getParent()).findViewById(R.id.transparencyPercentageText);
        		transparencyTextView.setText(Integer.toString(progress));
        	}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        
        Gallery g = (Gallery) findViewById(R.id.gallery);
        g.setAdapter(new ImageAdapter(this));

        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(WidgetPreferencesActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                View parentObj = (View) parent.getParent();
                if (parent != null) {
                	ImageView preview = (ImageView) parentObj.findViewById(R.id.previewImage);
               		preview.setImageResource(Values.backgroundIds[position]);
                }
            }
        });
        
        Button editTextColour = (Button) findViewById(R.id.widgetPreferencesSetTextColor);
       
        editTextColour.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(v.getContext(), widget.getTextColor(v.getContext()), onColorSelected );
				dialog.show();				
			}
        });
	}
    @Override
	public void onResume() {
    	super.onResume();
		widget = (WidgetInstance) getIntent().getSerializableExtra("widget");
		TextView widgetName = (TextView) findViewById(R.id.WidgetName);
		widgetName.setText(widget.toString());
		
        Gallery g = (Gallery) findViewById(R.id.gallery);
        Log.d(TAG, "background:" + widget.getSelectedBackgroundId(this));
        int background = widget.getSelectedBackgroundId(this);
        g.setSelection(background);
    	ImageView preview = (ImageView) findViewById(R.id.previewImage);
   		preview.setImageResource(Values.backgroundIds[background]);
        
        SeekBar s = (SeekBar) findViewById(R.id.transparencySeekBar);
        s.setProgress(widget.getTransparency(this));
	}
    
    @Override
    public void onPause() {
    	super.onPause();
    	saveChanges();
    }
    
    private void saveChanges() {
        Gallery g = (Gallery) findViewById(R.id.gallery);
		widget.setSelectedBackgroundId(this, g.getSelectedItemPosition());
		
        SeekBar s = (SeekBar) findViewById(R.id.transparencySeekBar);
        widget.setTransparency(this, s.getProgress());
        
		Log.d(TAG, "Started from preferences activity.");
		startService(new Intent(this, UpdateWidgetService.class));
    }
    private OnAmbilWarnaListener onColorSelected = new  OnAmbilWarnaListener() {
        @Override
        public void onOk(AmbilWarnaDialog dialog, int color) {
        	widget.setTextColor(WidgetPreferencesActivity.this, color);
        }
        
        @Override
        public void onCancel(AmbilWarnaDialog dialog) {
                // cancel was selected by the user
        }
    };
    private OnClickListener saveListener = new OnClickListener() {
    	public void onClick(View v) {
    		saveChanges();
            finish();
    	}
    };
    private OnClickListener cancelListener = new OnClickListener() {
    	public void onClick(View v) {
    		finish();
    	}
    };

}
class ImageAdapter extends BaseAdapter {
    int mGalleryItemBackground;
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
        /* TypedArray a = obtainStyledAttributes(R.styleable.HelloGallery);
        mGalleryItemBackground = a.getResourceId(
                R.styleable.HelloGallery_android_galleryItemBackground, 0); 
                a.recycle();*/
    }

    public int getCount() {
        return Values.backgroundIds.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView i = new ImageView(mContext);

        i.setImageResource(Values.backgroundIds[position]);
        i.setLayoutParams(new Gallery.LayoutParams(150, 100));
        i.setScaleType(ImageView.ScaleType.FIT_XY);
        i.setBackgroundResource(mGalleryItemBackground);

        return i;
    }
}

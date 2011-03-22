package biz.shadowservices.DegreesToolbox.Preferences;

import biz.shadowservices.DegreesToolbox.R;
import biz.shadowservices.DegreesToolbox.Values;
import biz.shadowservices.DegreesToolbox.WidgetInstance;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
import android.widget.Toast;

public class WidgetPreferencesActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_preferences);
        
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
                Toast.makeText(WidgetPreferencesActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                View parentObj = (View) parent.getParent();
                if (parent != null) {
                	ImageView preview = (ImageView) parentObj.findViewById(R.id.previewImage);
               		preview.setImageResource(Values.backgroundIds[position]);
                }
            }
        });

	}
    @Override
	public void onResume() {
    	super.onResume();
		WidgetInstance widget = (WidgetInstance) getIntent().getSerializableExtra("widget");
		TextView widgetName = (TextView) findViewById(R.id.WidgetName);
		widgetName.setText(widget.toString());
	}
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

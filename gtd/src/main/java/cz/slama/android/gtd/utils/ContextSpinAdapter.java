package cz.slama.android.gtd.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.slama.android.gtd.model.ContextGtd;

/**
 * Created by Drugnanov on 29.3.2015.
 */

public class ContextSpinAdapter extends ArrayAdapter<ContextGtd> {

  // Your sent context
  private Context context;
  // Your custom values for the spinner (User)
  private ContextGtd[] values;

  public ContextSpinAdapter(Context context, int textViewResourceId,
                            ContextGtd[] values) {
    super(context, textViewResourceId, values);
    this.context = context;
    this.values = values;
  }

  public int getCount() {
    return values.length;
  }

  public ContextGtd getItem(int position) {
    return values[position];
  }

  public long getItemId(int position) {
    return position;
  }


  // And the "magic" goes here
  // This is for the "passive" state of the spinner
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    TextView label = new TextView(context);
    label.setTextColor(Color.BLACK);
    label.setText(values[position].getTitle());
    return label;
  }

  // And here is when the "chooser" is popped up
  // Normally is the same view, but you can customize it if you want
  @Override
  public View getDropDownView(int position, View convertView,
                              ViewGroup parent) {
    TextView label = new TextView(context);
    label.setTextColor(Color.BLACK);
    label.setText(values[position].getTitle());
    return label;
  }
}

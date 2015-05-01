package cz.slama.android.gtd.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.slama.android.gtd.model.State;

/**
 * Created by Drugnanov on 29.3.2015.
 */

public class StateSpinAdapter extends ArrayAdapter<State> {

  // Your sent context
  private Context context;
  // Your custom values for the spinner (User)
  private State[] values;

  public StateSpinAdapter(Context context, int textViewResourceId,
                          State[] values) {
    super(context, textViewResourceId, values);
    this.context = context;
    this.values = values;
  }

  public int getCount() {
    return values.length;
  }

  public State getItem(int position) {
    return values[position];
  }

  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    TextView label = new TextView(context);
    label.setTextColor(Color.BLACK);
    label.setText(values[position].getTitle());
    return label;
  }

  @Override
  public View getDropDownView(int position, View convertView,
                              ViewGroup parent) {
    TextView label = new TextView(context);
    label.setTextColor(Color.BLACK);
    label.setText(values[position].getTitle());
    return label;
  }
}

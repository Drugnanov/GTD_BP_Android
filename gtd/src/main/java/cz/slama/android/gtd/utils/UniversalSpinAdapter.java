package cz.slama.android.gtd.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;

/**
 * Created by Drugnanov on 29.3.2015.
 */

public class UniversalSpinAdapter<T extends IObjectTitle> extends ArrayAdapter<T> {

  // Your sent context
  private Context context;
  // Your custom values for the spinner (User)
  private T[] values;

  public UniversalSpinAdapter(Context context, int textViewResourceId,
                              T[] values) {
    super(context, textViewResourceId, values);
    this.context = context;
    this.values = values;
  }

  public int getCount() {
    return values.length;
  }

  public T getItem(int position) {
    return values[position];
  }

  public long getItemId(int position) {
    return position;
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );;
    View row = inflater.inflate(R.layout.simple_spinner_item, parent,
        false);
    TextView make = (TextView) row.findViewById(R.id.text1);
    make.setText(values[position].getTitle());
    return row;
  }


  public View getDropDownView(int position, View convertView, ViewGroup parent) {

    LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );;;
    View row = inflater.inflate(R.layout.simple_spinner_item, parent,
        false);
    TextView make = (TextView) row.findViewById(R.id.text1);
    make.setText(values[position].getTitle());
    return row;
  }

//  @Override
//  public View getDropDownView(int position, View view, ViewGroup parent) {
//    if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
//      view = getLayoutInflater().inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
//      view.setTag("DROPDOWN");
//    }
//
//    TextView textView = (TextView) view.findViewById(android.R.id.text1);
//    textView.setText(getTitle(position));
//
//    return view;
//  }
//
//  @Override
//  public View getView(int position, View view, ViewGroup parent) {
//    if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
//      view = getLayoutInflater().inflate(R.layout.
//          toolbar_spinner_item_actionbar, parent, false);
//      view.setTag("NON_DROPDOWN");
//    }
//    TextView textView = (TextView) view.findViewById(android.R.id.text1);
//    textView.setText(getTitle(position));
//    return view;
//  }
//
//  @Override
//     public View getView(int position, View convertView, ViewGroup parent) {
//    TextView label = new TextView(context);
//    label.setTextColor(Color.BLACK);
//    label.setText(values[position].getTitle());
//    return label;
//  }
//
//  @Override
//  public View getDropDownView(int position, View convertView,
//                              ViewGroup parent) {
//    TextView label = new TextView(context);
//    label.setTextColor(Color.BLACK);
//    label.setText(values[position].getTitle());
//    return label;
//  }
}

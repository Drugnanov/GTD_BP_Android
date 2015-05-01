package cz.slama.android.gtd.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.model.Project;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;

import java.util.List;

/**
 * Created by Drugnanov on 29.4.2015.
 */
public class StableArrayAdapter<T extends IObjectTitle> extends ArrayAdapter<T> {

  private final List<T> list;
  private final Activity activity;

  public StableArrayAdapter(Activity activity, int textViewResourceId,
                            List<T> objects) {
    super(activity, textViewResourceId, objects);
    this.list = objects;
    this.activity = activity;
  }

  static class ViewHolder {
    protected TextView text;
//    protected TextView descripton;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = null;
    if (convertView == null) {
      LayoutInflater inflator = activity.getLayoutInflater();
      view = inflator.inflate(R.layout.project_item, null);
      final ViewHolder viewHolder = new ViewHolder();
      viewHolder.text = (TextView) view.findViewById(R.id.tv_project_parent_title);
//      viewHolder.descripton = (TextView) view.findViewById(R.id.tv_project_parent_description);
      view.setTag(viewHolder);
    } else {
      view = convertView;
    }
    ViewHolder holder = (ViewHolder) view.getTag();
    holder.text.setText(list.get(position).getTitle());
//    holder.descripton.setText(list.get(position).getDescription());
    return view;
  }

}

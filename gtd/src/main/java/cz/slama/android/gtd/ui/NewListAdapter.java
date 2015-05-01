package cz.slama.android.gtd.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.ui.adapter.model.Group;

public class NewListAdapter extends BaseAdapter {

  Context contx;
  List<Group> objects;

  LayoutInflater layoutInflator;
  NewMultilevelTreeListView fragment;

  public NewListAdapter(Context context, int textViewResourceId, List<Group> groups,
                        NewMultilevelTreeListView fragment) {
    this.objects = groups;
    contx = context;
    layoutInflator = LayoutInflater.from(contx);
    this.fragment = fragment;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    try {
      Group CC = getItem(position);
      if (convertView == null) {
        convertView = View.inflate(contx, R.layout.row_cell_multilevel, null);
        holder = new ViewHolder();
        holder.txtName = (TextView) convertView.findViewById(R.id.row_cell_text_multilevel);
        holder.tvDummy = (TextView) convertView.findViewById(R.id.row_cell_text_dummy_multilevel);
        holder.btn = (Button) convertView.findViewById(R.id.row_cell_btn_multilevel);
        holder.linearLayout = (LinearLayout) convertView.findViewById(R.id.row_cell_text_id_layout);
        convertView.setTag(holder);
      }
      else {

      }
      holder = (ViewHolder) convertView.getTag();

      if (CC.getHasChild() > 0) {
        holder.btn.setVisibility(View.VISIBLE);
        if (!CC.isOpened) {
          holder.btn.setText("+");
        }
        else {
          holder.btn.setText("-");
        }
        holder.btn.setEnabled(true);
      }
      else {
        holder.btn.setVisibility(View.INVISIBLE);
        holder.btn.setText("");
        holder.btn.setEnabled(false);
      }

      holder.btn.setTag(position);
      holder.txtName.setTag(position);
      holder.txtName.setText(CC.getTitle());
      String str = "";
      int level = CC.level;
      for (int i = 0; i < level * 3; i++) {
        str += "-";
      }
      holder.tvDummy.setText("" + str);

      holder.txtName.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          fragment.cellTextClick(v);
        }
      });
      holder.btn.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          fragment.cellButtonClick(v);
        }
      });
      if (CC.isProject()) {
        holder.linearLayout
            .setBackgroundColor(contx.getResources().getColor(R.color.button_background_enabled_start));
        holder.tvDummy.setTextColor(contx.getResources().getColor(R.color.button_background_enabled_start));
      }
      else{
        holder.linearLayout
            .setBackgroundColor(contx.getResources().getColor(R.color.button_delete_background_enabled_end));
        holder.tvDummy.setTextColor(contx.getResources().getColor(R.color.button_delete_background_enabled_end));
      }
    }
    catch (Exception e) {
      Log.d("Exception", "" + e.getMessage());
    }
    return convertView;
  }

  static class ViewHolder {

    TextView txtName;
    Button btn;
    TextView tvDummy;
    LinearLayout linearLayout;
  }
  
  //@Override
  public int getCount() {
    return this.objects == null ? 0 : this.objects.size();
  }

  //@Override
  public Group getItem(int position) {
    return this.objects == null ? null : this.objects.get(position);
  }

  //		@Override
  public long getItemId(int position) {
    return 0;
  }
}

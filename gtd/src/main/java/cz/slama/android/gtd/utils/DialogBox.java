package cz.slama.android.gtd.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import cz.slama.android.gtd.R;
import cz.slama.android.gtd.exceptions.GtdException;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Drugnanov on 23.3.2015.
 */
public class DialogBox {

  private static boolean showed = false;

  public static void showDialog(String message, String windowName, Activity activity) {
    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(activity);
    dlgAlert.setMessage(message);
    dlgAlert.setTitle(windowName);
    dlgAlert.setPositiveButton("OK", null);
    dlgAlert.setCancelable(true);
    dlgAlert.create().show();
  }

  public static void showDateTimePicker(View v, Activity activity) {
    if (showed){
      return;
    }
    final EditText et = (EditText) v;
    final View dialogView = View.inflate(activity, R.layout.date_time_picker, null);
    final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.dtp_date_picker);
    TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.dtp_time_picker);

    String timeInString = et.getText().toString();
    if (!Compare.isNullOrEmpty(timeInString)) {
      try {
        Calendar calendar = Converter.getCalendarFromString(timeInString);
        datePicker
            .updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
      }
      catch (GtdException e) {
        System.out.println("error during date settings");
      }
    }

    dialogView.findViewById(R.id.dtp_date_time_set).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.dtp_date_picker);
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.dtp_time_picker);

        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
            datePicker.getMonth(),
            datePicker.getDayOfMonth(),
            timePicker.getCurrentHour(),
            timePicker.getCurrentMinute());
        et.setText(Converter.getStringFromCalendar(calendar));
        alertDialog.dismiss();
        showed = false;
      }
    });
    dialogView.findViewById(R.id.dtp_date_time_cancel).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
        showed = false;
      }
    });
    alertDialog.setView(dialogView);
    showed = true;
    alertDialog.show();
  }
}

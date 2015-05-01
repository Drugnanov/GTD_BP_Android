package cz.slama.android.gtd.model.util;

import cz.slama.android.gtd.model.Person;
import cz.slama.android.gtd.model.Task;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class PersonUtils {

  protected static Person person = null;

  protected static Timestamp actualDataTime;

  public PersonUtils() {
  }

  public static void setPerson(Person person) {
    PersonUtils.person = person;
    actualDataTime = new Timestamp(new Date().getTime());
  }

  public static Person getPerson() {
    return person;
  }

  public static boolean isNewData(Timestamp personLoaded) {
    if (actualDataTime == null || personLoaded == null){
      return true;
    }
    if (actualDataTime.after(personLoaded)){
      return true;
    }
    return false;
  }
}

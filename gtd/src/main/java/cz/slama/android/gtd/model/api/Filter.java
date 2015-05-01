package cz.slama.android.gtd.model.api;

import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.State;
import cz.slama.android.gtd.model.util.StateUtils;
import cz.slama.android.gtd.persistence.ShrPrefUtils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Drugnanov on 23.4.2015.
 */

public class Filter {

  List<StateUtils.EStates> statesToShow = new ArrayList<StateUtils.EStates>();

  private static Timestamp actualTimeStamp = null;

  public static Timestamp getActualTimeStamp() {
    return actualTimeStamp;
  }

  public Filter() {
    Filter filter = ShrPrefUtils.loadFilter(this);
    if (filter == null){
      setAllStates();
    }

    actualTimeStamp =  new Timestamp(new Date().getTime());
  }

  private void setAllStates() {
    for(StateUtils.EStates state : StateUtils.EStates.values()){
      statesToShow.add(state);
    }
  }

  public void saveFilter() {
    ShrPrefUtils.saveFilter(this);
    actualTimeStamp =  new Timestamp(new Date().getTime());
  }

  public void addState(StateUtils.EStates state) {
    statesToShow.add(state);
  }

  public void removeState(StateUtils.EStates state) {
    statesToShow.remove(state);
  }

  public List<StateUtils.EStates> getStatesToShow() {
    return statesToShow;
  }

  public void setStatesToShow(List<StateUtils.EStates> statesToShow) {
    this.statesToShow = statesToShow;
  }

  public boolean contains(StateUtils.EStates state) {
    if (statesToShow.contains(state)) {
      return true;
    }
    else {
      return false;
    }
  }

  public boolean contains(StateUtils.EStateTypes stateTypes, State state) {
    StateUtils.EStates stateEnum = null;
    try {
      stateEnum = StateUtils.getStateEnum(stateTypes, state);
    }
    catch (GtdException e) {
      //if we cannot recognize state show it by default
      return true;
    }
    if (stateEnum == null){
      return true;
    }
    if (statesToShow.contains(stateEnum)) {
      return true;
    }
    else {
      return false;
    }
  }

  public static boolean filterChanged(Timestamp objectReloaded) {
    if (objectReloaded == null || actualTimeStamp == null){
      return true;
    }
    return objectReloaded.before(actualTimeStamp);
  }
}

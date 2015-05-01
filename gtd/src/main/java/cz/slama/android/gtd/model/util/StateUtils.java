package cz.slama.android.gtd.model.util;

import cz.slama.android.gtd.R;
import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.State;
import cz.slama.android.gtd.utils.ContextClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Drugnanov on 28.3.2015.
 */
public class StateUtils extends ContextClass {



  public enum EStates {
    TASK_CREATED,
    TASK_ACTIVE,
    TASK_IN_CALENDAR,
    TASK_DONE,
    PROJECT_ACTIVE,
    PROJECT_DONE
  }

  public enum EStateTypes {
    TASK,
    PROJECT,
    PERSON
  }

  //  private static Map<EStates, State> stateMap = new HashMap<EStates, State>();
  private static Map<EStateTypes, Map<EStates, State>> stateTypeMap = new HashMap<EStateTypes, Map<EStates, State>>();

  static {
    CheckContext();
    Map<EStates, State> stateTaskMap = new HashMap<EStates, State>();
    stateTaskMap.put(EStates.TASK_CREATED, new State("V", _context.getString(R.string.task_state_created)));
    stateTaskMap.put(EStates.TASK_ACTIVE, new State("A", _context.getString(R.string.task_state_active)));
    stateTaskMap.put(EStates.TASK_IN_CALENDAR, new State("K", _context.getString(R.string.task_state_calendar)));
    stateTaskMap.put(EStates.TASK_DONE, new State("H", _context.getString(R.string.task_state_done)));
    stateTypeMap.put(EStateTypes.TASK, stateTaskMap);
    Map<EStates, State> stateProjectMap = new HashMap<EStates, State>();
    stateProjectMap.put(EStates.PROJECT_ACTIVE, new State("A", _context.getString(R.string.project_state_active)));
    stateProjectMap.put(EStates.PROJECT_DONE, new State("D", _context.getString(R.string.project_state_done)));
    stateTypeMap.put(EStateTypes.PROJECT, stateProjectMap);
  }

  public static State getState(EStateTypes type, EStates eStates) throws GtdException {
    Map<EStates, State> stateMap = getStatesByType(type);
    if (stateMap.containsKey(eStates)) {
      return stateMap.get(eStates);
    }
    throw new GtdException("Undefined type of state: " + eStates.toString());
  }

  public static Map<EStates, State> getStatesByType(EStateTypes type) throws GtdException {
    if (stateTypeMap.containsKey(type)) {
      return stateTypeMap.get(type);
    }
    throw new GtdException("Undefined type of state: " + type.toString());
  }

  public static List<State> getStatesArray(EStateTypes type) throws GtdException {
    Map<EStates, State> stateMap = getStatesByType(type);
    List<State> list = new ArrayList<State>(stateMap.values());
    return list;
//    return stateMap.values().toArray(new State[stateMap.size()]);
  }

  public static EStates getStateEnum(EStateTypes stateTypes, State state) throws GtdException {
    Map<EStates, State> statesMap = getStatesByType(stateTypes);
    for (Map.Entry<EStates, State> entry : statesMap.entrySet()) {
      State stateMap = entry.getValue();
      if (stateMap.getCode().equals(state.getCode())) {
        return entry.getKey();
      }
    }
    throw new GtdException("Undefined type of state: " + stateTypes.toString() + " and code " + state.getCode() + ".");
  }

  public static boolean isInCalendarState(State state) throws GtdException {
    if (StateUtils.getState(StateUtils.EStateTypes.TASK, StateUtils.EStates.TASK_IN_CALENDAR).equals(state)) {
      return true;
    }
    else {
      return false;
    }
  }

  //state hasnt empty value - check only for null value
  public static boolean isEmptyState(State stateSelected) {
    if (stateSelected == null) {
      return true;
    }
    else {
      return false;
    }
  }

  public static String getStateName(EStateTypes stateTypes, State state) {
    Map<EStates, State> statesMap = null;
    try {
      statesMap = getStatesByType(stateTypes);
    }
    catch (GtdException e) {
      return "";
    }
    for (Map.Entry<EStates, State> entry : statesMap.entrySet()) {
      State stateMap = entry.getValue();
      if (stateMap.getCode().equals(state.getCode())) {
        return stateMap.getTitle();
      }
    }
    return "";
  }
}

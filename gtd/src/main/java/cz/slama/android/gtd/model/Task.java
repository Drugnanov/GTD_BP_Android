
package cz.slama.android.gtd.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import cz.slama.android.gtd.exceptions.GtdException;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import cz.slama.android.gtd.model.util.StateUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Generated("org.jsonschema2pojo")
public class Task implements Serializable, IObjectTitle {

  private static final long serialVersionUID = 1L;

  @Expose
  private int id;
  @Expose
  private String title;
  @Expose
  private String description;
  @Expose
  private String owner;
  @Expose
  private Project project;
  @Expose
  private String creator;
  @Expose
  private Calendar calendar;
  @Expose
  private State state;
  @Expose
  private ContextGtd context;
  @Expose
  private List<Note> notes = new ArrayList<Note>();
  @Expose
  private long facebookPublishDate;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Project getProject() {
    return project;
  }

  public void setProject(Project project) {
    this.project = project;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public Calendar getCalendar() {
    return calendar;
  }

  public void setCalendar(Calendar calendar) {
    this.calendar = calendar;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public ContextGtd getContext() {
    return context;
  }

  public void setContext(ContextGtd contextGtd) {
    this.context = contextGtd;
  }

  public List<Note> getNotes() {
    return notes;
  }

  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  public long getFacebookPublishDate() {
    return facebookPublishDate;
  }

  public void setFacebookPublishDate(long facebookPublishDate) {
    this.facebookPublishDate = facebookPublishDate;
  }

  //  @Override
//  public String toString() {
//    return ToStringBuilder.reflectionToString(this);
//  }
//
//  @Override
//  public int hashCode() {
//    return HashCodeBuilder.reflectionHashCode(this);
//  }
//
//  @Override
//  public boolean equals(Object other) {
//    return EqualsBuilder.reflectionEquals(this, other);
//  }

  public Task() {
    try {
      setState(StateUtils.getState(StateUtils.EStateTypes.TASK, StateUtils.EStates.TASK_ACTIVE));
    }
    catch (GtdException e) {
      //create without state
    }
  }
}

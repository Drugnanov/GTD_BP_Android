
package cz.slama.android.gtd.model;

import com.google.gson.annotations.Expose;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class Project implements Serializable, IObjectTitle {

  @Expose
  private int id;
  @Expose
  private String title;
  @Expose
  private String description;
  @Expose
  private String owner;
  //Parent
  @Expose
  private Project project;
  @Expose
  private List<Note> notes = new ArrayList<Note>();
  @Expose
  private State state;

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

  public List<Note> getNotes() {
    return notes;
  }

  public void setNotes(List<Note> notes) {
    this.notes = notes;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
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

//    @Override
//    public boolean equals(Object other) {
//        return EqualsBuilder.reflectionEquals(this, other);
//    }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (!(o instanceof Project)) {
      return false;
    }

    Project project = (Project) o;

    if (id != project.id) return false;
    if (!title.equals(project.title)) return false;

    return true;
  }
}

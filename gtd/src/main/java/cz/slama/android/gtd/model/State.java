
package cz.slama.android.gtd.model;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import cz.slama.android.gtd.model.interfaces.IObjectTitle;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

@Generated("org.jsonschema2pojo")
public class State implements Serializable, IObjectTitle {

  @Expose
  private int id;
  @Expose
  private String code;
  @Expose
  private String title;
  @Expose
  private String description;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
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
//    @Override
//    public boolean equals(Object other) {
//        return EqualsBuilder.reflectionEquals(this, other);
//    }
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;

    if (!(obj instanceof State)) {
      return false;
    }

    final State other = (State) obj;
    if (!this.code.equals(other.code)) {
      return false;
    }
    return true;
  }

  public State(String code, String title) {
    this.code = code;
    this.title = title;
  }

  public State() {
  }
}

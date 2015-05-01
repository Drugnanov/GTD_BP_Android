package cz.slama.android.gtd.ui.adapter.model;

import cz.slama.android.gtd.model.interfaces.IObjectTitle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Drugnanov on 29.4.2015.
 */
public class Group {

  public IObjectTitle item;
  //  public List<Group> children = new ArrayList<Group>();
  public int HasChild;
  public int level;
  public boolean isOpened = false;
  public int index;
  public int indexParent = 0;
  public boolean project = false;
  public String prefix = "";

  public Group(IObjectTitle item, int hasChild, int level, int index, int indexParent, boolean isProject,
               String prefix) {
    this.item = item;
    this.HasChild = hasChild;
    this.level = level;
    this.indexParent = indexParent;
    this.index = index;
    this.project = isProject;
    this.prefix = prefix;
  }

  public IObjectTitle getItem() {
    return item;
  }

  public String getTitle() {
    return this.prefix + " " + getItem().getTitle();
  }

  public void setItem(IObjectTitle item) {
    this.item = item;
  }

  public int getHasChild() {
    return HasChild;
  }

  public void setHasChild(int hasChild) {
    HasChild = hasChild;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public boolean isOpened() {
    return isOpened;
  }

  public void setOpened(boolean isOpened) {
    this.isOpened = isOpened;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getIndexParent() {
    return indexParent;
  }

  public void setIndexParent(int indexParent) {
    this.indexParent = indexParent;
  }

  public boolean isProject() {
    return project;
  }

  public void setProject(boolean project) {
    this.project = project;
  }
}

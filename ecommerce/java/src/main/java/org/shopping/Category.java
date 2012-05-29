package org.shopping;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Category {
  private final String path;
  private final String root;
  private String name;

  public Category(String name, String path, String root) {
    this.name = name;
    this.path = path;
    this.root = root;
  }

  public DBObject toDbObject() {
    BasicDBObject object = new BasicDBObject("name", name);
    object.put("root", root);
    object.put("path", path);
    return object;
  }
}

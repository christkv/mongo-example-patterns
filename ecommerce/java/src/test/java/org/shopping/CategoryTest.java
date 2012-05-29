package org.shopping;

import com.mongodb.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;

public class CategoryTest {

  private Mongo mongo;
  private DB db;

  @Before
  public void setUp() throws UnknownHostException {
    mongo = new Mongo("localhost", 27017);
    mongo.dropDatabase("ecommerce");
    db = mongo.getDB("ecommerce");
  }

  @Test
  public void shouldInsertACategory() {
    Category category = new Category("tv", "/tv", "/");
    DBCollection collection = db.getCollection("categories");

    collection.insert(category.toDbObject(), WriteConcern.SAFE);
    DBObject object = collection.findOne(new BasicDBObject("name", "tv"));
    Assert.assertEquals("tv", object.get("name"));
  }

}

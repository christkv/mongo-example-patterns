package org.shopping;

import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class Product {
  private final String title;
  private final String description;
  private final int quantity;
  private final int cost;
  private String[] categories;
  private DB db;
  private DBCollection categoryCollection;
  private ObjectId id;

  public Product(DB db, ObjectId id, String title, String description, int quantity, int cost, String[] categories) {
    // Db reference
    this.db = db;
    // Collection references
    this.categoryCollection = db.getCollection("categories");

    // Set variables
    this.id = id;
    this.title = title;
    this.description = description;
    this.quantity = quantity;
    this.cost = cost;
    this.categories = categories;
  }

  public DBObject toDbObject() {
    return new BasicDBObjectBuilder()
            .add("_id", id)
            .add("title", title)
            .add("description", description)
            .add("quantity", quantity)
            .add("cost", cost)
            .add("categories", categories).get();
  }


  public static List<Product> productsByCategory(DB db, String category) {
    DBCollection productCollection = db.getCollection("products");
    List<Product> products = new ArrayList<Product>();

    DBCursor cursor = productCollection.find(new BasicDBObject("categories", category));

    while(cursor.hasNext()) {
      BasicDBObject object = (BasicDBObject)cursor.next();
      Product product = new Product(
              db,
              object.getObjectId("_id"),
              object.getString("title"),
              object.getString("description"),
              object.getInt("cost"),
              object.getInt("quantity"),
              ((BasicDBList)object.get("categories")).toArray(new String[0]));
      // Add the product
      products.add(product);
    }

    // Return the list
    return products;
  }

  public Category[] getCategories() {
    ArrayList<Category> categories = new ArrayList<Category>();

    DBCursor cursor = categoryCollection.find(new BasicDBObject("name"
            , new BasicDBObject("$in", this.categories)));

    while (cursor.hasNext()) {
      BasicDBObject object = (BasicDBObject) cursor.next();

      categories.add(new Category(
         object.getString("name"),
         object.getString("root"),
         object.getString("path")
      ));
    }

    return categories.toArray(new Category[0]);
  }

  public static Product findOne(DB db, BasicDBObject basicDBObject) {
    DBCollection collection = db.getCollection("products");
    DBObject dbObject = collection.findOne(basicDBObject);

    if(dbObject != null) {
      BasicDBObject object = (BasicDBObject) dbObject;
      return new Product(
          db,
          object.getObjectId("_id"),
          object.getString("title"),
          object.getString("description"),
          object.getInt("cost"),
          object.getInt("quantity"),
          ((BasicDBList)object.get("categories")).toArray(new String[0]));
    } else {
      return null;
    }
  }

  public String getTitle() {
    return title;
  }

  public int getCost() {
    return cost;
  }

  public ObjectId getId() {
    return id;
  }
}

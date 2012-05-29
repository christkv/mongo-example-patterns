package org.shopping;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;
import java.util.List;

public class ProductTest {
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
    String[] categories = {"tv"};
    DBCollection collection = db.getCollection("products");
    Product product = new Product(db, new ObjectId(), "samsung tv", "awesome tv", 5, 500, categories);

    collection.insert(product.toDbObject(), WriteConcern.SAFE);

    DBObject object = collection.findOne(new BasicDBObject("title", "samsung tv"));
    Assert.assertEquals("samsung tv", object.get("title"));
  }

  @Test
  public void shouldRetrieveAllProductsByCategory() {
    Category category = new Category("tv", "/tv", "/");
    DBCollection categoriesCollection = db.getCollection("categories");
    categoriesCollection.drop();
    categoriesCollection.insert(category.toDbObject(), WriteConcern.SAFE);

    String[] categories = {"tv"};
    DBCollection productCollection = db.getCollection("products");
    Product product = new Product(db, new ObjectId(), "samsung tv", "awesome tv", 5, 500, categories);
    productCollection.drop();

    productCollection.insert(product.toDbObject(), WriteConcern.SAFE);

    List<Product> products = Product.productsByCategory(db, "tv");
    Assert.assertEquals(1, products.size());
  }

  @Test
  public void shouldRetrieveAllCategoriesByProduct() {
    Category category = new Category("tv", "/tv", "/");
    DBCollection categoriesCollection = db.getCollection("categories");
    categoriesCollection.drop();
    categoriesCollection.insert(category.toDbObject(), WriteConcern.SAFE);

    String[] categories = {"tv"};
    DBCollection productCollection = db.getCollection("products");
    Product product = new Product(db, new ObjectId(), "samsung tv", "awesome tv", 5, 500, categories);
    productCollection.drop();

    productCollection.insert(product.toDbObject(), WriteConcern.SAFE);

    Category[] categorieses = product.getCategories();
    Assert.assertEquals(1, categories.length);
  }
}

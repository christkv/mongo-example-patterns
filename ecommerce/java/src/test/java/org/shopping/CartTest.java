package org.shopping;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.UnknownHostException;

public class CartTest {

  private Mongo mongo;
  private DB db;

  @Before
  public void setUp() throws UnknownHostException {
    mongo = new Mongo("localhost", 27017);
    mongo.dropDatabase("ecommerce");
    db = mongo.getDB("ecommerce");

    Category category = new Category("tv", "/tv", "/");
    DBCollection categoriesCollection = db.getCollection("categories");
    categoriesCollection.drop();
    categoriesCollection.insert(category.toDbObject(), WriteConcern.SAFE);

    String[] categories = {"tv"};
    DBCollection productCollection = db.getCollection("products");
    Product product = new Product(db, new ObjectId(), "samsung tv", "awesome tv", 5, 500, categories);
    productCollection.drop();
    productCollection.insert(product.toDbObject(), WriteConcern.SAFE);
  }

  @Test
  public void shouldAddAProductToCart() {
    Cart cart = new Cart(db, "somesession_id", "active", 0, null, null);

    DBCollection cartsCollection = db.getCollection("carts");
    cartsCollection.insert(cart.toDbObject(), WriteConcern.SAFE);

    Product product = Product.findOne(db, new BasicDBObject());
    Assert.assertTrue(cart.addProduct(product, 1));
    Assert.assertFalse(cart.addProduct(product, 100));
  }


  @Test
  public void shouldAddAndUpdateAProductToCart() {
    Cart cart = new Cart(db, "somesession_id", "active", 0, null, null);

    DBCollection cartsCollection = db.getCollection("carts");
    cartsCollection.insert(cart.toDbObject(), WriteConcern.SAFE);

    Product product = Product.findOne(db, new BasicDBObject());
    Assert.assertTrue(cart.addProduct(product, 1));

    Assert.assertTrue(cart.updateProduct(product, 1));
    Assert.assertFalse(cart.updateProduct(product, 100));
  }

}

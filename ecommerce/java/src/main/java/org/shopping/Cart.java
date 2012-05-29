package org.shopping;

import com.mongodb.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cart {
  private DB db;
  private final String sessionId;
  private final String status;
  private final double cost;
  private final Date modifiedOn;
  private final List<CartItem> products;
  private DBCollection cartCollection;
  private DBCollection productCollection;

  public Cart(DB db, String sessionId, String status, double cost, Date modifiedOn, List<CartItem> products) {
    this.db = db;
    this.sessionId = sessionId;
    this.status = status;
    this.cost = cost;
    this.modifiedOn = modifiedOn;
    this.products = products != null ? products : new ArrayList<CartItem>();
    this.cartCollection = db.getCollection("carts");
    this.productCollection = db.getCollection("products");
  }

  public DBObject toDbObject() {
    BasicDBObject object = new BasicDBObject("_id", sessionId);
    object.put("status", status);
    object.put("cost", cost);
    object.put("modified_on", modifiedOn);
    object.put("products", products);
    return object;
  }

  public boolean addProduct(Product product, int quantity) {
    // Add to cart
    BasicDBObject query = new BasicDBObject("_id", sessionId);
    query.put("status", "active");

    BasicDBObject document = new BasicDBObject("id", product.getId());
    document.put("quantity", quantity);
    document.put("cost", product.getCost());

    BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("modified_on", new Date()));
    update.put("$push", new BasicDBObject("products", document));

    WriteResult result = this.cartCollection.update(query, update, false, false, WriteConcern.SAFE);
    if(result.getLastError().getErrorMessage() != null) return false;

    // Add to product
    query = new BasicDBObject("title", product.getTitle());
    query.put("quantity", new BasicDBObject("$gte", quantity));

    Date timestamp = new Date();

    document = new BasicDBObject("id", this.sessionId);
    document.put("quantity", quantity);
    document.put("timestamp", timestamp);

    update = new BasicDBObject("$inc", new BasicDBObject("quantity", (-1)*quantity));
    update.put("$push", new BasicDBObject("in_carts", document));
    result = this.productCollection.update(query, update, false, false, WriteConcern.SAFE);

    // Rollback if error
    if(!result.getLastError().getBoolean("updatedExisting")) {
      DBObject modify = new BasicDBObject("products", new BasicDBObject("id", product.getId()));
      modify.put("products", new BasicDBObject("quantity", quantity));

      result = this.cartCollection.update(new BasicDBObject("_id", sessionId)
              , new BasicDBObject("$pull", modify), false, false, WriteConcern.SAFE);
      return false;
    } else {
      // Create a new cart item
      this.products.add(new CartItem(product.getId(), quantity, timestamp));
    }

    // Return true
    return true;
  }

  public boolean updateProduct(Product product, int deltaQuantity) {
    int oldValue = 0;
    int newValue = 0;

    // Add to cart
    BasicDBObject query = new BasicDBObject("_id", sessionId);
    query.put("status", "active");
    query.put("products.id", product.getId());

    BasicDBObject object = (BasicDBObject) this.cartCollection.findOne(query);
    BasicDBList products = (BasicDBList) object.get("products");

    for(int i = 0; i < products.size(); i++) {
      BasicDBObject dbObject = (BasicDBObject) products.get(i);

      if(dbObject.getString("id").equals(product.getId())) {
        oldValue = dbObject.getInt("quantity");
      }
    }

    newValue = oldValue + deltaQuantity;

    // Add to cart
    query = new BasicDBObject("_id", sessionId);
    query.put("status", "active");
    query.put("products.id", product.getId());

    BasicDBObject update = new BasicDBObject();
    update.put("$set", new BasicDBObject("products.$.quantity", newValue));

    WriteResult result = this.cartCollection.update(query, update, false, false, WriteConcern.SAFE);
    if(result.getLastError().getErrorMessage() != null) return false;

    // Add to product
    query = (BasicDBObject) new BasicDBObjectBuilder()
            .add("title", product.getTitle())
            .add("in_carts.id", this.sessionId)
            .add("quantity", new BasicDBObject("$gte", deltaQuantity))
            .get();

    update = new BasicDBObject("$inc", new BasicDBObject("quantity", (-1)*deltaQuantity));
    update.put("$set", new BasicDBObject("in_carts.$.quantity", newValue));
    result = this.productCollection.update(query, update, false, false, WriteConcern.SAFE);

    // Rollback if error
    if(!result.getLastError().getBoolean("updatedExisting")) {
      // Add to cart
      query = new BasicDBObject("_id", sessionId);
      query.put("status", "active");
      query.put("products.id", product.getId());

      update = new BasicDBObject();
      update.put("$set", new BasicDBObject("products.$.quantity", oldValue));

      result = this.cartCollection.update(query, update, false, false, WriteConcern.SAFE);
      return false;
    }

    return true;
  }

  public class CartItem {
    private final ObjectId id;
    private final int quantity;
    private final Date timestamp;

    public CartItem(ObjectId id, int quantity, Date timestamp) {
      this.id = id;
      this.quantity = quantity;
      this.timestamp = timestamp;
    }
  }
}

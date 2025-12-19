
package coursework;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * Domain model for a product.
 * - Tracks ID, name, quantity, and lastUpdated date.
 * - Maintains ONLY the last 4 activities via a small circular Queue<Activity>.
 * - Serializable so the whole app state can be saved/loaded.
 */

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productID;
    private String productName;
    private LocalDate lastUpdated;
    private int productQuantity;

    // last 4 activities
    private Queue<Activity> activities = new Queue<>(4);

    public Product(String productID, String productName, int productQuantity) {
        this.productID = productID;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.lastUpdated = LocalDate.now();
    }

    public String getProductID() { 
        return productID; 
    }
    
    public String getProductName() { 
        return productName; 
    }
    
    public LocalDate getEntryDate() { 
        return lastUpdated; 
    }
    public int getProductQuantity() { 
        return productQuantity; 
    }

    
    /**
     * Increase stock by q (q >= 0) and log an "AddToStock" activity.
     * Throws IllegalArgumentException if q < 0 (caught by UI/manager).
     */

    public void addQuantity(int q) {
        
        if (q < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.productQuantity += q;
        this.lastUpdated = LocalDate.now();
        activities.enqueue(new Activity("AddToStock", q));
    }

    
/**
     * Decrease stock by q, if enough in stock, and log "RemoveFromStock".
     * Returns false when q < 0 or not enough stock; true on success.
     */
    public boolean removeQuantity(int q) {
        if (q < 0) return false;
        if (q > productQuantity) return false;
        this.productQuantity -= q;
        this.lastUpdated = LocalDate.now();
        activities.enqueue(new Activity("RemoveFromStock", q));
        return true;
    }

    // still available if you want to manually add (rarely used now)
    public void addActivity(Activity activity) {
        activities.enqueue(activity);
    }

    
    /**
     * Return a typed array of the (up to) last 4 activities.
     * Using typed array avoids ClassCastException.
     */
    public Activity[] getActivitiesAsArray() {
        return activities.toArray(new Activity[0]); // typed array to avoid ClassCastException
    }

    @Override
    public String toString() {
        return "Product ID: " + productID +
               ", Name: " + productName +
               ", Entry Date: " + lastUpdated +
               ", Quantity: " + productQuantity;
       }
}
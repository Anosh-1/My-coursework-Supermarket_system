
package coursework;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single stock activity for a product.
 * <p>
 * Supported activity types:
 * - "AddToStock"     : items added to stock
 * - "RemoveFromStock": items removed from stock
 * - (optional) "AddProduct" when a product is initially added
 * <p>
 * IDs are auto-generated (A1, A2, A3, ... ) using a static counter.
 * Implements Serializable so activities can be persisted to disk.
 */
public class Activity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Auto-increment counter used to generate unique activity IDs. */
    private static int counter = 1;

    /** e.g., "A1" */
    private String activityID;

    /** e.g., "AddToStock" or "RemoveFromStock" */
    private String activityName;

    /** Amount added/removed in this activity (non-negative) */
    private int activityQuantity;

    /** Date the activity occurred (uses LocalDate.now()) */
    private LocalDate activityDate;

    /**
     * Creates an activity with the given name and quantity.
     * The ID is auto-generated and the date is set to today.
     *
     * @param activityName     operation label, e.g., "AddToStock"
     * @param activityQuantity number of items (>= 0)
     */
    public Activity(String activityName, int activityQuantity) {
        this.activityID = "A" + counter++; // Auto-incremented ID
        this.activityName = activityName;
        this.activityQuantity = activityQuantity;
        this.activityDate = LocalDate.now();
    }

    // --- Getters ---

    /** @return auto-generated activity ID (e.g., "A27") */
    public String getActivityID() { return activityID; }

    /** @return activity type/name ("AddToStock" / "RemoveFromStock") */
    public String getActivityName() { return activityName; }

    /** @return quantity involved in this activity (>= 0) */
    public int getActivityQuantity() { return activityQuantity; }

    /** @return date when the activity occurred */
    public LocalDate getActivityDate() { return activityDate; }

    /**
     * Used after loading persisted activities to continue IDs without collisions.
     * Example: if the highest saved ID is "A42", we set counter to 43.
     *
     * @param next next integer to use for auto-generated IDs
     */
    public static void setCounter(int next) { counter = next; }

    @Override
    public String toString() {
        return "Activity ID: " + activityID +
               ", Name: " + activityName +
               ", Quantity: " + activityQuantity +
               ", Date: " + activityDate;
    }
}

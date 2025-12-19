package coursework;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 * Simple manager service for products:
 *  Stores products in an ArrayList (JCF requirement).
 *  Linear search by ID (used for delete/update per coursework).
 *  Bubble sort (ascending) of last 4 activities by quantity.
 *  Save/Load the entire list via Java serialization (additional feature).
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class ProductManager {

    private ArrayList<Product> products = new ArrayList<>();

    // Add new product, RETURN FALSE ON DUPLICTE
    public boolean addProduct(Product p) {
        // prevent duplicate IDs
        if (searchProductIndex(p.getProductID()) != -1) {
            return false; 
        }
        products.add(p);
        p.addActivity(new Activity("AddProduct", p.getProductQuantity()));
        return true;
    }

    // Search product by ID (Linear Search)
    public int searchProductIndex(String productID) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductID().equals(productID)) {
                return i;
            }
        }
        return -1; // not found
    }

    public Product searchProduct(String productID) {
        int index = searchProductIndex(productID);
        if (index == -1) return null;
        return products.get(index);
    }

    // Delete product
    public boolean deleteProduct(String productID) {
        int index = searchProductIndex(productID);
        if (index == -1) return false;

        products.remove(index);
        return true;
    }

    // -------------------------------
    // AddToStock activity
    // -------------------------------
    public boolean addToStock(String productID, int quantity) {
        Product p = searchProduct(productID);
        if (p == null) return false;
        if (quantity < 0) return false;

        p.addQuantity(quantity);        // FIX: add only the requested quantity
        // remove: p.addActivity(new Activity("AddToStock", quantity));
        return true;
    }
    //Remove from stock
    public boolean removeFromStock(String productID, int quantity) {
        Product p = searchProduct(productID);
        if (p == null) return false;
        if (quantity < 0) return false;
        if (p.getProductQuantity() < quantity) return false;

        p.removeQuantity(quantity);     // Product logs "RemoveFromStock" internally
        // remove: p.addActivity(new Activity("RemoveFromStock", quantity));
        return true;
    }
    // Display all products
    public ArrayList<Product> getAllProducts() {
        return products;
    }

    // Get last 4 activities sorted by quantity
    public Activity[] getSortedActivities(String productID) {
        Product p = searchProduct(productID);
        if (p == null) return null;

        Activity[] arr = p.getActivitiesAsArray();

        // bubble sort based on quantity
        bubbleSort(arr);

        return arr;
    }
    //Search by ID or name
    public ArrayList<Product> searchByIdOrName(String query) {
        ArrayList<Product> res = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            res.addAll(products);
            return res;
        }
        String q = query.trim().toLowerCase();
        for (Product p : products) {
            if (p.getProductID().toLowerCase().contains(q) || p.getProductName().toLowerCase().contains(q)) {
                res.add(p);
            }
        }
        return res;
    }
    //Sort by name (A>Z)
    public void sortByNameAsc() {
        Collections.sort(products, Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER));
    }
    //Sort by ID
    public void sortByQuantityAsc() {
        Collections.sort(products, Comparator.comparingInt(Product::getProductQuantity));
    }

    // -------------------------------
    //Bubble sorting algorithm
    // -------------------------------
    private void bubbleSort(Activity[] arr) {
        int n = arr.length;

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {

                if (arr[j].getActivityQuantity() > arr[j + 1].getActivityQuantity()) {

                    Activity temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
    /** Save entire list (and nested activities) to a binary file. */
    public boolean saveToFile(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(products);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    
    /**
     * Load list from file; reseed Activity ID counter so new IDs remain unique.
     * Returns true on success, false if file missing or read failed.
     */

    @SuppressWarnings("unchecked")
    public boolean loadFromFile(String path) {
        File f = new File(path);
        if (!f.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList<?>) {
                this.products = (ArrayList<Product>) obj;

                // OPTIONAL: reseed Activity ID counter to avoid collisions across sessions
                int maxId = products.stream()
                        .flatMap(p -> java.util.Arrays.stream(p.getActivitiesAsArray()))
                        .mapToInt(a -> {
                            try { return Integer.parseInt(a.getActivityID().substring(1)); }
                            catch (Exception e) { return 0; }
                        })
                        .max().orElse(0);
                Activity.setCounter(maxId + 1);

                return true;
            }
            return false;
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return false;
        }
    }

}


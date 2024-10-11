package ru.treiden.Wishlist;

public class Gift {
    private int id;
    private String name;
    private String description;
    private Category category;

    public Gift(int id, String name, String description, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
}

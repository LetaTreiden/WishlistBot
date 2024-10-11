package ru.treiden.Wishlist;

public enum Category {
    SMALL("Мелочи"),
    ONE_K("В районе 1к"),
    EXPENSIVE("Дорого"),
    SUPER_EXPENSIVE("Капец дорого");

    private String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
package com.lucaslabs.achis.model;

/**
 * Model class with common fields for searching a Social Network by hashtag response.
 *
 * @author lucas.nobile
 */
public class Item {
    private String photoUrl;
    private String text;
    private String username;
    // TODO-LMN Add type: Instagram or Twitter

    public Item(String photoUrl, String text, String username) {
        this.photoUrl = photoUrl;
        this.text = text;
        this.username = username;
    }

    public static Item empty() {
        return new Item("photoUrl", "text", "username");
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getText() {
        return text;
    }

    public String getUsername() {
        return username;
    }
}

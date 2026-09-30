package com.example.integrationdemo.dao.entity;

public class FavoriteAuthor {
    private int authorOrder;
    private String author;

    public FavoriteAuthor() {}
    public FavoriteAuthor(int authorOrder, String author) {
        this.authorOrder = authorOrder;
        this.author = author;
    }
    public int getAuthorOrder() { return authorOrder; }
    public void setAuthorOrder(int authorOrder) { this.authorOrder = authorOrder; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}

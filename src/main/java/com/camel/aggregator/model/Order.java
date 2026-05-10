package com.camel.aggregator.model;

public class Order {
    private String id;
    private String customer;
    private double amount;
    private String status; // PENDING, BILLED, SHIPPED

    public Order() {}

    public Order(String id, String customer, double amount, String status) {
        this.id = id;
        this.customer = customer;
        this.amount = amount;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

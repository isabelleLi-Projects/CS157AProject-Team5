package com.studyfinder.model;

public class Reward {
    private int rewardId;
    private String name;
    private String description;
    private int pointCost;
    private int stockQuantity;

    public int getRewardId() { return rewardId; }
    public void setRewardId(int rewardId) { this.rewardId = rewardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPointCost() { return pointCost; }
    public void setPointCost(int pointCost) { this.pointCost = pointCost; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}

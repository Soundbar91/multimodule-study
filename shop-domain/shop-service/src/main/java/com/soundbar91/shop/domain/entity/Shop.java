package com.soundbar91.shop.domain.entity;

import com.soundbar91.shop.domain.vo.ShopCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 상점 엔티티
 */
@Entity
@Table(name = "shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShopCategory category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Shop() {
    }

    public Shop(String name, ShopCategory category, String description, String address, String phoneNumber, Long ownerId) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.ownerId = ownerId;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(String name, String description, String address, String phoneNumber) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public void updateCategory(ShopCategory category) {
        this.category = category;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShopCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

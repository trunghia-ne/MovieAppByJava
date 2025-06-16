package com.example.movieappbyjava.model;

import java.util.Date;

public class Payment {
    private Long id;
    private int amount;
    private boolean paid;
    private String paymentMethod;
    private Date paymentTime; // Dùng Date để Gson parse từ JSON dễ dàng
    private String userId;

    public Payment() {}

    public Payment(int amount, boolean paid, String paymentMethod, Date paymentTime, String userId) {
        this.amount = amount;
        this.paid = paid;
        this.paymentMethod = paymentMethod;
        this.paymentTime = paymentTime;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Date getPaymentTime() { return paymentTime; }
    public void setPaymentTime(Date paymentTime) { this.paymentTime = paymentTime; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

package com.example.movieappbyjava.model;

import com.google.gson.annotations.SerializedName;

public class PaymentUrlResponse {
    @SerializedName("paymentUrl")
    private String paymentUrl;

    @SerializedName("transactionId")
    private String transactionId;

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getTransactionId() {
        return transactionId;
    }
}

package com.mercado.mercadosegundamano.dto;

public class CheckoutForm {

    private String street;
    private String postalCode;
    private String city;
    private String province;
    private String cardNumber;
    private String expiry;
    private String cvv;

    public String getFullAddress() {
        return String.format("%s, %s, %s, %s", street, postalCode, city, province);
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}
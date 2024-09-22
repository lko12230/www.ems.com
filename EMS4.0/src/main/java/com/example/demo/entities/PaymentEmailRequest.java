package com.example.demo.entities;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class PaymentEmailRequest {
	private String invoice;
    private String message;
    private String subject;
    private String to;
    
    // Override equals() to compare message, subject, and to fields
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentEmailRequest that = (PaymentEmailRequest) o;
        return Objects.equals(invoice, that.invoice) &&
        	   Objects.equals(message, that.message) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(to, that.to);
    }

    // Override hashCode() to generate a hash code based on message, subject, and to fields
    @Override
    public int hashCode() {
        return Objects.hash(invoice, message, subject, to);
    }

}

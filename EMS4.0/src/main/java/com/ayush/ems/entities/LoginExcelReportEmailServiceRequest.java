package com.ayush.ems.entities;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LoginExcelReportEmailServiceRequest {
    private String invoice;
    private String message;
    private String subject;
    private String to; // changed from String to List<String>

    // Override equals() to compare all fields
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginExcelReportEmailServiceRequest that = (LoginExcelReportEmailServiceRequest) o;
        return Objects.equals(invoice, that.invoice) &&
               Objects.equals(message, that.message) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(to, that.to); // compare lists
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoice, message, subject, to);
    }
}

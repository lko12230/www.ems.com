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
public class EmailRequestCC {
    private String message;
    private String subject;
    private String to;
    private String cc;
    
    // Override equals() to compare message, subject, and to fields
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailRequestCC that = (EmailRequestCC) o;
        return Objects.equals(message, that.message) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(to, that.to) &&
               Objects.equals(cc, that.cc);
    }

    // Override hashCode() to generate a hash code based on message, subject, and to fields
    @Override
    public int hashCode() {
        return Objects.hash(message, subject, to, cc);
    }

}

package com.ayush.ems.entities;

import java.util.List;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class EmailRequestCCList {
    private String message;
    private String subject;
    private String to;
    private String cc;
    private List<String> emailList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailRequestCCList that = (EmailRequestCCList) o;
        return Objects.equals(message, that.message) &&
               Objects.equals(subject, that.subject) &&
               Objects.equals(to, that.to) &&
               Objects.equals(cc, that.cc) &&
               Objects.equals(emailList, that.emailList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, subject, to, cc, emailList);
    }
}

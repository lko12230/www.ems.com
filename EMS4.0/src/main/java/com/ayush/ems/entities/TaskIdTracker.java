package com.ayush.ems.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_id_tracker")
public class TaskIdTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SNO")
    private Integer sno;

    @Column(name = "LAST_TASK_ID", nullable = false)
    private Integer lastTaskId;

    @Column(name = "ADDDATE")
    private LocalDateTime addDate;

    @Column(name = "ADDWHO")
    private String addWho;

    @Column(name = "EDITDATE")
    private LocalDateTime editDate;

    @Column(name = "EDITWHO")
    private String editWho;

    public TaskIdTracker() {
    }

    public Integer getSno() {
        return sno;
    }

    public void setSno(Integer sno) {
        this.sno = sno;
    }

    public Integer getLastTaskId() {
        return lastTaskId;
    }

    public void setLastTaskId(Integer lastTaskId) {
        this.lastTaskId = lastTaskId;
    }

    public LocalDateTime getAddDate() {
        return addDate;
    }

    public void setAddDate(LocalDateTime addDate) {
        this.addDate = addDate;
    }

    public String getAddWho() {
        return addWho;
    }

    public void setAddWho(String addWho) {
        this.addWho = addWho;
    }

    public LocalDateTime getEditDate() {
        return editDate;
    }

    public void setEditDate(LocalDateTime editDate) {
        this.editDate = editDate;
    }

    public String getEditWho() {
        return editWho;
    }

    public void setEditWho(String editWho) {
        this.editWho = editWho;
    }
}
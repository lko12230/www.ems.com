package com.ayush.ems.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "team")
@Getter
@Setter
//@AllArgsConstructor
//@NoArgsConstructor
public class Team {
	@Id
	private int sno;
	private int id;
	private String team_id;
	private String team_description;
	private Date adddate;
	private String addwho;
	private Date editdate;
	private String editwho;
//@OneToMany(cascade = CascadeType.ALL,mappedBy = "team",orphanRemoval = true )
//private List<UserDetail> team_users_list =new ArrayList<>();
//@OneToOne
//private User user;
//@ManyToOne
//@JsonIgnore
//private User user;
}

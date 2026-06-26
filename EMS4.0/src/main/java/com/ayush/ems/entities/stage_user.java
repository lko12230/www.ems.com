package com.ayush.ems.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.*;

import org.hibernate.annotations.DynamicInsert;
import lombok.*;

@Entity
@Table(name = "STAGE_EMPLOYEE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
public class stage_user implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Column(name = "sno", nullable = true)
 	private Integer sno;
 	   @Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	private Integer id;

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters")
    private String username;

    private String state;

    @Column(unique = true)
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

//    @NotEmpty(message = "Password cannot be empty")
//    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;
//
//    @NotEmpty(message = "Repassword cannot be empty")
//    @Size(min = 4, message = "Repassword must be at least 4 characters")
    private String repassword;

    @Column(unique = true)
    @NotEmpty(message = "Phone Number cannot be empty")
    @Pattern(regexp = "\\d{10}", message = "Phone Number must be exactly 10 digits")
    private String phone;

    private String gender;

    @NotBlank(message = "Date of Birth cannot be empty")
    private String dob;

    private boolean enabled;

    @NotBlank(message = "Home Address is required")
    private String address;

    @NotEmpty(message = "Country cannot be empty")
    private String country;

    private String imageUrl;
    private Date editdate;
    private String editwho;
    private String status;
    private int alertMessageSent;
    private Date adddate;
    private String addwho;
    private String role;
    private String ipAddress;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    private boolean defaultPasswordSent;
    private int processFlag;
    private String designation;
    private String baseLocation;
    private boolean managerOrNot;
    private String company;
    private String companyId;
    private String errorMessage;
    private String location;
    private String addwhoAdminId;
    // Transient fields (not stored in DB)
    @Transient
    private String captcha;

    @Transient
    private String hidden;

    @Transient
    private String imageCaptcha;
}

package com.altester.auth.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "codes")
public class Codes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_code")
    @JsonIgnore
    private String verificationCode;

    @Column(name = "verification_expiration")
    @JsonIgnore
    private LocalDateTime verificationCodeExpiredAt;

    @Column(name = "password_reset_code")
    @JsonIgnore
    private String passwordResetCode;

    @Column(name = "password_reset_expiration")
    @JsonIgnore
    private LocalDateTime passwordResetCodeExpiredAt;

    @Column(name = "sms_code")
    @JsonIgnore
    private String smsCode;

    @Column(name = "sms_code_expiration")
    @JsonIgnore
    private LocalDateTime smsCodeExpiredAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sendAt")
    @JsonIgnore
    private LocalDateTime sendAt;
}

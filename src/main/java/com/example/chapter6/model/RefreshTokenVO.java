package com.example.chapter6.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RefreshTokenVO {

    private int id;
    private String refreshToken;
    private int refreshCount;
    private int memberId;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
    private Instant expiryDate;

    private String userNm;

}

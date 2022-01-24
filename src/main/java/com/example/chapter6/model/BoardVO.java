package com.example.chapter6.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class BoardVO{

    private int id = 0;
    private int code = 1000;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    private String regId;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
    private int count;

}

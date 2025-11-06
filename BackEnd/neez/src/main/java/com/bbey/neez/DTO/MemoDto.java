package com.bbey.neez.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoDto {
    private Long cardId;
    private String memoContent;
    private String memoFile;
}

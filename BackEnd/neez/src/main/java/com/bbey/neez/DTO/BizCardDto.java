package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "명함 정보 DTO")
public class BizCardDto {

    @Schema(description = "명함 고유 ID", example = "10")
    private Long idx;

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long userIdx;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "회사 이름", example = "네이버")
    private String companyName;

    @Schema(description = "부서명", example = "개발1팀")
    private String department;

    @Schema(description = "직급", example = "주임")
    private String position;

    @Schema(description = "이메일", example = "gildong@naver.com")
    private String email;

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "유선 전화번호", example = "02-987-6543")
    private String lineNumber;

    @Schema(description = "팩스 번호", example = "02-333-2222")
    private String faxNumber;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "메모 내용", example = "네이버 담당자, 다음 주 회의 예정")
    private String memoContent;

    @Schema(description = "해시태그 목록", example = "[\"회의\", \"네이버\", \"중요\"]")
    private List<String> hashTags;
}

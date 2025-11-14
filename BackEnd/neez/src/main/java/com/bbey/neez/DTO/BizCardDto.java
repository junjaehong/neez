package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "명함 정보 DTO (명함 원문 회사명 + 연결된 회사 ID 포함)")
public class BizCardDto {

    @Schema(description = "명함 고유 ID", example = "10")
    private Long idx;

    @Schema(description = "사용자 고유 ID", example = "1")
    private Long userIdx;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(
            description = "명함에 적힌 회사 이름(원문 그대로 저장된 값)",
            example = "삼성생명 대구금융SFP지점"
    )
    private String cardCompanyName;

    @Schema(
            description = "companies 테이블과 연결된 회사 IDX (없을 수도 있음)",
            example = "3",
            nullable = true
    )
    private Long companyIdx;

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

    @Schema(description = "주소", example = "대구광역시 중구 달구벌대로 2095 ...")
    private String address;

    @Schema(description = "메모 내용(파일에서 읽어온 내용)", example = "다음 주 화요일 미팅 예정")
    private String memoContent;

    @Schema(description = "해시태그 목록", example = "[\"중요\", \"삼성\", \"생명보험\"]")
    private List<String> hashTags;
}

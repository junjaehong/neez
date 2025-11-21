package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "명함 정보 DTO")
public class BizCardDto {

        @Schema(description = "명함 PK", example = "61")
        private Long idx;

        @Schema(description = "사용자 PK", example = "15")
        private Long userIdx;

        @Schema(description = "이름", example = "진형록")
        private String name;

        @Schema(description = "명함에 적힌 회사명", example = "(주)쿠로엔시스")
        private String cardCompanyName;

        @Schema(description = "companies 테이블 PK (없으면 null)", example = "13")
        private Long companyIdx;

        @Schema(description = "부서", example = "전략사업부")
        private String department;

        @Schema(description = "직책", example = "선임연구원")
        private String position;

        @Schema(description = "이메일", example = "hrjin@curonsys.com")
        private String email;

        @Schema(description = "휴대폰 번호", example = "010.7520.9944")
        private String phoneNumber;

        @Schema(description = "대표 번호", example = "070.5121.6825")
        private String lineNumber;

        @Schema(description = "팩스 번호", example = "061.337.6825")
        private String faxNumber;

        @Schema(description = "주소", example = "58217 전라남도 나주시 한빛로 262 스마트프라자 502호")
        private String address;

        @Schema(description = "메모 내용 (파일에서 로드된 텍스트)", example = "")
        private String memoContent;

        @Schema(description = "해시태그 목록", example = "[\"개발자\", \"클라이언트\"]")
        private List<String> hashTags;
}

package com.bbey.neez.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "명함 기본 정보 DTO")
public class BizCardDto {

        @Schema(description = "명함 PK", example = "61")
        private Long idx;

        @Schema(description = "사용자 PK (users.idx)", example = "15")
        private Long userIdx;

        @Schema(description = "이름", example = "진형록")
        private String name;

        @Schema(description = "명함에 적힌 회사명(원문)", example = "(주)쿠로엔시스")
        private String cardCompanyName;

        @Schema(description = "연결된 회사 PK (companies.idx)", example = "13")
        private Long companyIdx;

        @Schema(description = "부서", example = "전략사업부")
        private String department;

        @Schema(description = "직책", example = "선임연구원")
        private String position;

        @Schema(description = "이메일", example = "hrjin@curonsys.com")
        private String email;

        @Schema(description = "휴대폰 번호", example = "010.7520.9944")
        private String phoneNumber;

        @Schema(description = "유선 번호(회사 번호 등)", example = "070.5121.6825")
        private String lineNumber;

        @Schema(description = "팩스 번호", example = "061.337.6825")
        private String faxNumber;

        @Schema(description = "주소", example = "58217 전라남도 나주시 한빛로 262 스마트프라자 502호")
        private String address;

        @Schema(description = "메모 내용", example = "AI 프로젝트 미팅에서 만난 담당자")
        private String memoContent;

        @Schema(description = "해시태그 목록", example = "[\"AI\", \"스마트팜\", \"광주\"]")
        private List<String> hashTags;

        public BizCardDto() {
        }

        public BizCardDto(Long idx, Long userIdx, String name, String cardCompanyName, Long companyIdx,
                        String department, String position, String email,
                        String phoneNumber, String lineNumber, String faxNumber,
                        String address, String memoContent, List<String> hashTags) {
                this.idx = idx;
                this.userIdx = userIdx;
                this.name = name;
                this.cardCompanyName = cardCompanyName;
                this.companyIdx = companyIdx;
                this.department = department;
                this.position = position;
                this.email = email;
                this.phoneNumber = phoneNumber;
                this.lineNumber = lineNumber;
                this.faxNumber = faxNumber;
                this.address = address;
                this.memoContent = memoContent;
                this.hashTags = hashTags;
        }

        public Long getIdx() {
                return idx;
        }

        public Long getUserIdx() {
                return userIdx;
        }

        public String getName() {
                return name;
        }

        public String getCardCompanyName() {
                return cardCompanyName;
        }

        public Long getCompanyIdx() {
                return companyIdx;
        }

        public String getDepartment() {
                return department;
        }

        public String getPosition() {
                return position;
        }

        public String getEmail() {
                return email;
        }

        public String getPhoneNumber() {
                return phoneNumber;
        }

        public String getLineNumber() {
                return lineNumber;
        }

        public String getFaxNumber() {
                return faxNumber;
        }

        public String getAddress() {
                return address;
        }

        public String getMemoContent() {
                return memoContent;
        }

        public List<String> getHashTags() {
                return hashTags;
        }

        public void setIdx(Long idx) {
                this.idx = idx;
        }

        public void setUserIdx(Long userIdx) {
                this.userIdx = userIdx;
        }

        public void setName(String name) {
                this.name = name;
        }

        public void setCardCompanyName(String cardCompanyName) {
                this.cardCompanyName = cardCompanyName;
        }

        public void setCompanyIdx(Long companyIdx) {
                this.companyIdx = companyIdx;
        }

        public void setDepartment(String department) {
                this.department = department;
        }

        public void setPosition(String position) {
                this.position = position;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public void setPhoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
        }

        public void setLineNumber(String lineNumber) {
                this.lineNumber = lineNumber;
        }

        public void setFaxNumber(String faxNumber) {
                this.faxNumber = faxNumber;
        }

        public void setAddress(String address) {
                this.address = address;
        }

        public void setMemoContent(String memoContent) {
                this.memoContent = memoContent;
        }

        public void setHashTags(List<String> hashTags) {
                this.hashTags = hashTags;
        }
}

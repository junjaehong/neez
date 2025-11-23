package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.Users;
import com.bbey.neez.exception.AccessDeniedBizException;
import com.bbey.neez.exception.ResourceNotFoundException;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.repository.UserRepository;
import com.bbey.neez.security.SecurityUtil;
import com.bbey.neez.service.Company.CompanyInfoExtractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class BizCardServiceImpl implements BizCardService {

    private final BizCardRepository bizCardRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final MemoStorage memoStorage;
    private final HashtagService hashtagService;
    private final CompanyInfoExtractService companyInfoExtractService;

    public BizCardServiceImpl(BizCardRepository bizCardRepository,
                              CompanyRepository companyRepository,
                              UserRepository userRepository,
                              MemoStorage memoStorage,
                              HashtagService hashtagService,
                              CompanyInfoExtractService companyInfoExtractService) {
        this.bizCardRepository = bizCardRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.memoStorage = memoStorage;
        this.hashtagService = hashtagService;
        this.companyInfoExtractService = companyInfoExtractService;
    }

    private String nvl(String s) {
        return (s == null) ? "" : s;
    }

    // 🔒 공통: 명함 소유자 검증
    private void verifyOwnership(Long cardUserIdx) {
        Long current = SecurityUtil.getCurrentUserIdx();
        if (current == null || !current.equals(cardUserIdx)) {
            throw new AccessDeniedBizException("해당 명함에 대한 접근 권한이 없습니다.");
        }
    }

    // 🔹 /me 목록
    @Override
    public Page<BizCardDto> getMyBizCards(Pageable pageable) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();
        Page<BizCard> page = bizCardRepository
                .findByUserIdxAndIsDeletedFalseOrderByCreatedAtDesc(userIdx, pageable);
        return page.map(this::toDto);
    }

    // 🔥 OCR/수기 공통 저장 (현재 로그인 유저 기준)
    @Override
    @Transactional
    public BizCardSaveResult saveFromOcrData(Map<String, String> data) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();

        String companyName = nvl(data.get("company"));
        String address = nvl(data.get("address"));
        Long companyIdx = null;

        if (!companyName.isEmpty()) {
            Optional<Company> compOpt = Optional.empty();

            // 1차: 외부 API까지 사용하는 extractAndSave 시도
            if (!address.isEmpty()) {
                compOpt = companyInfoExtractService.extractAndSave(companyName, address);
            }

            // 2차: 실패 시 DB 기반 matchOrCreateCompany
            if (!compOpt.isPresent()) {
                compOpt = companyInfoExtractService.matchOrCreateCompany(companyName, address);
            }

            if (compOpt.isPresent()) {
                Company company = compOpt.get();
                companyIdx = company.getIdx();
                // companyName = company.getName(); // 저장 시 DB 회사명으로 통일하고 싶으면 사용
            }
        }

        Long finalUserId;
        if (userIdx != null && userIdx > 0 && userRepository.existsById(userIdx)) {
            finalUserId = userIdx;
        } else {
            // 이 케이스는 거의 안 타야 정상 (/me는 인증 필수)
            Users u = new Users();
            u.setUserId("auto_" + System.currentTimeMillis());
            u.setPassword("temp");
            u.setName("auto_generated");
            u.setEmail("auto@example.com");
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            finalUserId = userRepository.save(u).getIdx();
        }

        String name = nvl(data.get("name"));
        String email = nvl(data.get("email"));
        if (!name.isEmpty() && !email.isEmpty()) {
            Optional<BizCard> existedOpt = bizCardRepository.findByUserIdxAndNameAndEmail(finalUserId, name, email);
            if (existedOpt.isPresent()) {
                return new BizCardSaveResult(existedOpt.get(), true);
            }
        }

        BizCard card = new BizCard();
        card.setUserIdx(finalUserId != null ? finalUserId : 0L);
        card.setName(name);

        card.setCardCompanyName(companyName);
        card.setCompanyIdx(companyIdx);

        card.setDepartment(nvl(data.get("department")));
        card.setPosition(nvl(data.get("position")));
        card.setEmail(email);
        card.setPhoneNumber(nvl(data.get("mobile")));
        card.setLineNumber(nvl(data.get("tel")));
        card.setFaxNumber(nvl(data.get("fax")));
        card.setAddress(address);
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setIsDeleted(false);

        BizCard saved = bizCardRepository.save(card);

        // 🔥 메모 저장도 같은 트랜잭션
        String reqMemo = nvl(data.get("memo"));
        if (!reqMemo.isEmpty()) {
            String fileName = "card-" + saved.getIdx() + ".txt";
            try {
                memoStorage.write(fileName, reqMemo);
                saved.setMemo(fileName);
                saved.setUpdatedAt(LocalDateTime.now());
                saved = bizCardRepository.save(saved);
            } catch (IOException e) {
                throw new RuntimeException("메모 파일 저장에 실패했습니다.", e);
            }
        }

        return new BizCardSaveResult(saved, false);
    }

    @Override
    @Transactional
    public BizCardSaveResult saveManual(Map<String, String> data) {
        // 수기 등록도 OCR과 동일 파이프라인 사용
        return saveFromOcrData(data);
    }

    @Override
    public Map<String, Object> getBizCardDetail(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        verifyOwnership(card.getUserIdx());

        Long companyIdx = card.getCompanyIdx();
        String cardCompanyName = card.getCardCompanyName();

        String memoContent = "";
        if (card.getMemo() != null && !card.getMemo().isEmpty()) {
            try {
                memoContent = memoStorage.read(card.getMemo());
            } catch (IOException e) {
                memoContent = "(메모 파일을 불러오는 중 오류가 발생했습니다)";
            }
        }

        List<String> hashtags = hashtagService.getTagsOfCard(id);

        Map<String, Object> cardMap = new LinkedHashMap<>();
        cardMap.put("idx", card.getIdx());
        cardMap.put("user_idx", card.getUserIdx());
        cardMap.put("name", card.getName());
        cardMap.put("card_company_name", cardCompanyName);
        cardMap.put("company_idx", companyIdx);
        cardMap.put("department", card.getDepartment());
        cardMap.put("position", card.getPosition());
        cardMap.put("email", card.getEmail());
        cardMap.put("phone_number", card.getPhoneNumber());
        cardMap.put("line_number", card.getLineNumber());
        cardMap.put("fax_number", card.getFaxNumber());
        cardMap.put("address", card.getAddress());
        cardMap.put("memo_path", card.getMemo());
        cardMap.put("memo_content", memoContent);
        cardMap.put("hashtags", hashtags);
        cardMap.put("created_at", card.getCreatedAt());
        cardMap.put("updated_at", card.getUpdatedAt());

        return cardMap;
    }

    @Override
    public BizCardDto getBizCardDetailDto(Long id) {
        Map<String, Object> m = getBizCardDetail(id);
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) m.getOrDefault("hashtags", new ArrayList<String>());

        return new BizCardDto(
                (Long) m.get("idx"),
                (Long) m.get("user_idx"),
                (String) m.get("name"),
                (String) m.get("card_company_name"),
                (Long) m.get("company_idx"),
                (String) m.get("department"),
                (String) m.get("position"),
                (String) m.get("email"),
                (String) m.get("phone_number"),
                (String) m.get("line_number"),
                (String) m.get("fax_number"),
                (String) m.get("address"),
                (String) m.get("memo_content"),
                tags
        );
    }

    @Override
    @Transactional
    public BizCard updateBizCard(Long idx, Map<String, String> data, boolean rematchCompany) {
        BizCard card = bizCardRepository.findById(idx)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + idx));

        verifyOwnership(card.getUserIdx());

        String name = data.get("name");
        if (name != null && !name.isEmpty()) {
            card.setName(name);
        }

        String companyName = data.get("company");
        if (companyName != null) {
            card.setCardCompanyName(companyName);
        }

        String companyIdxStr = data.get("company_idx");
        if (companyIdxStr != null && !companyIdxStr.isEmpty()) {
            card.setCompanyIdx(Long.valueOf(companyIdxStr));
        }

        String department = data.get("department");
        if (department != null) {
            card.setDepartment(department);
        }

        String position = data.get("position");
        if (position != null) {
            card.setPosition(position);
        }

        String email = data.get("email");
        if (email != null) {
            card.setEmail(email);
        }

        String mobile = data.get("mobile");
        if (mobile != null) {
            card.setPhoneNumber(mobile);
        }

        String tel = data.get("tel");
        if (tel != null) {
            card.setLineNumber(tel);
        }

        String fax = data.get("fax");
        if (fax != null) {
            card.setFaxNumber(fax);
        }

        String address = data.get("address");
        if (address != null) {
            card.setAddress(address);
        }

        // 회사 정보 재매칭 옵션
        if (rematchCompany) {
            String rematchName = card.getCardCompanyName();
            String rematchAddr = card.getAddress();
            if (rematchName != null && !rematchName.isEmpty()) {
                Optional<Company> compOpt = Optional.empty();
                if (rematchAddr != null && !rematchAddr.isEmpty()) {
                    compOpt = companyInfoExtractService.extractAndSave(rematchName, rematchAddr);
                }
                if (!compOpt.isPresent()) {
                    compOpt = companyInfoExtractService.matchOrCreateCompany(rematchName, rematchAddr);
                }

                if (compOpt.isPresent()) {
                    Company company = compOpt.get();
                    card.setCompanyIdx(company.getIdx());
                    card.setCardCompanyName(company.getName());
                }
            }
        }

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }

    // 🔹 userIdx 기반 (관리자/통계용)
    @Override
    public Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.findByUserIdxAndIsDeletedFalseOrderByCreatedAtDesc(userIdx, pageable);
        return page.map(this::toDto);
    }

    @Override
    public Page<BizCardDto> getDeletedBizCardsByUserIdx(Long userIdx, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.findByUserIdxAndIsDeletedTrue(userIdx, pageable);
        return page.map(this::toDto);
    }

    @Override
    public long countBizCardsByUser(Long userIdx) {
        return bizCardRepository.countByUserIdxAndIsDeletedFalse(userIdx);
    }

    @Override
    @Transactional
    public void deleteBizCard(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        verifyOwnership(card.getUserIdx());

        card.setIsDeleted(true);
        card.setUpdatedAt(LocalDateTime.now());
        bizCardRepository.save(card);
    }

    @Override
    @Transactional
    public void restoreBizCard(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + id));

        verifyOwnership(card.getUserIdx());

        card.setIsDeleted(false);
        card.setUpdatedAt(LocalDateTime.now());
        bizCardRepository.save(card);
    }

    // 🔹 특정 userIdx에 대한 검색 (관리자/통계용)
    @Override
    public Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.searchByKeyword(userIdx, keyword, pageable);
        return page.map(this::toDto);
    }

    // 🔹 내 명함 검색(키워드)
    @Override
    public Page<BizCardDto> searchMyBizCards(String keyword, Pageable pageable) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();
        Page<BizCard> page = bizCardRepository.searchByKeyword(userIdx, keyword, pageable);
        return page.map(this::toDto);
    }

    @Override
    public Page<BizCardDto> getMyDeletedBizCards(Pageable pageable) {
        Long userIdx = SecurityUtil.getCurrentUserIdx();
        Page<BizCard> page = bizCardRepository.findByUserIdxAndIsDeletedTrue(userIdx, pageable);
        return page.map(this::toDto);
    }

    @Override
    public long countMyBizCards() {
        Long userIdx = SecurityUtil.getCurrentUserIdx();
        return bizCardRepository.countByUserIdxAndIsDeletedFalse(userIdx);
    }

    @Override
    public boolean existsMyBizCard(String name, String email) {
        if (name == null || email == null) return false;
        Long userIdx = SecurityUtil.getCurrentUserIdx();
        return bizCardRepository.existsByUserIdxAndNameAndEmailAndIsDeletedFalse(userIdx, name, email);
    }

    @Override
    public boolean existsBizCard(Long userIdx, String name, String email) {
        if (name == null || email == null) return false;
        return bizCardRepository.existsByUserIdxAndNameAndEmailAndIsDeletedFalse(userIdx, name, email);
    }

    private BizCardDto toDto(BizCard card) {
        if (card == null) return null;

        String memoContent = "";
        if (card.getMemo() != null && !card.getMemo().isEmpty()) {
            try {
                memoContent = memoStorage.read(card.getMemo());
            } catch (IOException e) {
                memoContent = "(메모 파일을 불러오는 중 오류가 발생했습니다)";
            }
        }

        List<String> hashtags = hashtagService.getTagsOfCard(card.getIdx());

        return new BizCardDto(
                card.getIdx(),
                card.getUserIdx(),
                card.getName(),
                card.getCardCompanyName(),
                card.getCompanyIdx(),
                card.getDepartment(),
                card.getPosition(),
                card.getEmail(),
                card.getPhoneNumber(),
                card.getLineNumber(),
                card.getFaxNumber(),
                card.getAddress(),
                memoContent,
                hashtags
        );
    }
}

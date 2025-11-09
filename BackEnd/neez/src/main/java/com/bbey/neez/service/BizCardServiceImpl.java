package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BizCardServiceImpl implements BizCardService {

    private final BizCardRepository bizCardRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final MemoStorage memoStorage;

    public BizCardServiceImpl(BizCardRepository bizCardRepository,
                              CompanyRepository companyRepository,
                              UserRepository userRepository,
                              MemoStorage memoStorage) {
        this.bizCardRepository = bizCardRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.memoStorage = memoStorage;
    }

    private String nvl(String s) {
        return (s == null) ? "" : s;
    }

    @Override
    public BizCardSaveResult saveFromOcrData(Map<String, String> data, Long userIdx) {
        // 회사
        String companyName = nvl(data.get("company"));
        Long companyIdx = null;
        if (!companyName.isEmpty()) {
            Optional<Company> opt = companyRepository.findByName(companyName);
            if (opt.isPresent()) {
                companyIdx = opt.get().getIdx();
            } else {
                Company c = new Company();
                c.setName(companyName);
                c.setCreated_at(LocalDateTime.now());
                c.setUpdated_at(LocalDateTime.now());
                companyIdx = companyRepository.save(c).getIdx();
            }
        }

        // 유저
        Long finalUserId;
        if (userIdx != null && userIdx > 0 && userRepository.existsById(userIdx)) {
            finalUserId = userIdx;
        } else {
            Users u = new Users();
            u.setName("auto_generated");
            u.setCreated_at(LocalDateTime.now());
            u.setUpdated_at(LocalDateTime.now());
            finalUserId = userRepository.save(u).getIdx();
        }

        // 중복 검사
        String name  = nvl(data.get("name"));
        String email = nvl(data.get("email"));
        if (!name.isEmpty() && !email.isEmpty()) {
            Optional<BizCard> existedOpt = bizCardRepository.findByNameAndEmail(name, email);
            if (existedOpt.isPresent()) {
                return new BizCardSaveResult(existedOpt.get(), true);
            }
        }

        // 신규 저장
        BizCard card = new BizCard();
        card.setUserIdx(finalUserId != null ? finalUserId : 0L);
        card.setName(name);
        card.setCompanyIdx(companyIdx);
        card.setDepartment(nvl(data.get("department")));
        card.setPosition(nvl(data.get("position")));
        card.setEmail(email);
        card.setPhoneNumber(nvl(data.get("mobile")));
        card.setLineNumber(nvl(data.get("tel")));
        card.setFaxNumber(nvl(data.get("fax")));
        card.setAddress(nvl(data.get("address")));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setIsDeleted(false);

        BizCard saved = bizCardRepository.save(card);

        // 메모가 있으면 파일 저장
        String reqMemo = nvl(data.get("memo"));
        if (!reqMemo.isEmpty()) {
            String fileName = "card-" + saved.getIdx() + ".txt";
            try {
                memoStorage.write(fileName, reqMemo);
                saved.setMemo(fileName);
                saved.setUpdatedAt(LocalDateTime.now());
                saved = bizCardRepository.save(saved);
            } catch (IOException e) {
                System.out.println("메모 파일 처리 실패: " + e.getMessage());
            }
        }

        return new BizCardSaveResult(saved, false);
    }

    @Override
    public BizCardSaveResult saveManual(Map<String, String> data, Long userIdx) {
        return saveFromOcrData(data, userIdx);
    }

    @Override
    public Map<String, Object> getBizCardDetail(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        String companyName = null;
        if (card.getCompanyIdx() != null && card.getCompanyIdx() > 0) {
            companyName = companyRepository.findById(card.getCompanyIdx())
                    .map(Company::getName)
                    .orElse(null);
        }

        String memoContent = "";
        if (card.getMemo() != null && !card.getMemo().isEmpty()) {
            try {
                memoContent = memoStorage.read(card.getMemo());
            } catch (IOException e) {
                memoContent = "(메모 파일을 불러오는 중 오류가 발생했습니다)";
            }
        }

        Map<String, Object> cardMap = new LinkedHashMap<>();
        cardMap.put("idx", card.getIdx());
        cardMap.put("user_idx", card.getUserIdx());
        cardMap.put("name", card.getName());
        cardMap.put("company_idx", card.getCompanyIdx());
        cardMap.put("company_name", companyName);
        cardMap.put("department", card.getDepartment());
        cardMap.put("position", card.getPosition());
        cardMap.put("email", card.getEmail());
        cardMap.put("phone_number", card.getPhoneNumber());
        cardMap.put("line_number", card.getLineNumber());
        cardMap.put("fax_number", card.getFaxNumber());
        cardMap.put("address", card.getAddress());
        cardMap.put("memo_path", card.getMemo());
        cardMap.put("memo_content", memoContent);
        cardMap.put("created_at", card.getCreatedAt());
        cardMap.put("updated_at", card.getUpdatedAt());

        return cardMap;
    }

    @Override
    public BizCard updateBizCard(Long idx, Map<String, String> data) {
        BizCard card = bizCardRepository.findById(idx)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + idx));

        String name = data.get("name");
        if (name != null && !name.isEmpty()) {
            card.setName(name);
        }

        String companyIdxStr = data.get("company_idx");
        if (companyIdxStr != null && !companyIdxStr.isEmpty()) {
            card.setCompanyIdx(Long.valueOf(companyIdxStr));
        }

        String dept = data.get("department");
        if (dept != null) card.setDepartment(dept);

        String position = data.get("position");
        if (position != null) card.setPosition(position);

        String email = data.get("email");
        if (email != null) card.setEmail(email);

        String mobile = data.get("mobile");
        if (mobile != null) card.setPhoneNumber(mobile);

        String tel = data.get("tel");
        if (tel != null) card.setLineNumber(tel);

        String fax = data.get("fax");
        if (fax != null) card.setFaxNumber(fax);

        String address = data.get("address");
        if (address != null) card.setAddress(address);

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }

    @Override
    public void deleteBizCard(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));
        card.setIsDeleted(true);
        card.setUpdatedAt(LocalDateTime.now());
        bizCardRepository.save(card);
    }

    @Override
    public void restoreBizCard(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));
        card.setIsDeleted(false);
        card.setUpdatedAt(LocalDateTime.now());
        bizCardRepository.save(card);
    }

    @Override
    public Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.findByUserIdxAndIsDeletedFalse(userIdx, pageable);
        return page.map(this::toDto);
    }

    @Override
    public Page<BizCardDto> getDeletedBizCardsByUserIdx(Long userIdx, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.findByUserIdxAndIsDeletedTrue(userIdx, pageable);
        return page.map(this::toDto);
    }

    @Override
    public Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.searchByKeyword(userIdx, keyword, pageable);
        return page.map(this::toDto);
    }

    @Override
    public long countBizCardsByUser(Long userIdx) {
        return bizCardRepository.countByUserIdxAndIsDeletedFalse(userIdx);
    }

    @Override
    public boolean existsBizCard(Long userIdx, String name, String email) {
        if (name == null || email == null) return false;
        return bizCardRepository.existsByUserIdxAndNameAndEmailAndIsDeletedFalse(userIdx, name, email);
    }

    private BizCardDto toDto(BizCard card) {
        String companyName = null;
        if (card.getCompanyIdx() != null) {
            companyName = companyRepository.findById(card.getCompanyIdx())
                    .map(Company::getName)
                    .orElse(null);
        }
        String memoContent = "";
        if (card.getMemo() != null && !card.getMemo().isEmpty()) {
            try {
                memoContent = memoStorage.read(card.getMemo());
            } catch (Exception ignored) {}
        }
        return new BizCardDto(
                card.getIdx(),
                card.getUserIdx(),
                card.getName(),
                companyName,
                card.getDepartment(),
                card.getPosition(),
                card.getEmail(),
                card.getPhoneNumber(),
                card.getLineNumber(),
                card.getFaxNumber(),
                card.getAddress(),
                memoContent
        );
    }
}

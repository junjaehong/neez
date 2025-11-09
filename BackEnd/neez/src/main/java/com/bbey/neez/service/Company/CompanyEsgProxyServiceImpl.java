package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.CompanyEsgProxyScore;
import com.bbey.neez.repository.CompanyEsgProxyScoreRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CompanyEsgProxyServiceImpl implements CompanyEsgProxyService {

    @Autowired
    private CompanyEsgProxyScoreRepository companyEsgProxyScoreRepository;

    @Override
    public BigDecimal evaluate(Company company) {
        // 기본점수
        double score = 0.5;

        // 1) 업종 가점/감점
        // 실제 ESG에서는 산업별 기준이 다르니까 아주 러프하게만.
        String industry = company.getIndustry();
        if (industry != null) {
            String ind = industry.toLowerCase();
            if (ind.contains("it") || ind.contains("software") || ind.contains("정보")
                    || ind.contains("플랫폼")) {
                score += 0.1;   // 디지털/서비스업은 보통 환경 리스크가 낮다 가정
            } else if (ind.contains("석유") || ind.contains("화학") || ind.contains("중공업")) {
                score -= 0.1;   // 환경부담 높은 업종
            }
        }

        // 2) 도메인/출처로 공공/교육/비영리 느낌이면 플러스
        String domain = company.getDomain();
        if (domain != null) {
            String d = domain.toLowerCase();
            if (d.endsWith(".go.kr") || d.endsWith(".or.kr") || d.contains("edu")) {
                score += 0.05;
            }
        }

        // 3) source 로직이 있으면 여기서도 미세 조정 가능
        // 예: 외부 수집(source != null)이면 최신성 있다고 보고 조금 플러스
        if (company.getSource() != null && !company.getSource().isEmpty()) {
            score += 0.02;
        }

        // 범위 클램프 0.0 ~ 1.0
        if (score > 1.0) score = 1.0;
        if (score < 0.0) score = 0.0;

         // ✅ ESG 점수 기록 저장
        CompanyEsgProxyScore rec = new CompanyEsgProxyScore();
        rec.setCompanyIdx(company.getIdx());
        rec.setScore(BigDecimal.valueOf(score));
        rec.setReason("industry=" + industry + ", domain=" + domain);
        rec.setCreatedAt(LocalDateTime.now());
        companyEsgProxyScoreRepository.save(rec);

        return BigDecimal.valueOf(score);
    }
}

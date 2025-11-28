package com.bbey.neez.repository;

import com.bbey.neez.entity.CompanyInsertRequest;
import com.bbey.neez.entity.CompanyInsertRequest.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyInsertRequestRepository
        extends JpaRepository<CompanyInsertRequest, Long> {

    Page<CompanyInsertRequest> findByStatus(RequestStatus status, Pageable pageable);
}

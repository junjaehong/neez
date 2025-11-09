package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;

public interface CompanyIdentificationService {
    Company findOrCreate(String name);
}

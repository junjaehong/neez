package com.bbey.neez.service;

import java.util.Map;
import com.bbey.neez.entity.BizCard;

public interface BizCardReaderService {

    public Map<String, String> readBizCard(String fileName);

    public BizCard saveBizCardFromOcr(Map<String, String> data, Long user_idx);

}
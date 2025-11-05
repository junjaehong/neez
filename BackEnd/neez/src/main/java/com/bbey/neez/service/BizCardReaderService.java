package com.bbey.neez.service;

import java.util.Map;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;

public interface BizCardReaderService {

    public Map<String, String> readBizCard(String fileName);

    public BizCardSaveResult saveBizCardFromOcr(Map<String, String> data, Long user_idx);

    public BizCardSaveResult saveManualBizCard(Map<String, String> data, Long user_idx);

}
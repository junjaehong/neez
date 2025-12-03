package com.bbey.neez.service.BizCard;

import java.io.IOException;

import com.bbey.neez.entity.BizCard.BizCard;

public interface BizCardMemoService {

    String getBizCardMemoContent(Long id) throws IOException;

    BizCard updateBizCardMemo(Long id, String memo);
}

package com.bbey.neez.service.BizCard;

import com.bbey.neez.entity.BizCard;

import java.io.IOException;

public interface BizCardMemoService {

    String getBizCardMemoContent(Long id) throws IOException;

    BizCard updateBizCardMemo(Long id, String memo);
}

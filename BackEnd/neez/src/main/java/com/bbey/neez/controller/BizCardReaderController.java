package com.bbey.neez.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.bbey.neez.service.BizCardReaderService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/bizcard")
public class BizCardReaderController {

    private final BizCardReaderService bizCardReaderServiceImpl;

    @Autowired
    public BizCardReaderController(BizCardReaderService bizCardReaderServiceImpl) {
        this.bizCardReaderServiceImpl = bizCardReaderServiceImpl;
    }

    @PostMapping("/read")
    public BizCardResponse readBizCard(@RequestBody BizCardRequest request) {
        return bizCardReaderServiceImpl.readBizCard(request);
    }
}
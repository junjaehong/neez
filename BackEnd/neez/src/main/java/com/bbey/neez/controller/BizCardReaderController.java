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

    @Autowired
    private BizCardReaderService bizCardReaderService;

    @RequestMapping("/read")
    public String readBizCard(){
        return bizCardReaderService.readBizCard();
    }
}
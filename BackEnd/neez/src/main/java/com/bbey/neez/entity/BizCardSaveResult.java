package com.bbey.neez.entity;

import com.bbey.neez.entity.BizCard;

public class BizCardSaveResult {
    private final BizCard bizCard;
    private final boolean existing;

    public BizCardSaveResult(BizCard bizCard, boolean existing) {
        this.bizCard = bizCard;
        this.existing = existing;
    }

    public BizCard getBizCard() {
        return bizCard;
    }

    public boolean isExisting() {
        return existing;
    }
}
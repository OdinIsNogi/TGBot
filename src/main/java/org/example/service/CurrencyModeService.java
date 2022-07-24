package org.example.service;

import org.example.entity.Currency;
import org.example.service.iml.HashMapCurrencyModeService;


public interface CurrencyModeService {

    static CurrencyModeService getInstance(){
        return new HashMapCurrencyModeService();
    }

    Currency getOriginalCurrency(long chatId);

    Currency getTargetCurrency(long chatid);

    void setOriginalCurrency(long chatId, Currency currency);

    void setTargetCurrency(long chatId, Currency currency);
}

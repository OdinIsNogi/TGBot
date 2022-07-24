package org.example.service;

import org.example.entity.Currency;
import org.example.service.iml.NbrbCurrencyConvertService;

public interface CurrencyConvertService {

    static CurrencyConvertService getInstance() {
        return new NbrbCurrencyConvertService();
    }

    double getConvertRatio(Currency original, Currency target);
}

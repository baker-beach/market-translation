package com.bakerbeach.market.core.service;

import java.util.List;
import java.util.Locale;

import com.bakerbeach.market.translation.api.model.I18NMessage;

public interface MessageDao {

	I18NMessage findByCode(String tag, String type, String code) throws DAOException;

	List<I18NMessage> findByCodePattern(String tag, String codePattern);

	I18NMessage findReverseTranslation(List<String> tags, String type, String translation, Locale locale) throws DAOException;

	void save(I18NMessage message);

}

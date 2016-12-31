package com.bakerbeach.market.translation.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceSupport;
import org.springframework.util.ObjectUtils;

import com.bakerbeach.market.core.service.DAOException;
import com.bakerbeach.market.core.service.I18NMessageImpl;
import com.bakerbeach.market.core.service.MessageDao;
import com.bakerbeach.market.translation.api.model.I18NMessage;
import com.bakerbeach.market.translation.api.service.TranslationService;

public class TranslationServiceImpl extends MessageSourceSupport implements TranslationService {
	private static final Logger log = LoggerFactory.getLogger(TranslationServiceImpl.class);

	private Map<String, MessageMap> reverseTranslationCaches = new HashMap<String, MessageMap>();
	private Map<String, MessageMap> messageCaches = new HashMap<String, MessageMap>();

	private MessageDao messageDao;
	private String defaultLocale;

	@Override
	public String getMessage(String tag, String code, Object[] args, String defaultMessage, Locale locale) {
		return getMessage(tag, "text", code, args, defaultMessage, locale);
	}
	
	@Override
	public String getMessage(String tag, String type, String code, Object[] args, String defaultMessage, Locale locale) {
		I18NMessage message = getMessage(tag, type, code);
		return getText(message, defaultMessage, locale, args);
	}
	
	@Override
	public I18NMessage getReverseUrlTranslation(List<String> tags, String text, Locale locale) throws TranslationServiceException {
		StringBuilder keyBuilder = new StringBuilder();
		for (String tag : tags) {
			keyBuilder.append(tag);
		}
		String key = keyBuilder.toString();
		
		
		I18NMessage message = null;
		MessageMap reverseTranslationCache = reverseTranslationCaches.get(key);
		if (reverseTranslationCache != null) {
			message = reverseTranslationCache.get(text);
			if (message != null) {
				Date now = new Date();
				if (message.getLastUpdate().getTime() > now.getTime() - 6000000) {
					return message;
				}
			}
		}
		
		try {
			message = loadReverseUrlTranslation(tags, "url",  text, locale);
		} catch (TranslationServiceException e) {
			log.warn(ExceptionUtils.getMessage(e));
			
			message = new I18NMessageImpl();
			((I18NMessageImpl) message).setMessageKey(text);
			((I18NMessageImpl) message).setTag(tags.get(0));
			((I18NMessageImpl) message).setType("url");
			
			HashMap<String,String> messageMap = new HashMap<String,String>();
			messageMap.put(locale.toString(), text);
			((I18NMessageImpl) message).setMessages(messageMap);
		}
		
		if (!reverseTranslationCaches.containsKey(key)) {
			reverseTranslationCaches.put(key, new MessageMap());
		}
		reverseTranslationCaches.get(key).put(text, message);			

		return message;
	}
	
	private I18NMessage getMessage(String tag, String type, String code) {
		String key = new StringBuilder(tag).append(":").append(type).toString();
		
		MessageMap messageCache = messageCaches.get(key);
		if (messageCache != null) {
			I18NMessage message = messageCache.get(code);
			if (message != null) {
				Date now = new Date();
				if (message.getLastUpdate().getTime() > now.getTime() - 6000000) {
					return message;
				}
			}
		}
		
		I18NMessage message = loadMessage(tag, type, code);
		if (!messageCaches.containsKey(key)) {
			messageCaches.put(key, new MessageMap());
		}
		messageCaches.get(key).put(code, message);
		
		return message;
	}

	/*
	private I18NMessage getMessage(String tag, String type, String code) {
		MessageMap messageCache = messageCaches.get(tag);
		if (messageCache != null) {
			I18NMessage message = messageCache.get(code);
			if (message != null) {
				Date now = new Date();
				if (message.getLastUpdate().getTime() > now.getTime() - 6000000) {
					return message;
				}
			}
		}
		
		I18NMessage message = loadMessage(tag, type, code);
		if (!messageCaches.containsKey(tag)) {
			messageCaches.put(tag, new MessageMap());
		}
		messageCaches.get(tag).put(code, message);
		
		return message;
	}
	*/
	
	private I18NMessage loadMessage(String tag, String type, String code) {
		try {
			I18NMessage message = messageDao.findByCode(tag, type, code);
			if (message != null) {
				return message;
			} else {
				throw new DAOException("");
			}
		} catch (Exception e) {
			I18NMessageImpl m = new I18NMessageImpl();
			m.setMessageKey(code);
			return m;
		}
	}

	private I18NMessage loadReverseUrlTranslation(List<String> tags, String type, String text, Locale locale) throws TranslationServiceException {
		try {
			I18NMessage message = messageDao.findReverseTranslation(tags, type, text, locale);
			if (message != null) {
				return message;
			} else {
				throw new TranslationServiceException();
			}
		} catch (DAOException e) {
			throw new TranslationServiceException();
		}
	}

	private String getText(I18NMessage message, String defaultText, Locale locale, Object[] args) {
		for (Locale tmpLocale : calculateLocaleKeysToCheck(locale)) {
			if (message.getText(tmpLocale) != null) {
				return renderMessage(message.getText(tmpLocale), tmpLocale, args);
			}
		}
		
		return renderMessage(defaultText, locale, args);
	}
	
	private List<Locale> calculateLocaleKeysToCheck(Locale locale) {
		List<Locale> result = new ArrayList<>(4);
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		StringBuilder temp = new StringBuilder();

		result.add(0, new Locale(defaultLocale));

		if (language.length() > 0) {
			temp.append(language);
			result.add(0, LocaleUtils.toLocale(temp.toString()));
		}

		if (country.length() > 0) {
			temp.append('_').append(country);
			result.add(0, LocaleUtils.toLocale(temp.toString()));
		}
		
		Locale test = Locale.UK;

		if (variant.length() > 0 && (language.length() > 0 || country.length() > 0)) {
			temp.append('_').append(variant);
			result.add(0, LocaleUtils.toLocale(temp.toString()));
		}

		return result;
	}

	private String renderMessage(String message, Locale locale, Object[] args) {
		if (message != null && locale != null) {
			MessageFormat messageFormat = new MessageFormat(message, locale);
			Object[] argsToUse = args;

			if (ObjectUtils.isEmpty(args))
				return message;
			else
				return messageFormat.format(argsToUse);
		} else {
			return "";
		}
	}

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	private static class MessageMap extends HashMap<String, I18NMessage> {
		private static final long serialVersionUID = 1L;
	}

}

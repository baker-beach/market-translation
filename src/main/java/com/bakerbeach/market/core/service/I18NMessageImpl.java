package com.bakerbeach.market.core.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.bakerbeach.market.translation.api.model.I18NMessage;

public class I18NMessageImpl implements I18NMessage {

	private String messageKey;
	private String tag;
	private String type;
	private Map<String, String> messages = new HashMap<String, String>();
	private Date lastUpdate = new Date();

	@Override
	public Map<String, String> getMessages() {
		return messages;
	}

	public void setMessages(Map<String, String> messages) {
		this.messages = messages;
	}

	@Override
	public Set<String> getAviableLocales() {
		return messages.keySet();
	}

	@Override
	public String getText(Locale locale) {
		return messages.get(locale.toString());
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public String getKey() {
		return messageKey;
	}

	@Override
	public String getText(String locale) {
		return messages.get(locale);
	}

	@Override
	public Date getLastUpdate() {
		return lastUpdate;
	}

	@Override
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Override
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("code: ").append(getKey()).append(", tag: ").append(getTag()).append(", type: ").append(getType());
		getMessages().forEach((k,v) -> {
			str.append(", ").append(k).append(": ").append(v);			
		});

		return str.toString();
	}

}

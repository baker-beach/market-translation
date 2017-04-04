package com.bakerbeach.market.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.bakerbeach.market.translation.api.model.I18NMessage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class MessageDaoImpl implements MessageDao {

	private MongoTemplate mongoTemplate;
	private String messagesCollection;
	private DBCollection dBCollection;

	@Override
	public void save(I18NMessage message) {
		String code = message.getKey();
		String tag = message.getTag();
		String type = message.getType();
		
		DBObject query = new BasicDBObject();
		query.put("code", code);
		query.put("tag", tag);
		query.put("type", type);
		
		DBObject update = new BasicDBObject();
		
		DBObject translations = new BasicDBObject();
		for (String locale : message.getAviableLocales()) {
			translations.put(locale, message.getText(locale));
		}
		if (!translations.keySet().isEmpty()) {
			update.put("$set", new BasicDBObject("translations", translations));
			getMessagesCollection().findAndModify(query, null, null, false, update, false, true);			
		}
	}
	
	@Override
	public I18NMessage findByCode(String tag, String type, String code) throws DAOException {
		QueryBuilder qb = new QueryBuilder();
		qb.and("code").is(code);
		if (tag != null) {
			qb.and("tag").is(tag);
		}
		if (type != null) {
			qb.and("type").is(type);
		}
		DBObject dbo = getMessagesCollection().findOne(qb.get());
		if (dbo != null) {
			return convertDBObject2Translation(dbo);
		} else {
			throw new DAOException("");
		}
	}

	@Override
	public List<I18NMessage> findByCodePattern(String tag, String codePattern) {
		QueryBuilder qb = new QueryBuilder();
		qb.and("code").regex(Pattern.compile(codePattern));
		if (tag != null) {
			qb.and("tag").is(tag);
		}
		DBCursor dbc = getMessagesCollection().find(qb.get());
		List<I18NMessage> result = new ArrayList<I18NMessage>();
		for (DBObject dbo : dbc) {
			try {
				result.add(convertDBObject2Translation(dbo));
			} catch (Exception e) {
			}
		}
		return result;
	}

	@Override
	public I18NMessage findReverseTranslation(List<String> tag, String type, String translation, Locale locale)
			throws DAOException {
		String key = new StringBuilder("translations.").append(locale).toString();

		QueryBuilder qb = new QueryBuilder();
		qb.and(key).is(translation);
		qb.and("tag").in(tag);
		qb.and("type").is(type);
		DBObject dbo = getMessagesCollection().findOne(qb.get());
		if (dbo != null) {
			I18NMessageImpl message = new I18NMessageImpl();
			message.setMessageKey(translation);
			message.setTag((String) dbo.get("tag"));
			message.setType((String) dbo.get("type"));

			HashMap<String, String> messageMap = new HashMap<String, String>();
			messageMap.put(Locale.ENGLISH.toString(), (String) dbo.get("code"));
			message.setMessages(messageMap);

			return message;
		} else {
			throw new DAOException("");
		}
	}

	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	private DBCollection getMessagesCollection() {
		if (dBCollection == null)
			dBCollection = mongoTemplate.getCollection(messagesCollection);
		return dBCollection;
	}

	private I18NMessage convertDBObject2Translation(DBObject dbo) {
		I18NMessageImpl translation = new I18NMessageImpl();
		translation.setMessageKey((String) dbo.get("code"));
		translation.setTag((String) dbo.get("tag"));
		translation.setType((String) dbo.get("type"));
		HashMap<String, String> messageMap = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Map<String, String> translations = (Map<String, String>) dbo.get("translations");
		if (translations != null) {
			for (Map.Entry<String, String> entry : translations.entrySet()) {
				String lang = entry.getKey();
				Locale locale;
				if (lang.contains("_")) {
					locale = new Locale(lang.split("_")[0], lang.split("_")[1]);
				} else {
					locale = new Locale(lang);
				}
				String msg = entry.getValue();
				messageMap.put(locale.toString(), msg);
			}
			translation.setMessages(messageMap);
		}
		return translation;
	}

	public void setMessagesCollection(String messagesCollection) {
		this.messagesCollection = messagesCollection;
	}

}

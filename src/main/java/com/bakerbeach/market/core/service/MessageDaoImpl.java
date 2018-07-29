package com.bakerbeach.market.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.bakerbeach.market.translation.api.model.I18NMessage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class MessageDaoImpl implements MessageDao {

    private MongoTemplate mongoTemplate;
    private String messagesCollection;
    private MongoCollection<Document> dBCollection;

    @Override
    public void save(I18NMessage message) {
        String code = message.getKey();
        String tag = message.getTag();
        String type = message.getType();

        Bson filter = Filters.and(Filters.eq("code", code), Filters.eq("tag", tag), Filters.eq("type", type));

        DBObject translations = new BasicDBObject();
        for (String locale : message.getAviableLocales()) {
            translations.put(locale, message.getText(locale));
        }
        if (!translations.keySet().isEmpty()) {
            getMessagesCollection().findOneAndUpdate(filter, new Document(new BasicDBObject("translations", translations)));
        }
    }

    @Override
    public I18NMessage findByCode(String tag, String type, String code) throws DAOException {
        Bson filter = Filters.eq("code", code);
        if (tag != null) {
            filter = Filters.and(filter, Filters.eq("tag", tag));
        }
        if (type != null) {
            filter = Filters.and(filter, Filters.eq("type", type));
        }
        FindIterable<Document> dbo = getMessagesCollection().find(filter);
        if (dbo != null && dbo.first() != null) {
            return convertDBObject2Translation(dbo.first());
        } else {
            throw new DAOException("");
        }
    }

    @Override
    public List<I18NMessage> findByCodePattern(String tag, String codePattern) {
        Bson filter = Filters.regex("code", Pattern.compile(codePattern));
        if (tag != null) {
            filter = Filters.and(filter, Filters.eq("tag", tag));
        }
        FindIterable<Document> dbo = getMessagesCollection().find(filter);
        List<I18NMessage> result = new ArrayList<>();
        for (Document document : dbo) {
            try {
                result.add(convertDBObject2Translation(document));
            } catch (Exception e) {
            }
        }
        return result;
    }

    @Override
    public I18NMessage findReverseTranslation(List<String> tag, String type, String translation, Locale locale) throws DAOException {
        String key = new StringBuilder("translations.").append(locale).toString();

        Bson filter = Filters.and(Filters.eq(key, translation), Filters.in("tag", tag), Filters.eq("type", type));
        FindIterable<Document> dbo = getMessagesCollection().find(filter);
        if (dbo != null && dbo.first() != null) {
            I18NMessageImpl message = new I18NMessageImpl();
            message.setMessageKey(translation);
            message.setTag((String) dbo.first().get("tag"));
            message.setType((String) dbo.first().get("type"));

            HashMap<String, String> messageMap = new HashMap<>();
            messageMap.put(Locale.ENGLISH.toString(), (String) dbo.first().get("code"));
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

    private MongoCollection<Document> getMessagesCollection() {
        if (dBCollection == null)
            dBCollection = mongoTemplate.getCollection(messagesCollection);
        return dBCollection;
    }

    private I18NMessage convertDBObject2Translation(Map<?, ?> dbo) {
        I18NMessageImpl translation = new I18NMessageImpl();
        translation.setMessageKey((String) dbo.get("code"));
        translation.setTag((String) dbo.get("tag"));
        translation.setType((String) dbo.get("type"));
        HashMap<String, String> messageMap = new HashMap<>();
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

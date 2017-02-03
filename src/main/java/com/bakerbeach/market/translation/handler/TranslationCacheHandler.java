package com.bakerbeach.market.translation.handler;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bakerbeach.market.translation.api.service.TranslationService;

public class TranslationCacheHandler {
	final Logger log = LoggerFactory.getLogger(TranslationCacheHandler.class);

	@Autowired
	private TranslationService translationService;

	public void clearCache(Exchange ex) {
		try {
			Map<String, Object> payload = getPayload(ex.getIn());
			if (payload != null) {
				String tag = (String) payload.get("tag");
				String type = (String) payload.get("type");
				String code = (String) payload.get("code");
				
				if (StringUtils.isNotBlank(tag) && StringUtils.isNotBlank(type) && StringUtils.isNotBlank(code)) {
					translationService.clearCache(tag, type, code);					
				} else {
					translationService.clearCache();					
				}				
			} else {
				translationService.clearCache();				
			}
		} catch (Exception e) {
			translationService.clearCache();				
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getPayload(Message message) throws Exception {
		Map<String, Object> payload = null;
		try {			
			if (message.getBody() instanceof String) {
				String payloadAsString = (String) message.getBody();
				if (payloadAsString != null && !payloadAsString.isEmpty()) {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = (Map<String, Object>) mapper.readValue(payloadAsString, Map.class);
					message.setBody(map);
				}
			}
						
			payload = (Map<String, Object>) message.getBody();
		} catch (IOException e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}
		
		return payload;
	}

}

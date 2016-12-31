package com.bakerbeach.market.translation.service;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.bakerbeach.market.translation.api.model.I18NMessage;
import com.bakerbeach.market.translation.api.service.TranslationService;

@ActiveProfiles(profiles = { "env.test", "product.published" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:spring/market-*.xml" })
public class TranslationServiceTest {

	@Autowired()
	protected TranslationService translationService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		int test = 0;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetMessage() {
		String tag = "category.url";
		String code = "men.ski.ski-jackets";
		Object[] args = null;
		String defaultMessage = "default";
		Locale locale = Locale.US;
		
		String message = translationService.getMessage(tag, code, args, defaultMessage, locale);
		Assert.assertTrue(message != null);
		Assert.assertTrue(message.equals("men/ski/ski-jackets"));
		
		// reload from cache ---
		String cachedMessage = translationService.getMessage(tag, code, args, defaultMessage, locale);
		Assert.assertTrue(message.equals(cachedMessage));
	}
	
	@Test
	public void testGetReverseTranslation() {
		List<String> tags = Arrays.asList("category.url");
		String text = "men/ski/ski-jackets";
		String code = "men.ski.ski-jackets";
		Locale locale = Locale.US;
			
		I18NMessage message = null;
		try {
			// load from db ---
			message = translationService.getReverseUrlTranslation(tags, text, locale);
			Assert.assertTrue(message != null);
			Assert.assertTrue(message.getText(Locale.ENGLISH).equals(code));
		} catch (TranslationServiceException e) {
			fail(e.getMessage());
		}

		try {
			// reload from cache ---
			I18NMessage cachedMessage = translationService.getReverseUrlTranslation(tags, text, locale);
			Assert.assertTrue(cachedMessage != null);
			Assert.assertTrue(message.equals(cachedMessage));			
		} catch (TranslationServiceException e) {
			fail(e.getMessage());
		}
		
	}

	/*
	@Test
	public void testGetReverseTranslation() {
		String tag = "category.url";
		String text = "men/ski/ski-jackets";
		String code = "men.ski.ski-jackets";
		Locale locale = Locale.US;
		
		// load from db ---
		I18NMessage message = translationService.getReverseTranslation(tag, text, locale);
		Assert.assertTrue(message.getTag().equals(tag));
		Assert.assertTrue(message.getKey().equals(text));
		Assert.assertTrue(message.getText(Locale.ENGLISH).equals(code));
		
		// reload from cache ---
		I18NMessage cachedMessage  = translationService.getReverseTranslation(tag, text, locale);
		Assert.assertTrue(message.equals(cachedMessage));
	}
	*/
}

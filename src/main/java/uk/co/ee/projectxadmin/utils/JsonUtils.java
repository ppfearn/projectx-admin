package uk.co.ee.projectxadmin.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtils {
	
	public static <T> T objectFromJson(String json, T clazz) {
		ObjectMapper objectMapper = getObjectMapper();
		@SuppressWarnings("unchecked")
		T fromJson = null;
		try {
			fromJson = (T) objectMapper.readValue(json, clazz.getClass());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return fromJson;
	}

	public static String jsonFromObject(Object obj) {
		
		ObjectMapper objectMapper = getObjectMapper();
		try {
			return  objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;		
	}

	
	
	private static ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		return objectMapper;
	}
	
	
}

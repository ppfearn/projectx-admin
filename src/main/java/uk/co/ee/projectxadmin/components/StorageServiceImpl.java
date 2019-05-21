package uk.co.ee.projectxadmin.components;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {
	
	private ClassLoader classLoader = getClass().getClassLoader();;
	
	private Map<String, CarLogo> logos = new HashMap<>();

	public CarLogo getLogo(String car) {
		synchronized(logos) {
			return logos.get(car);
		}
	}

	public URL getFlag(String car) {
		return classLoader.getResource("static/images/flag" + car + ".png");
	}

	public void setLogo(String car, byte[] imageData, String mimeType) {
		if (car != null && imageData != null && mimeType != null) {
			synchronized(logos) {
				CarLogo logo = new CarLogo();
				logo.setImageData(imageData);
				logo.setMimeType(mimeType);
				logos.put(car, logo);
			}
		}
		
	}
	
	public void clear() {
		synchronized(logos) {
			logos.clear();
		}
	}

	@Override
	public URL getResource(String path) {
		return classLoader.getResource(path);
	}


}

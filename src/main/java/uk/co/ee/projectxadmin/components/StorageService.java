package uk.co.ee.projectxadmin.components;

import java.net.URL;

public interface StorageService {
	
	public CarLogo getLogo(String car);
	public URL getFlag(String car);
	public URL getResource(String path);
	
	public void setLogo(String car, byte[] imageData, String mimeType);
	public void clear();

}

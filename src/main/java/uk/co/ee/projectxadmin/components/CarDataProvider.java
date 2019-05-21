package uk.co.ee.projectxadmin.components;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CarDataProvider {
	
	@Autowired
	private StorageService storageService;
	
	private List<CarData> carData  = new ArrayList<>();
	
	private List<String> ipAddresses = new ArrayList<>();
	
	{
		ipAddresses.add("192.168.1.48");
		ipAddresses.add("192.168.1.48");
		ipAddresses.add("192.168.1.48");
		ipAddresses.add("192.168.1.48");
		ipAddresses.add("192.168.1.48");
		ipAddresses.add("192.168.1.48");
		ipAddresses.add("192.168.1.48");
		
	}
	
	// TODO - For testing to create initial cars. remove later
	{
		initDummyCarData();
	}
	
	public List<CarData> getCarData() {
		synchronized (carData) {
			return carData;
		}
	}
	
	public CarData addCar(String teamName, MultipartFile logo) throws Exception {
		synchronized (carData) {
			if (carData.size() >= ipAddresses.size()) {
				throw new Exception("All cars allocated");
			}
			int nextCarId = carData.size() + 1;
			CarData car = new CarData();
			car.teamName =  teamName;
			car.index = nextCarId;
			car.ipAddress = ipAddresses.get(carData.size());
			carData.add(car);
			if (logo != null) {
				storageService.setLogo(String.valueOf(car.index), logo.getBytes(), logo.getContentType());
			}
			
			return car;
		}
		
	}

	private void initDummyCarData() {
		CarData carInfo = new CarData();
		carInfo.setIndex(1);
		carInfo.setTeamName("Team 1");
		carInfo.setFlagImage("flag 1");
		carInfo.setIpAddress(ipAddresses.get(0));
		carData.add(carInfo);
		
		carInfo = new CarData();
		carInfo.setIndex(2);
		carInfo.setTeamName("Team 2");
		carInfo.setFlagImage("flag 2");
		carInfo.setIpAddress(ipAddresses.get(1));
		carData.add(carInfo);
		
	}

	public void setCarData(List<CarData> carData) {
		synchronized (carData) {
			this.carData = carData;
		}
	}

	public List<String> getIpAddresses() {
		return ipAddresses;
	}

	public void setIpAddresses(List<String> ipAddresses) {
		this.ipAddresses = ipAddresses;
	}
	
	public void reset() {
		synchronized (carData) {
			carData.clear();
		}
	}
	
	
	
}

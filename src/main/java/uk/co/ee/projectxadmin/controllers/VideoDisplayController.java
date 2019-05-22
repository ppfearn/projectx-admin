package uk.co.ee.projectxadmin.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import uk.co.ee.projectxadmin.components.CarData;
import uk.co.ee.projectxadmin.components.CarDataProvider;
import uk.co.ee.projectxadmin.components.CarLogo;
import uk.co.ee.projectxadmin.components.StorageService;
import uk.co.ee.projectxadmin.utils.JsonUtils;

@Controller
public class VideoDisplayController {

	String actionPattern = ".*(action=[a-z]+).*";
	String speedPattern = ".*(speed=[0-9]+).*";
	Boolean isRunning = true;

	@Autowired
	private StorageService storageService;

	@Autowired
	private CarDataProvider carData;

	// The main screen
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		return "dashboard";
	}
	
	// The main screen
	@GetMapping("/enter-team-details")
	public String enterTeamDetails(Model model) {
		return "enter-team-details";
	}

	// Get a "test" video steam
    @GetMapping("/test-stream")
    public void getTestStream(HttpServletResponse response) throws IOException {
    	InputStream in = storageService.getResource("static/images/test-image.jpeg").openStream();
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        IOUtils.copy(in, response.getOutputStream());
    }

	// Get the car data from the CarDataProvider
	@RequestMapping(value = "/car-data", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getCarData() {
		String json = JsonUtils.jsonFromObject(carData.getCarData());
		System.out.println("json: " + json);
		return json;
	}
	
	

	// Create a car with an optional logo
	@RequestMapping(value = "/create-car", method = RequestMethod.POST, produces = "application/json")
	public String addCar(@RequestParam("team-name") String teamName,
			@RequestParam(name = "logo", required = false) MultipartFile logo) {
			System.out.println("logo: " +logo);
		try {
			CarData car = carData.addCar(teamName, logo);
			return "dashboard";
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}

	}

	// Get the car data from the CarDataProvider
	@RequestMapping(value = "/car-logo", method = RequestMethod.GET, produces = "image/*")
	public void getCarLogo(@RequestParam("car") String car, HttpServletResponse response) {
		CarLogo logo = storageService.getLogo(car);

		if (logo != null) {
			response.setContentType(logo.getMimeType());
			try {
				response.getOutputStream().write(logo.getImageData());
				response.setStatus(200);
			} catch (IOException e) {
				e.printStackTrace();
				response.setStatus(500);
			}
		} else {
			response.setStatus(404);
		}
	}
	
	// Get the car data from the CarDataProvider
	@RequestMapping(value = "/car-flag", method = RequestMethod.GET, produces = "image/png")
	public void getCarFlag(@RequestParam("car") String car, HttpServletResponse response) {
		URL flagUrl = storageService.getFlag(car);

		if (flagUrl != null) {
			response.setContentType(MediaType.IMAGE_PNG_VALUE);
			try {
				StreamUtils.copy(flagUrl.openStream(), response.getOutputStream());
				response.setStatus(200);
			} catch (IOException e) {
				e.printStackTrace();
				response.setStatus(500);
			}
			
		} else {
			response.setStatus(404);
		}
	}


	// Get the last entry in a log file for a specified car
	@RequestMapping(value = "/car-logs", method = RequestMethod.GET, produces = "application/text")
	public @ResponseBody String getCarLogs(@RequestParam("car") String car, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (isRunning) {
				String line = getLastLogEntry(car, request, response);
				return parseLine(line);
			} else {
				response.setStatus(406);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "unknown";
	}
	
	// TEMP METHOD TO TEST LOGS WHEN CAR NOT CONNECTED
	@RequestMapping(value = "/car-logs-test", method = RequestMethod.GET, produces = "application/text")
	public @ResponseBody String getCarLogsTest(@RequestParam("car") String car, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			if (isRunning) {
				String line = readRandomLineFromStream(storageService.getResource("picar.log").openStream());
				return parseLine(line);
			} else {
				response.setStatus(406);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "unknown";
	}

	private String getLastLogEntry(String car, HttpServletRequest req, HttpServletResponse resp) {
		try {

			String ipAddress = carData.getIpAddresses().get(Integer.parseInt(car));
			final URL url = new URL("http://" + ipAddress + "/admin/log/picar.log.txt");
			System.out.println("Forwarding to: " + url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			Encoder enc = Base64.getEncoder();
			String userpassword = "admin" + ":" + "H3rm3s";
			String encodedAuthorization = enc.encodeToString(userpassword.getBytes());
			conn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			conn.setConnectTimeout(2000);
			conn.setRequestMethod("GET");

			final Enumeration<String> headers = req.getHeaderNames();
			while (headers.hasMoreElements()) {
				final String header = headers.nextElement();
				final Enumeration<String> values = req.getHeaders(header);
				while (values.hasMoreElements()) {
					final String value = values.nextElement();
					conn.addRequestProperty(header, value);
				}
			}

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.connect();

			return readLastLineFromStream(conn.getInputStream());
		} catch (Exception e) {
			resp.setStatus(408);
			return null;
		}
	}
	
	private String readLastLineFromStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = null;
		while (reader.ready()) {
			line = reader.readLine();
		}

		is.close();
		return line;
	}
	
	private String readRandomLineFromStream(InputStream is) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		List<String> lines = new ArrayList<>();
		String line = null;
		while (reader.ready()) {
			lines.add(reader.readLine());
		}

		is.close();
		
		int rnd = new Random().nextInt(lines.size());
		
		return lines.get(rnd);
	}
	
	

	// Reset the cars data
	@RequestMapping(value = "/reset", method = RequestMethod.GET)
	public String reset() {
		isRunning = false;
		carData.reset();
		return "dashboard";
	}

	// Start a new race
	@RequestMapping(value = "/start-new-game", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<Void> startNewGame() {
		isRunning = true;
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private String parseLine(String line) {
		if (line == null) {
			return "unknown";
		}
		Matcher matcher = Pattern.compile(actionPattern).matcher(line);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		matcher = Pattern.compile(speedPattern).matcher(line);
		if (matcher.matches()) {
			return matcher.group(1);
		}

		return "unknown";
	}

	// Remote control of individual Pis
	@GetMapping("/cali/")
	public void calibrate(HttpServletRequest req, HttpServletResponse resp, @RequestParam("action") String action,
			@RequestParam("car") String car) throws IOException {
		forwardRequest(car, action, req, resp);
	}

	@GetMapping("/run")
	public void run(HttpServletRequest req, HttpServletResponse resp, @RequestParam("action") String action,
			@RequestParam("car") String car) throws IOException {
		forwardRequest(car, action, req, resp);
	}

	private void forwardRequest(String car, String action, HttpServletRequest req, HttpServletResponse resp) {
		String requestPath = req.getRequestURI();

		try {
			String ipAddress = carData.getIpAddresses().get(Integer.parseInt(car));
			final URL url = new URL("http://" + ipAddress + ":8000" + requestPath + "/?action=" + action);
			System.out.println("Forwarding to: " + url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			Encoder enc = Base64.getEncoder();
			String userpassword = "admin" + ":" + "H3rm3s";
			String encodedAuthorization = enc.encodeToString(userpassword.getBytes());
			conn.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			conn.setConnectTimeout(2000);
			conn.setRequestMethod("GET");

			final Enumeration<String> headers = req.getHeaderNames();
			while (headers.hasMoreElements()) {
				final String header = headers.nextElement();
				final Enumeration<String> values = req.getHeaders(header);
				while (values.hasMoreElements()) {
					final String value = values.nextElement();
					conn.addRequestProperty(header, value);
				}
			}

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.connect();

			resp.setStatus(conn.getResponseCode());
		} catch (Exception e) {
			resp.setStatus(408);
		}
	}

}

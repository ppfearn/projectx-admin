package uk.co.ee.projectxadmin.controllers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Enumeration;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import uk.co.ee.projectxadmin.components.CarData;
import uk.co.ee.projectxadmin.components.CarDataProvider;
import uk.co.ee.projectxadmin.utils.JsonUtils;
@Controller
public class VideoDisplayController { 
	
	String actionPattern = ".*(action=[a-z]+).*";
	String speedPattern = ".*(speed=[0-9]+).*";
	Boolean isRunning = true;
	
	@Autowired
	private CarDataProvider carData;
	
	// The main screen
	@GetMapping("/home")
    public String welcome(Model model){
    	System.out.println("In WelcomeController");
        return "welcome";
    }
	
	// Get a "test" video steam
//    @GetMapping("/stream")
//    public void stream(HttpServletResponse response) throws IOException {
//    	InputStream in = new FileInputStream( "/Users/davidgough/projectx-admin/src/main/resources/static/images/test-image.jpeg" );
//        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
//        IOUtils.copy(in, response.getOutputStream());
//    }
    
    
    // Get the car data from the CarDataProvider
    @RequestMapping(value = "/car-data", method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody String getCarData() {
    	String json = JsonUtils.jsonFromObject(carData.getCarData());
    	System.out.println("json: " + json);
        return json;
        
        
    }
    
    @RequestMapping(value = "/create-car", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> addCar(@RequestParam("team-name") String teamName, @RequestParam(name="logo", required=false) MultipartFile logo) {
    	
    	try {
    		CarData car = carData.addCar(teamName, logo);
    		return new ResponseEntity<String>(JsonUtils.jsonFromObject(car), HttpStatus.CREATED);
    	} catch (Exception e) {
    		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    	
    }
    
    // Get the last entry in a log file for a specified car
    @RequestMapping(value = "/car-logs", method = RequestMethod.GET, produces = "application/text")
    public @ResponseBody String getCarLogs(@RequestParam("car") String car, HttpServletRequest request, HttpServletResponse response) {
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
    
    private String getLastLogEntry(String car, HttpServletRequest req, HttpServletResponse resp) {
	    try {
	    	
	    	String ipAddress = carData.getIpAddresses().get(Integer.parseInt(car));
	        final URL url = new URL("http://" + ipAddress + "/admin/log/picar.log.txt");
	        System.out.println("Forwarding to: " + url.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        Encoder enc = Base64.getEncoder();
	        String userpassword = "admin" + ":" + "H3rm3s";
	        String encodedAuthorization = enc.encodeToString(userpassword.getBytes());
	        conn.setRequestProperty("Authorization", "Basic "+
	              encodedAuthorization);
	        
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
	        
	        InputStream is = conn.getInputStream();
	        
	        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	        
	        String line = null;
	        while(reader.ready()) {
	            line = reader.readLine();
	       }
	
	        return line;
	    } catch (Exception e) {
	        resp.setStatus(408);
	        return null;
	    }
    }
    
    
    // Reset the cars data
    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ResponseEntity<Void> reset() {
    	isRunning = false;
    	carData.reset();
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
    // Start a new race
    @RequestMapping(value = "/start-new-game", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Void> startNewGame() {
    	isRunning = true;
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
    
    private String parseLine(String line) {
    	System.out.println("Checking line: " + line);
    	if (line == null) {
    		return "unknown";
    	}
		Matcher matcher = Pattern.compile(actionPattern).matcher(line);
		if (matcher.matches()) {
			System.out.println("actionPattern matches");
			return matcher.group(1);
		} 
		
		matcher = Pattern.compile(speedPattern).matcher(line);
		if (matcher.matches()) {
			System.out.println("speedPattern matches");
			return matcher.group(1);
		} 
		
		return "unknown";
	}

	// Remote control of individual Pis
    @GetMapping("/cali/")
    public void calibrate(HttpServletRequest req, HttpServletResponse resp, @RequestParam("action") String action, @RequestParam("car") String car) throws IOException {
        forwardRequest(car, action, req, resp);
    }
    
    @GetMapping("/run")
    public void run(HttpServletRequest req, HttpServletResponse resp, @RequestParam("action") String action, @RequestParam("car") String car) throws IOException {
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
            conn.setRequestProperty("Authorization", "Basic "+
                  encodedAuthorization);
            
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

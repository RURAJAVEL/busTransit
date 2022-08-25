package com.busservice.rest.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import com.google.gson.*;

@Controller
public class WelcomeController {

	
	public static String route;
    public static String stop;
    public static String direction;
	
    public static int time;
	public static int routeID;
	public static int directionID;
	public static String dir;
	public static String urlString;
	public static String stopID = "";
	public static String timeStamp = "";
	public static URL url = null;
	
    //@GetMapping("/welcome/bustrip")
	@RequestMapping(value="/welcome/bustrip/{route}/{stop}/{direction}")
    @ResponseBody 
    public String gettime(@PathVariable("route") String route,@PathVariable("stop") String stop,@PathVariable("direction") String direction) {   
    	
		String Output;
	
        
		//Set the direction to the expected output to correctly match the JSON element
		if(direction.equals("north"))
		{
			dir = "NORTHBOUND";
		}
		else if(direction.equals("south"))
		{
			dir = "SOUTHBOUND";
		}
		else if(direction.equals("east"))
		{
			dir = "EASTBOUND";
		}
		else if (direction.equals("west"))
		{
			dir = "WESTBOUND";
		}
		else
		{
			//Bad Input
			Output = "The direction that was entered was invalid";
			return Output;
			//System.exit(-1);
		}
        		
        //Find the route ID for the specific route that was entered and return the ID
        urlString = "http://svc.metrotransit.org/NexTrip/Routes?format=json";
		routeID = FetchInformation(urlString, "Description", "Route", route);
		
		//error checking to make sure we should continue
        if(routeID == -1)
        {
        	Output = route + " was not found.";
            return Output;
        }
        
        //Find the direction ID for the specific direction that was entered and return the ID
        urlString = "http://svc.metrotransit.org/NexTrip/Directions/"+ routeID + "?format=json";
		directionID = FetchInformation(urlString, "Text", "Value", dir);
        
		//error checking to make sure we should continue
        if(directionID == -1)
        {
        	Output = direction + " is incorrect for this route.";
        	return Output;
        }    
        
        //Find the stop ID String and set in global variable
        urlString = "http://svc.metrotransit.org/NexTrip/Stops/" + routeID + "/"+ directionID +"?format=json";
        FetchInformation(urlString, "Text", "Value", stop);
        
        //error checking to make sure we should continue
        if(stopID.equals(""))
        {
        	Output = stop + " was not found.";
            return Output;
        }
        
        //Find the timeStamp String and set in global variable
        urlString = "http://svc.metrotransit.org/NexTrip/" + routeID + "/"+ directionID + "/" + stopID +"?format=json";
        FetchInformation(urlString, "RouteDirection", "DepartureTime", timeStamp);
        
        //error checking to make sure we should continue
        if(timeStamp.equals(""))
	    {
	    	//The specification says that if the last bus of the day has already left to not return anything. So I exit clean here.
        	Output = "";
        	return Output;
	    }
		
		
      //get the portion of the timeStamp we need and convert it to a long
      		timeStamp = timeStamp.substring(6,19);
      		Long longTime = Long.valueOf(timeStamp).longValue();
      		Date currentDate = new Date();

      		//take the difference between the current time and the departure time (longTime). Divide by 60000 to account for milliseconds and minutes
      		long timeTillBus = (longTime-currentDate.getTime())/60000;
      		if(timeTillBus > 1)
      		{
      			Output = timeTillBus + " minutes";
      			return Output;
      		}
      		else if (timeTillBus == 0)
      		{
      			Output = "Bus is on the stop";
      			return Output;
      		}
      		else
      		{
      			Output = timeTillBus + " minute";
      			return Output;
      		}
		
    
    	
    	
    	
    	
    //	return Output;
    }
    
    
  
    
    
    public static int FetchInformation(String Url, String ElementOne, String ElementTwo, String compareString)
	{
		//set up a connection to get ready to send a GET request to the url that is passed in
		HttpURLConnection request;
		try
		{
			url = new URL(Url);
		    request = (HttpURLConnection)url.openConnection();
		    request.setDoOutput(true);
		    request.setRequestMethod("GET");
		    
		    //we will recieve a Json Array and extract the element from it
		    request.connect();
		    JsonParser jp = new JsonParser();
		    JsonElement element = jp.parse(new InputStreamReader((InputStream)request.getInputStream()));
		    
		    //check to make sure our data will be valid before parsing commences
		    if (request.getResponseCode() != HttpURLConnection.HTTP_OK)
		    {
		    	System.out.println(request.getErrorStream());
		    }

		    JsonArray jsonArrayObj = element.getAsJsonArray();
		    
		    //parse the elements in the array and return either an int or a string depending on input
		    for (JsonElement obj : jsonArrayObj)
		    {
		    	if(obj.getAsJsonObject().get(ElementOne).getAsString().contains(compareString))
		    	{
		    		if(compareString.equals(stop))
		    		{
		    	        stopID = obj.getAsJsonObject().get(ElementTwo).getAsString();
			    		return 0;
		    		}
		    		if(compareString.equals(timeStamp))
		    		{
		    	        timeStamp = obj.getAsJsonObject().get(ElementTwo).getAsString();
			    		return 0;
		    		}
		    		return obj.getAsJsonObject().get(ElementTwo).getAsInt();
		    	}
			}
		}
		catch(IOException e)
		{
			System.out.println("Caused an IOException");
			e.printStackTrace();
		}
		return -1;
	}
    
    
    
    
    
    
}
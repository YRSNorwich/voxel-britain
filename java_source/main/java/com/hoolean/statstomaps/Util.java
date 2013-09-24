package com.hoolean.statstomaps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;

import uk.me.jstott.jcoord.LatLng;
import uk.me.jstott.jcoord.OSRef;

/**
 * A class with miscellaneous methods to cut down on repeated code in other classes.
 * @author Harry
 *
 */
public class Util {

	public static Random random = new Random(); // a Random object for use in all classes
	
	/**
	 * Downloads a file from the internet and returns as a String.
	 * @param path that the file is at on the internet
	 * @return the file in String form
	 * @throws IOException if not connected to the Internet or the requested path is not accessible/present
	 */
	public static String downloadFile(String path) throws IOException {

		return read(new URL(path).openStream());
				
	}
	
	
	/**
	 * Gets a resource from the JAR file and returns as a String.
	 * @param path that file is at in the package.
	 * @return the file in String form
	 */
	public static String getResource(String path) {
		
		return read(Util.class.getResourceAsStream("/"+path));
		
	}
	
	/**
	 * Reads from a requested Stream and returns as a String what has been read.
	 * For use in shortening methods in this and other classes.
	 * @param stream to read from
	 * @return the read String
	 */
	public static String read(InputStream stream) {
	
		Scanner scanner = new Scanner(stream);
		StringBuilder sb = new StringBuilder();
		while(scanner.hasNextLine()) {
			
			sb.append(scanner.nextLine()+"\n");
			
		}
		return sb.toString().trim();
		
	}
	
	/**
	 * Converts latitude/longitude reference points to the position on the tileMap they correspond to.
	 * @param lat ...itude
	 * @param lng ...itude
	 * @param tileMap that they should correspond with
	 * @return the coordinates that it corresponds to, in the form {x, y}
	 */
	public static int[] latitudeToGrid(double lat, double lng, Tile[][] tileMap) {
		
		LatLng loc = new LatLng(lat, lng);
		loc.toOSGB36();
		OSRef ref = loc.toOSRef();
		return new int[] {(int) (ref.getEasting()/1000), (int) (tileMap[0].length-(ref.getNorthing()/1000))};
		
	}

}

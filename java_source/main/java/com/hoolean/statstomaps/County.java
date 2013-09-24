package com.hoolean.statstomaps;

import java.awt.Point;
import java.io.Serializable;

public class County implements Serializable {

	/**
	 * Serial ID to help with serialisation.
	 */
	private static final long serialVersionUID = 5911466762511774235L;
	
	/**
	 * The name of the county
	 */
	public final String name;
	
	/**
	 * The id of the county
	 */
	public final byte id;
	
	/**
	 * The primary initializer.
	 * @param name of county
	 * @param lat ...itude of county
	 * @param lng ...itude of county
	 * @param id of the county
	 */
	public County(String name, double lat, double lng, byte id) {
		
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.id = id;
		
	}
	
	/**
	 * Wind speed of county
	 */
	public double windSpeed;
	/**
	 * Temperature of county
	 */
	public double temperature;
	/**
	 * Cloud cover of county
	 */
	public double cloudCover;
	/**
	 * Visibility in county
	 */
	public double visibility;
	/**
	 * Intensity of rain; 0 means no rain
	 */
	public double precipIntensity;
	/**
	 * Population density of county
	 */
	public short populationDensity = 0;
	
	/**
	 * Amount of crime incidents that have happened in the county divided by the population density
	 */
	public double crimeIncidents = 0;
	
	
	/**
	 * Latitude of the center of the county
	 */
	public final double lat;
	/**
	 * Longitude of the center of the county
	 */
	public final double lng;
	
	/**
	 * Gets the location of the center of the county as a Point object
	 * @param tileMap the county is in
	 * @return the Point that represents the center of the county
	 */
	public Point getGridPoint(Tile[][] tileMap) {
		
		int[] coords = Util.latitudeToGrid(lat, lng, tileMap);
		return new Point(coords[0], coords[1]);
		
	}
	
}

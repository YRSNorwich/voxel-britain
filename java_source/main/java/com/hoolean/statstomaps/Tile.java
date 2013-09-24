package com.hoolean.statstomaps;

public class Tile {

	/**
	 * A sea tile
	 */
	public transient static final byte TYPE_SEA = 0;
	
	/**
	 * A blank grass tile
	 */
	public transient static final byte TYPE_LAND_UNPOPULATED = 1;
	
	/**
	 * A farm tile
	 */
	public transient static final byte TYPE_LAND_FARM = 2;
	
	/**
	 * A tile that is part of a village
	 */
	public transient static final byte TYPE_LAND_VILLAGE = 3;
	
	/**
	 * A tile that is part of a town
	 */
	public transient static final byte TYPE_LAND_TOWN = 4;
	
	/**
	 * A tile that is part of a city
	 */
	public transient static final byte TYPE_LAND_CITY = 5;
	
	
	/**
	 * The County the tile is part of
	 */
	public County county;
	
	/** 
	 * The type the County object is
	 */
	public byte type = 0;
	
	public transient int chunkX = -1;
	public transient int chunkY = -1;
	
}
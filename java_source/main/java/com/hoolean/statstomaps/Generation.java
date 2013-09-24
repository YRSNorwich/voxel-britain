package com.hoolean.statstomaps;

import java.awt.Point;

public class Generation {
	
	/**
	 * Generates a city structure on the tileMap.
	 * @param tileMap to generate city on
	 * @param point to generate city at
	 * @param rating of population density to choose how big city is
	 */
	public static void generateCity(Tile[][] tileMap, Point point, int rating) {

		for(int x = point.x - (3*(rating+1)); x < point.x + 3; x++) {

			for(int y = point.y - (3*(rating+1)); y < point.y + 4; y++) {

				Tile tile = tileMap[x][y];
				if(tile.type > Tile.TYPE_SEA) {

					tile.type = Tile.TYPE_LAND_CITY;

				}

			}

		}

	}
	
	/**
	 * Generates a village stucture on the tileMap.
	 * @param tileMap to generate village on
	 * @param point to generate village at
	 */
	public static void generateVillage(Tile[][] tileMap, Point point) {

		for(int x = point.x - 1; x < point.x + 1; x++) {

			for(int y = point.y - 2; y < point.y + 1; y++) {

				Tile tile = tileMap[x][y];
				if(tile.type == Tile.TYPE_LAND_UNPOPULATED) {

					tile.type = Tile.TYPE_LAND_VILLAGE;

				}

			}

		}

	}

	/**
	 * Generates a town stucture on the tileMap.
	 * @param tileMap to generate town on
	 * @param point to generate town at
	 */
	public static void generateTown(Tile[][] tileMap, Point point) {

		for(int x = point.x - 2; x < point.x + 2; x++) {

			for(int y = point.y - 2; y < point.y + 3; y++) {

				Tile tile = tileMap[x][y];
				if(tile.type == Tile.TYPE_LAND_UNPOPULATED) {

					tile.type = Tile.TYPE_LAND_TOWN;

				}

			}

		}

	}
	
	
	/**
	 * Generates a farm stucture on the tileMap.
	 * @param tileMap to generate farm on
	 * @param point to generate farm at
	 */
	public static void generateFarm(Tile[][] tileMap, Point point) {
		
		int x = point.x;
		do {
			Tile tile = tileMap[x][point.y];
			if(tile.type == Tile.TYPE_LAND_UNPOPULATED) {
				
				tile.type = Tile.TYPE_LAND_FARM;
				
			}
		} while(Util.random.nextBoolean());
		
	}
	
}

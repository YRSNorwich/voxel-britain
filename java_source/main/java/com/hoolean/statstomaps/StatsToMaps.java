package com.hoolean.statstomaps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StatsToMaps {

	/**
	 * The path where all File I/O is done
	 */
	public static File path;

	/**
	 * Whether or not to dump an image showing displaying the city data in a
	 * visual form
	 */
	private static boolean dumpImage = false;

	private static final String FORECAST_IO_API_TEMPLATE = "https://api.forecast.io/forecast/1247025787501563ed8e2444d8e35987/%s,%s";
	private static final String POLICE_UK_API_TEMPLATE = "http://data.police.uk/api/crimes-street/all-crime?lat=%s&lng=%s";
	private static final int CHUNK_WIDTH = 14;
	private static final int CHUNK_HEIGHT = 20;

	private final Date date = new Date();

	private long lastTime;

	private ArrayList<County> counties;

	private Tile[][] tileMap;

	/**
	 * The method that is called when the JAR file is executed.
	 * 
	 * @param args
	 *            that StatsToMaps should know to function correctly. In this
	 *            case the path for creating files in is required.
	 * @throws Exception
	 *             if something goes wrong - hopefully not :P
	 */
	public static void main(String[] args) throws Exception {

		if (args.length > 0) {

			path = new File(args[0]);
			if (!path.isDirectory()) {

				System.out.println("Path specified is not a directory.");
				System.exit(1);

			}
			if (!path.exists()) {

				System.out.println("Path specified does not exist.");
				System.exit(1);

			}

			if (args.length > 1) {

				dumpImage = args[1].equalsIgnoreCase("dumpimage");

			}

		} else {

			System.out
					.println("Please specify a path to create directories in.");
			System.exit(1);

		}

		new StatsToMaps().start();

	}

	/**
	 * Starts the process of gathering and exporting data.
	 * 
	 * @throws Exception
	 *             when something goes wrong :P
	 */
	public void start() throws Exception {

		verbStarted("Download");

		String binaryMap = loadBinaryMap();

		verbDone("Download");

		verbStarted("Convert");

		tileMap = toTileMap(binaryMap);

		verbDone("Convert");

		verbStarted("Populat", "weather and crime");

		populateAndLoadCounties();

		verbDone("Populat");

		verbStarted("Spread", "county data");

		spreadCountyData();

		verbDone("Spread");

		verbStarted("Sett", "tile type");

		setTileType();

		verbDone("Sett");

		if (dumpImage) {

			verbStarted("Dump", "image");

			dumpImage();

			verbDone("Dump");

		}

		verbStarted("Export", "chunks");

		export();

		verbDone("Export");

	}

	/**
	 * Loads a binary representation of a map of Britain, where 1 represents
	 * land and 0 represents sea. Access github repositery to do so.
	 * 
	 * @return file as string
	 * @throws MalformedURLException
	 *             if the URL is wrong, which it is not. :P
	 * @throws IOException
	 *             if the internet cannot be accessed.
	 */
	public String loadBinaryMap() throws MalformedURLException, IOException {

		return Util.getResource("britain.txt");

	}

	/**
	 * Converts string representation of binary map to a 2D Tile Array.
	 * 
	 * @param binaryMap
	 *            to convert
	 * @return the 2D Tile Array
	 */
	public Tile[][] toTileMap(String binaryMap) {

		// the width/height of the array
		final int width = binaryMap.split("\n")[0].length();
		final int height = binaryMap.split("\n").length;

		// the TileMap to populate
		Tile[][] tileMap = new Tile[width][height];

		int x = 0;
		int y = 0;

		for (int i = 0; i < binaryMap.length(); i++) {

			char character = binaryMap.charAt(i);
			switch (character) {
			case '1':// is true, should populate land
			{
				Tile tile = new Tile();
				tile.type = Tile.TYPE_LAND_UNPOPULATED;
				tileMap[width - (x + 1)][y] = tile;
				x++;
				break;
			}
			case '0': {
				Tile tile = new Tile();
				tile.type = Tile.TYPE_SEA;
				tileMap[width - (x + 1)][y] = tile;
				x++;
				break;
			}
			case '\n':
				y++;
				x = 0;
				break;
			default:
				break;
			}

		}

		return tileMap;

	}

	/**
	 * Collects weather data and applies to tileMap.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	public void populateAndLoadCounties() throws FileNotFoundException,
			IOException, ClassNotFoundException, JSONException,
			InterruptedException {

		String dateString = new SimpleDateFormat("dd_MM_yy").format(date);
		File dateFile = new File(path, "weather_data/weather_" + dateString
				+ ".txt");
		counties = null;
		if (dateFile.exists()) {

			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					dateFile));
			Object obj = ois.readObject();
			ois.close();
			if (obj instanceof ArrayList) {

				counties = (ArrayList<County>) obj;
				System.out.print(" Read file...");

			} else {

				dateFile.delete();
				System.out.print(" Corrupt file...");

			}

		}

		if (!dateFile.exists()) { // this looks like bad code but in this case,
			// it has a purpose

			counties = new ArrayList<County>();

			Scanner scanner = new Scanner(getClass().getResourceAsStream(
					"/counties.txt"));

			System.out.print(" Gathering Data...");

			byte i = 0;
			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();
				String[] lineParts = line.split(";");
				String countyName = lineParts[0];
				double lat = Double.parseDouble(lineParts[1]);
				double lng = Double.parseDouble(lineParts[2]);
				short populationDensity = Short.parseShort(lineParts[3]);

				System.out.print(" Loading " + countyName + "...");

				// weather IO
				JSONObject weatherJson = new JSONObject(
						Util.downloadFile(String.format(
								FORECAST_IO_API_TEMPLATE, lat, lng)))
						.getJSONObject("currently");
				County county = new County(countyName, lat, lng, i);
				county.cloudCover = weatherJson.getDouble("cloudCover");
				county.temperature = weatherJson.getDouble("temperature");
				county.visibility = weatherJson.getDouble("visibility");
				county.windSpeed = weatherJson.getDouble("windSpeed");
				county.precipIntensity = weatherJson
						.getDouble("precipIntensity");
				county.populationDensity = populationDensity;

				JSONArray crimeJson = new JSONArray(Util.downloadFile(String
						.format(POLICE_UK_API_TEMPLATE, lat, lng)));
				county.crimeIncidents = Double.valueOf(crimeJson.length())
						/ county.populationDensity;
				counties.add(county);

				i++;

			}

			File parentDir;
			if (!(parentDir = new File(path, "weather_data/")).exists()) {

				parentDir.mkdirs();

			}

			dateFile.createNewFile();

			System.out.print(" Write Data...");

			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(dateFile));
			oos.writeObject(counties);
			oos.close();

		}

		for (County county : counties) {

			try {

				int[] coords = Util.latitudeToGrid(county.lat, county.lng,
						tileMap);

				tileMap[coords[0]][coords[1]].county = county;
			} catch (Exception e) {

				System.out.printf(" %s avoided...", county.name);

			}

		}

	}

	/**
	 * Enlarges counties to ensure every tile that is not part of the sea
	 * belongs to a county.
	 */
	public void spreadCountyData() {

		HashMap<County, Point> loc = new HashMap<County, Point>();
		for (int x = 0; x < tileMap.length; x++) {

			for (int y = 0; y < tileMap[0].length; y++) {

				Tile tile = tileMap[x][y];
				if (tile.county != null) {

					loc.put(tile.county, new Point(x, y));

				}

			}

		}
		for (int x = 0; x < tileMap.length; x++) {

			for (int y = 0; y < tileMap[0].length; y++) {

				Tile tile = tileMap[x][y];

				if (tile.county == null && tile.type != Tile.TYPE_SEA) {

					Point tilePoint = new Point(x, y);

					int min = Integer.MAX_VALUE;
					County county = null;

					for (County loopingCounty : loc.keySet()) {

						Point countyPoint = loc.get(loopingCounty);
						int distance = (int) Math.round(countyPoint
								.distanceSq(tilePoint));

						if (distance < min) {

							min = distance;
							county = loopingCounty;

						}

					}

					tile.county = county;
				}

			}

		}

	}

	/**
	 * Populates them map with cities, towns, villages and farms.
	 */
	public void setTileType() {

		for (County county : counties) {

			Point point = county.getGridPoint(tileMap);
			int populationRating = (int) Math
					.floor(county.populationDensity / 1000.0);
			Generation.generateCity(tileMap, point, populationRating);
			if (populationRating == 0) {

				for (int i = 0; i < 15; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateVillage(tileMap, randPoint);

				}
				for (int i = 0; i < 7; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateTown(tileMap, randPoint);

				}
				for (int i = 0; i < 10; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateFarm(tileMap, randPoint);

				}

			} else if (populationRating == 1) {

				for (int i = 0; i < 30; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateTown(tileMap, randPoint);

				}

				for (int i = 0; i < 2; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateCity(tileMap, randPoint, 0);

				}

			} else if (populationRating == 2) {

				for (int i = 0; i < 20; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateTown(tileMap, randPoint);

				}

				for (int i = 0; i < 7; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateCity(tileMap, randPoint, 0);

				}

			} else if (populationRating == 3) {

				for (int i = 0; i < 10; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateTown(tileMap, randPoint);

				}

				for (int i = 0; i < 3; i++) {

					Point randPoint = new Point(point.x
							+ (Util.random.nextInt(60) - 30), point.y
							+ Util.random.nextInt(60) - 30);
					Generation.generateCity(tileMap, randPoint, 1);

				}

			}

		}

	}

	/**
	 * Dumps an image of the map for debug purposes.
	 * 
	 * @throws IOException
	 *             when file IO does not work.
	 */
	public void dumpImage() throws IOException {

		BufferedImage image = new BufferedImage(tileMap.length,
				tileMap[0].length, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		for (int x = 0; x < tileMap.length; x++) {

			for (int y = 0; y < tileMap[0].length; y++) {

				byte tileType = tileMap[x][y].type;
				if (tileType == Tile.TYPE_SEA) {

					g.setColor(Color.CYAN);

				} else if (tileType == Tile.TYPE_LAND_UNPOPULATED) {

					g.setColor(Color.GREEN);

				} else {

					g.setColor(Color.YELLOW);

				}
				g.fillRect(x, y, 1, 1);

			}

		}
		g.dispose();
		ImageIO.write(image, "PNG", new File(path, "dumped_map_image.png"));

	}

	/**
	 * Exports all data so the client can access it to the path specified as an
	 * argument to the main class.
	 * 
	 * @throws JSONException
	 *             if JSON creation goes wrong
	 * @throws IOException
	 *             if file I/O messes up
	 * @throws IllegalArgumentException
	 *             is the wrong arguments are pushed into a method
	 * @throws IllegalAccessException
	 *             if the field of County can't be accessed
	 */
	public void export() throws JSONException, IOException,
			IllegalArgumentException, IllegalAccessException {

		ArrayList<Tile[][]> chunks = new ArrayList<Tile[][]>();

		for (int x = 0; x < tileMap.length; x += CHUNK_WIDTH) {

			for (int y = 0; y < tileMap[0].length; y += CHUNK_HEIGHT) {

				Tile[][] chunk = new Tile[CHUNK_WIDTH][CHUNK_HEIGHT];
				for (int xx = x; xx < x + CHUNK_WIDTH; xx++) {

					for (int yy = y; yy < y + CHUNK_HEIGHT; yy++) {

						chunk[xx - x][yy - y] = tileMap[xx][yy];

					}

				}
				chunk[0][0].chunkX = x;
				chunk[0][0].chunkY = y;
				chunks.add(chunk);

			}

		}

		File dataDirectory = new File(path, "exported_data/");
		if (dataDirectory.exists()) {

			dataDirectory.delete();

		}
		File chunkDirectory = new File(dataDirectory, "chunks/");
		chunkDirectory.mkdirs();

		for (Tile[][] chunk : chunks) {

			byte[][][] simplifiedChunk = new byte[chunk.length][chunk[0].length][2];
			for (int x = 0; x < chunk.length; x++) {

				for (int y = 0; y < chunk[0].length; y++) {

					simplifiedChunk[x][y][0] = chunk[x][y].type;
					simplifiedChunk[x][y][1] = chunk[x][y].type == Tile.TYPE_SEA ? -1
							: chunk[x][y].county.id;

				}

			}
			JSONObject jsonified = new JSONObject();
			jsonified.put("chunk", simplifiedChunk);
			File file = new File(chunkDirectory, String.format(
					"chunk_%s_%s.json", chunk[0][0].chunkX, chunk[0][0].chunkY));
			file.createNewFile();
			PrintWriter pw = new PrintWriter(file);
			pw.print(jsonified.toString());
			pw.close();

		}

		JSONObject countiesJson = new JSONObject();
		for (County county : counties) {

			JSONObject countyJson = new JSONObject();
			for (Field field : county.getClass().getDeclaredFields()) {

				if (!Modifier.isPrivate(field.getModifiers())) {

					countyJson.put(field.getName(), field.get(county));

				}

			}
			countiesJson.put(county.name, countyJson);

		}

		File countiesFile = new File(dataDirectory, "counties.json");
		countiesFile.createNewFile();
		PrintWriter pw = new PrintWriter(countiesFile);
		pw.print(countiesJson.toString());
		pw.close();

	}

	/**
	 * Debugs messages to show starting of action. Prevents code repetition in
	 * debugging.
	 * 
	 * @param verb
	 *            to show action beginning
	 */
	private void verbStarted(String... verb) {

		StringBuilder sb = new StringBuilder(verb[0] + "ing");
		for (int i = 1; i < verb.length; i++) {
			sb.append(" " + verb[i]);
		}
		System.out.print(sb.toString() + "...");
		lastTime = System.currentTimeMillis();

	}

	/**
	 * Debugs messages to show finishing of action. Additionally shows the time
	 * taken to complete the action. Prevents code repetition in debugging.
	 * 
	 * @param verb
	 *            to show action ending
	 */
	private void verbDone(String verb) {

		System.out.println(" " + verb + "ed. ("
				+ ((System.currentTimeMillis() - lastTime) / 1000.0)
				+ " seconds)");

	}

}

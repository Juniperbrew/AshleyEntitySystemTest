package core;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import components.Health;
import components.Position;
import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.MapObject;
import tiled.core.ObjectGroup;
import tiled.io.TMXMapReader;

import java.util.*;

public class WorldLoader {

	public WorldLoader(){

	}

	public static WorldData loadWorld(String startingMapName){
		String resFolderPath = (System.getProperty("user.dir") + "\\res\\");

		HashMap<String,Map> allMaps = new HashMap<String,Map>();
		Engine engine = new Engine();
		WorldData worldData = new WorldData(engine, allMaps);

		System.out.println("#Loading world from: " + resFolderPath);
		System.out.println("#Loading mainmap: " + startingMapName);
		allMaps.put(startingMapName, loadMap(resFolderPath, startingMapName));

		String subMapsString = allMaps.get(startingMapName).getProperties().getProperty("SubMaps");
		if(subMapsString != null){
			String[] subMaps = subMapsString.split(" ");
			for(String subMapName : subMaps){
				System.out.println("#Loading submap: " + subMapName);
				allMaps.put(subMapName,loadMap(resFolderPath, subMapName));
			}
		}

		for(String mapName : allMaps.keySet()){
			loadObjects(worldData, mapName);
		}
		worldData.printEntities();
		worldData.allMaps = allMaps;
		worldData.engine = engine;
		return worldData;
	}
	
	private static void loadObjects(WorldData worldData, String mapName){

		System.out.println("#Loading objects in map: " + mapName);
		ObjectGroup objectLayer = null;


		Vector<MapLayer> mapLayers = worldData.allMaps.get(mapName).getLayers();
		for(MapLayer layer: mapLayers){

			Properties layerProperties = layer.getProperties();
			System.out.println("Checking layers: " + layer.getName());
			if(layer instanceof ObjectGroup){
				objectLayer = (ObjectGroup) layer;
				System.out.println("#Found object layer#");
			}
			System.out.println(layer.getClass());
			layerProperties.list(System.out);
		}
		System.out.println();

		Iterator<MapObject> objIterator = objectLayer.getObjects();
		int objectCount = 0;
		int collissionCount = 0;
		int entityCount = 0;
		int messageCount = 0;
		int teleportCount = 0;
		int spawnCount = 0;
		int exitCount = 0;
		int noTypeCount = 0;

		System.out.println("##Listing non collission objects##");
		while(objIterator.hasNext()){
			objectCount++;
			MapObject obj = objIterator.next();
			if(obj.getType().equalsIgnoreCase("collission")){
				collissionCount++;
				//System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				//obj.getProperties().list(System.out);
			}else if(obj.getType().equalsIgnoreCase("entity")){
				entityCount++;
				createEntity(worldData.engine, mapName, obj);
			}else if(obj.getType().equalsIgnoreCase("message")){
				messageCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println();
			}else if(obj.getType().equalsIgnoreCase("teleport")){
				teleportCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println();
			}else if(obj.getType().equalsIgnoreCase("spawn")){
				spawnCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println();
			}else if(obj.getType().equalsIgnoreCase("exit")){
				exitCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println();
				//System.out.println("Found exit leading to map: " + obj.getProperties().getProperty("exit") + " Location: " + obj.getProperties().getProperty("location"));
			}else if(obj.getType().equalsIgnoreCase("")){
				noTypeCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println(obj.getImageSource());
				System.out.println(obj.getImage(1));
				System.out.println();
			}
		}
		System.out.println("Collission count: " + collissionCount);
		System.out.println("Entity count: " + entityCount);
		System.out.println("Message count: " + messageCount);
		System.out.println("Teleport count: " + teleportCount);
		System.out.println("Spawn count: " + spawnCount);
		System.out.println("Exit count: " + exitCount);
		System.out.println("Objects without type: " + noTypeCount);
		System.out.println("Supported objects: " + (collissionCount+entityCount+messageCount+teleportCount+spawnCount+exitCount+noTypeCount));
		System.out.println("Total object count: " + objectCount);
		System.out.println();
	}

	private static void createEntity(Engine engine, String mapName, MapObject obj){
		System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
		Properties entityProperties = obj.getProperties();
		entityProperties.list(System.out);
		System.out.println();

		Entity newEntity = new Entity();
		newEntity.add(new Position(obj.getX(), obj.getY()));

		if(entityProperties.containsKey("health")){
			int health = Integer.parseInt(entityProperties.getProperty("health"));
			newEntity.add(new Health(health));
		}

		engine.addEntity(newEntity);
	}
	
	private static Map loadMap(String resFolderPath, String mapName){

		System.out.println("#Loading map: " + mapName);
		TMXMapReader mapReader = new TMXMapReader();

		Map tiledMap = null;

		try {
			tiledMap = mapReader.readMap(resFolderPath + mapName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(tiledMap == null){
			return null;
		}

		String mapFileName = tiledMap.getFilename();
		int tileHeight = tiledMap.getTileHeight();
		int tileWidth = tiledMap.getTileWidth();
		int mapWidth = tiledMap.getWidth();
		int mapHeight = tiledMap.getHeight();

		Properties mapProperties = tiledMap.getProperties();

		System.out.println("Map file name: " + mapFileName);
		System.out.println("TileHeight: " + tileHeight);
		System.out.println("TileWidth: " + tileWidth);
		System.out.println("MapWidth: " + mapWidth);
		System.out.println("MapHeight: " + mapHeight);
		mapProperties.list(System.out);
		System.out.println();
		
		return tiledMap;
	}

	//For testing
	public static void main(String args[]){
		WorldLoader.loadWorld("untitled.tmx");
	}
}
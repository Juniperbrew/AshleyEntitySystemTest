package core;

import com.badlogic.ashley.core.Engine;
import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.core.MapObject;
import tiled.core.ObjectGroup;
import tiled.io.TMXMapReader;

import java.io.File;
import java.util.*;

public class WorldLoader {

	public static WorldData loadWorld(String startingMapName){
		String resFolderPath = (System.getProperty("user.dir") + File.separator + "res" + File.separator);

		HashMap<String,Map> allMaps = new HashMap<>();

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

		WorldData worldData = new WorldData(allMaps);
		for(String mapName : allMaps.keySet()){
			printMapObjects(worldData, mapName);
		}
		worldData.createEntitiesFromObjects();
		worldData.assignTargets();
		worldData.printEntities();
		return worldData;
	}

	private static Map loadMap(String resFolderPath, String mapName){

		System.out.println("#Loading map: " + mapName);
		TMXMapReader mapReader = new TMXMapReader();

		Map tiledMap = null;

		try {
			tiledMap = mapReader.readMap(resFolderPath + mapName);
		} catch (Exception e) {
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

	private static void printMapObjects(WorldData worldData, String mapName){

		System.out.println("#Printing objects in map: " + mapName);
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
		int collisionCount = 0;
		int entityCount = 0;
		int messageCount = 0;
		int teleportCount = 0;
		int spawnCount = 0;
		int exitCount = 0;
		int noTypeCount = 0;

		System.out.println("##Listing non collision objects##");
		while(objIterator.hasNext()){
			objectCount++;
			MapObject obj = objIterator.next();
			if(obj.getType().equalsIgnoreCase("collision")){
				collisionCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
			}else if(obj.getType().equalsIgnoreCase("entity")){
				entityCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println();
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
			}else if(obj.getType().equalsIgnoreCase("")){
				noTypeCount++;
				System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
				obj.getProperties().list(System.out);
				System.out.println(obj.getImageSource());
				System.out.println(obj.getImage(1));
				System.out.println();
			}
		}
		System.out.println("Collision count: " + collisionCount);
		System.out.println("Entity count: " + entityCount);
		System.out.println("Message count: " + messageCount);
		System.out.println("Teleport count: " + teleportCount);
		System.out.println("Spawn count: " + spawnCount);
		System.out.println("Exit count: " + exitCount);
		System.out.println("Objects without type: " + noTypeCount);
		System.out.println("Unsupported objects: " + (objectCount-(collisionCount+entityCount+messageCount+teleportCount+spawnCount+exitCount+noTypeCount)));
		System.out.println("Total object count: " + objectCount);
		System.out.println();
	}

	//For testing
	public static void main(String args[]){
		WorldLoader.loadWorld("untitled.tmx");
	}
}
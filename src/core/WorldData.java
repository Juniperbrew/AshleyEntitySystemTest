package core;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.utils.ImmutableArray;
import tiled.core.Map;
import util.EntityToString;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Litude on 7.1.2015.
 */
public class WorldData implements EntityListener {

    public Engine engine;
    public HashMap<String,Map> allMaps;
	//public Vector<Entity> entities;
    public HashMap<String,Vector<Entity>> entitiesInMap;

    public WorldData(Engine engine, HashMap<String, Map> allMaps){
        this.engine = engine;
        engine.addEntityListener(this);
        this.allMaps = allMaps;
        entitiesInMap = new HashMap<String,Vector<Entity>>();
    }

    public Vector<Entity> getEntitiesInMap(String mapName){
        return entitiesInMap.get(mapName);
    }

    public void printEntities(){
        for(String mapName : entitiesInMap.keySet()){
            System.out.println(mapName + " contains following entities: ");
            for(Entity entity : entitiesInMap.get(mapName)){
                System.out.print(entity + " ");
                for(Component component : entity.getComponents()){
                    System.out.print(component.getClass() + " ");
                }
                System.out.println();
            }
        }
    }

    public Vector<String> getEntitiesAsString(){
        Vector<String> entitiesAsString = new Vector<String>();
        for(String mapName : entitiesInMap.keySet()) {
            for (Entity e : entitiesInMap.get(mapName)) {
                entitiesAsString.add(EntityToString.convert(e));
            }
        }
        return entitiesAsString;
    }

	@Override
	public void entityAdded(Entity entity) {
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Added " + entity + " to map " + map);
        if(entitiesInMap.get(map) == null){
            Vector<Entity> entities = new Vector<Entity>();
            entities.add(entity);
            entitiesInMap.put(map,entities);
        }else{
            entitiesInMap.get(map).add(entity);
        }
		//entities.add(entity);
		//entitiesAsString.add(EntityToString.convert(entity));
    }

	@Override
	public void entityRemoved(Entity entity) {
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Removed " + entity + " from map " + map);

        if(entitiesInMap.get(map) == null){
            System.out.println("Tried to remove entity from a map that isn't listed this shouldnt happen");
        }else{
            entitiesInMap.get(map).remove(entity);
        }
		//entities.remove(entity);
		//entitiesAsString.remove(EntityToString.convert(entity));
	}
}

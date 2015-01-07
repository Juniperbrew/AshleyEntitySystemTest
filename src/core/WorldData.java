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
	public Vector<Entity> entities;
	//public Vector<String> entitiesAsString;

    public WorldData(Engine engine, HashMap<String, Map> allMaps){
        this.engine = engine;
        engine.addEntityListener(this);
        this.allMaps = allMaps;
		entities = new Vector<Entity>();

    }

    public void printEntities(){
        for(String mapName : allMaps.keySet()){
            System.out.println(mapName + " contains following entities: ");
            for(Entity entity : entities){
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
        for(Entity e : entities){
            entitiesAsString.add(EntityToString.convert(e));
        }
        return entitiesAsString;
    }

	@Override
	public void entityAdded(Entity entity) {
        System.out.println("Added: " + entity);
		entities.add(entity);
		//entitiesAsString.add(EntityToString.convert(entity));
    }

	@Override
	public void entityRemoved(Entity entity) {
        System.out.println("Removed: " + entity);
		entities.remove(entity);
		//entitiesAsString.remove(EntityToString.convert(entity));
	}
}

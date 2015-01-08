package core;

import com.badlogic.ashley.core.*;
import components.*;
import systems.AIRandomMovementSystem;
import tiled.core.Map;
import tiled.core.MapObject;
import util.EntityToString;

import java.util.*;

public class WorldData implements EntityListener {

    private Engine engine;
    public HashMap<String,Map> allMaps;
	public Vector<Long> entityIDs;
    public HashMap<String,Vector<Entity>> entitiesInMaps;

    private long networkIDCounter;

    public WorldData(Engine engine, HashMap<String, Map> allMaps){
        this.engine = engine;
        engine.addEntityListener(this);
        this.allMaps = allMaps;
        entitiesInMaps = new HashMap<String,Vector<Entity>>();
        entityIDs = new Vector<Long>();

        engine.addSystem(new AIRandomMovementSystem(Family.all(Position.class).get()));
    }

    public int getEntityCount(){
        return entityIDs.size();
    }

    public Vector<Entity> getEntitiesInMap(String mapName){
        return entitiesInMaps.get(mapName);
    }

    public void printEntities(){
        for(String mapName : entitiesInMaps.keySet()){
            System.out.println(mapName + " contains following entities: ");
            for(Entity entity : entitiesInMaps.get(mapName)){
                System.out.print(entity + " ");
                for(Component component : entity.getComponents()){
                    System.out.print(component.getClass() + " ");
                }
                System.out.println();
            }
        }
    }

    public Entity getEntityWithID(long id){
        for(Vector<Entity> entitiesInMap : entitiesInMaps.values()){
            for(Entity e : entitiesInMap){
                if(Mappers.idM.get(e).id == id){
                    return e;
                }
            }
        }
        return null;
    }

    public Vector<String> getEntitiesAsString(){
        Vector<String> entitiesAsString = new Vector<String>();
        for(String mapName : entitiesInMaps.keySet()) {
            for (Entity e : entitiesInMaps.get(mapName)) {
                entitiesAsString.add(EntityToString.convert(e));
            }
        }
        return entitiesAsString;
    }

    public void updateEntities(HashMap<Long,Component[]> updatedEntities){

        Set<Long> updateIDList = updatedEntities.keySet();

        //Loop through all entities sent in update
        for(long id : updateIDList){
            //The changed local entity
            Entity e = getEntityWithID(id);
            //Get the new components
            Component[] updatedComponents = updatedEntities.get(id);
            if(e == null) {
                //If the entity doesnt exist in local store we add it
                Entity newEntity = new Entity();
                //Add new components
                for(Component updatedComponent : updatedComponents){
                    newEntity.add(updatedComponent);
                }
                addEntity(newEntity);
            }else{
                //remove all components
                e.removeAll();
                //Add new components
                for(Component updatedComponent : updatedComponents){
                    e.add(updatedComponent);
                }
            }
        }

        //Remove all entities not sent in update
        //Copy id list to avoid ConcurrentModificationException caused by calls to entityRemoved(Entity e)
        //FIXME is this a good idea
        Vector<Long> idListCopy = new Vector<>();
        for(long id : entityIDs){
            idListCopy.add(id);
        }

        for(long id : idListCopy){
            if (updateIDList.contains(id)) {
                //This entity was sent in update
            } else {
                //We dont need to remove ID here since it's removed in the call to listeners entityRemoved()
                //iter.remove();
                engine.removeEntity(getEntityWithID(id));

                System.out.println("Removed one entity from local list, this should never happen on server");
            }
        }

    }

    protected void createEntity(String mapName, MapObject obj){
        System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
        Properties entityProperties = obj.getProperties();
        entityProperties.list(System.out);
        System.out.println();

        Entity newEntity = new Entity();
        newEntity.add(new Name(obj.getName()));
        newEntity.add(new MapName(mapName));
        newEntity.add(new Position(obj.getX(), obj.getY()));

        if(entityProperties.containsKey("health")){
            int health = Integer.parseInt(entityProperties.getProperty("health"));
            newEntity.add(new Health(health));
        }


        addEntity(newEntity);
    }

    public void addEntity(Entity e){
        //If entity doesnt have a networkID we give it one
        if(e.getComponent(NetworkID.class) == null){
            e.add(new NetworkID(networkIDCounter));
            networkIDCounter++;
            System.out.println("Giving entity a network ID, this should never be called on client");
        }
        engine.addEntity(e);
    }

    public void removeAllEntities(){
        engine.removeAllEntities();
        networkIDCounter = 0;

        entityIDs.clear();
        entitiesInMaps.clear();

    }

    public void updateWorld(float deltaTime){
        engine.update(deltaTime);
    }

	@Override
	public void entityAdded(Entity entity) {
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Added " + entity + " to map " + map);
        entityIDs.add(Mappers.idM.get(entity).id);

        if(entitiesInMaps.get(map) == null){
            Vector<Entity> entities = new Vector<Entity>();
            entities.add(entity);
            entitiesInMaps.put(map,entities);
        }else{
            entitiesInMaps.get(map).add(entity);
        }
    }

	@Override
	public void entityRemoved(Entity entity) {
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Removed " + entity + " from map " + map);
        entityIDs.remove(Mappers.idM.get(entity).id);

        if(entitiesInMaps.get(map) == null){
            System.out.println("Tried to remove entity from a map that isn't listed this shouldnt happen");
        }else{
            entitiesInMaps.get(map).remove(entity);
        }
	}
}

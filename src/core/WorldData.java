package core;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.utils.ImmutableArray;
import components.server.Destination;
import components.server.Movement;
import components.server.Target;
import components.shared.*;
import systems.ListeningEntitySystem;
import tiled.core.*;
import tiled.core.Map;
import util.EntityToString;
import util.MapMask;

import java.util.*;

public class WorldData implements EntityListener {

    public HashMap<String,Map> allMaps;
    public HashMap<Long,Entity> entityIDs;
    public HashMap<String,Vector<Entity>> entitiesInMaps;
    private HashMap<String,Engine> allEngines;
    private HashMap<String,LinkedList<MapObject>> objectsInMaps;

    private HashMap<Long,String> tempTargetStorage;

    private long networkIDCounter;

    public WorldData(HashMap<String, Map> allMaps){
        allEngines = new HashMap<>();
        for(String mapName : allMaps.keySet()){
            Engine engine = new Engine();
            engine.addEntityListener(this);
            allEngines.put(mapName,engine);
        }
        this.allMaps = allMaps;
        entitiesInMaps = new HashMap<>();
        entityIDs = new HashMap<>();
        objectsInMaps = new HashMap<>();
        tempTargetStorage = new HashMap<>();
        for(String mapName:allMaps.keySet()){
            objectsInMaps.put(mapName, loadObjectsFromMap(mapName));
        }
    }

    public boolean hasMap(String mapName){
        return allMaps.keySet().contains(mapName);
    }

    public Set<String> getAllMapNames(){
        return allMaps.keySet();
    }

    public MapMask getMapMask(String mapName, String property){

        Vector<TileLayer> layers = new Vector<TileLayer>();
        allMaps.get(mapName).getLayers();
        for ( MapLayer rawLayer : allMaps.get(mapName).getLayers() )
        {
            if(rawLayer instanceof TileLayer){
                layers.add((TileLayer) rawLayer);
            }
        }
        int width = layers.get(0).getWidth();
        int height = layers.get(0).getHeight();

        return new MapMask(height,width,layers,property);
    }

    public void toggleAllSystemsOnMap(String mapName, boolean enabled){
        for(EntitySystem system:allEngines.get(mapName).getSystems()){
            system.setProcessing(enabled);
        }
        System.out.println((enabled?"Enabled ":"Disabled ")+" all systems on map "+mapName);
    }

    public void toggleSystemOnMap(int index, String mapName){
        EntitySystem system = allEngines.get(mapName).getSystems().get(index);
        if(system == null){
            System.out.println("No system with index "+index+" in map "+mapName);
            return;
        }
        system.setProcessing(!system.checkProcessing());
        System.out.println(system+" is now "+(system.checkProcessing()?" enabled.":" disabled."));
    }

    public LinkedList<MapObject> getObjectsFromMap(String mapName){
        return objectsInMaps.get(mapName);
    }

    private LinkedList<MapObject> loadObjectsFromMap(String mapName){

        ObjectGroup objectLayer = null;

        Map map = allMaps.get(mapName);
        Vector<MapLayer> mapLayers = map.getLayers();
        int mapHeightPixels = map.getHeight()*map.getTileHeight();

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
        LinkedList<MapObject> mapObjects = new LinkedList<>();
        Iterator<MapObject> iter = objectLayer.getObjects();
        while(iter.hasNext()){
            MapObject obj = iter.next();
            //Invert all Y coordinates -.-
            int invertedY = mapHeightPixels-obj.getY()-obj.getHeight();
            obj.setY(invertedY);
            mapObjects.add(obj);
        }
        return mapObjects;
    }

    public void addSystemToMap(EntitySystem system, String mapName){
        allEngines.get(mapName).addSystem(system);
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
                System.out.println(EntityToString.convert(entity));
            }
        }
    }

    public Entity getEntityWithID(long id){
        return entityIDs.get(id);
    }

    public Vector<String> getEntitiesAsString(){
        Vector<String> entitiesAsString = new Vector<>();
        for(String mapName : entitiesInMaps.keySet()) {
            for (Entity e : entitiesInMaps.get(mapName)) {
                entitiesAsString.add(EntityToString.convert(e));
            }
        }
        return entitiesAsString;
    }


    public void updateEntitiesInAllMaps(HashMap<Long,Component[]> updatedEntities){
        System.out.println("Updating all maps");
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
                addEntity(newEntity, Mappers.mapM.get(newEntity).map);
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
        for(long id : entityIDs.keySet()){
            idListCopy.add(id);
        }

        for(long id : idListCopy){
            if (updateIDList.contains(id)) {
                //This entity was sent in update
            } else {
                //We dont need to remove ID here since it's removed in the call to listeners entityRemoved()
                //iter.remove();
                allEngines.get(Mappers.mapM.get(getEntityWithID(id)).map).removeEntity(getEntityWithID(id));

                System.out.println("Removed one entity from local list, this should never happen on server");
            }
        }
    }

    public void assignTargets(){
        if(tempTargetStorage != null){
            for(long id:tempTargetStorage.keySet()){
                Entity entity = entityIDs.get(id);
                String target = tempTargetStorage.get(id);
                for(Entity e:entityIDs.values()){
                    if(Mappers.nameM.get(e).name.equals(target)){
                        entity.add(new Target(e));
                        System.out.println(Mappers.nameM.get(entity).name+" target is now "+Mappers.nameM.get(e).name);
                    }
                }
            }
        }
        tempTargetStorage = null;
    }

    protected void createEntitiesFromObjects(){
        for(String mapName:objectsInMaps.keySet()){
            LinkedList<MapObject> objects = objectsInMaps.get(mapName);
            for(MapObject obj:objects){
                if(obj.getType().equalsIgnoreCase("entity")) {
                    Entity newEntity = new Entity();
                    long networkID = getNextNetworkID();
                    newEntity.add(new NetworkID(networkID));
                    Tile tile = obj.getTile();
                    if (tile != null) {
                        int tileID = obj.getTile().getId();
                        newEntity.add(new TileID(tileID));
                        newEntity.add(new Bounds(Global.TILE_SIZE, Global.TILE_SIZE));
                        System.out.println("Name: " + obj.getName() + " Type: " + obj.getType() + " TileID: " + tileID);
                    } else {
                        System.out.println("Name: " + obj.getName() + " Type: " + obj.getType());
                        newEntity.add(new Bounds(obj.getWidth(), obj.getHeight()));
                    }

                    Properties entityProperties = obj.getProperties();
                    entityProperties.list(System.out);
                    System.out.println();

                    newEntity.add(new Name(obj.getName()));
                    newEntity.add(new MapName(mapName));
                    newEntity.add(new Position(obj.getX(), obj.getY()));

                    if (entityProperties.getProperty("AI") != null) {
                        String ai = entityProperties.getProperty("AI");
                        if (ai.equals("move")) {
                            newEntity.add(new Destination(obj.getX(), obj.getY()));
                        } else if (ai.equals("follow")) {
                            String target = entityProperties.getProperty("target");
                            if (target != null) {
                                tempTargetStorage.put(networkID, target);
                            }
                        }
                    }

                    if (entityProperties.containsKey("health")) {
                        int health = Integer.parseInt(entityProperties.getProperty("health"));
                        newEntity.add(new Health(health));
                    }
                    addEntity(newEntity, mapName);
                }
            }
        }
    }

    public long getNextNetworkID(){
        long nextID = networkIDCounter;
        networkIDCounter++;
        System.out.println("Assigning networkID: " + nextID);
        return nextID;
    }

    public void addEntity(Entity e, String mapName){
        //If entity doesnt have a networkID we give it one
        if(e.getComponent(NetworkID.class) == null){
            e.add(new NetworkID(getNextNetworkID()));
            System.out.println("Giving entity " + Mappers.nameM.get(e).name + " a network ID, this should never be called on client");
        }
        //If entity doesn't have a Movement component we give it one
        //FIXME This component will be needed on both server and client for collision handling but the data will not be shared so it's probably best to assign these separately in both also there might be a better place where to add it
        if(Mappers.movementM.get(e) == null){
            e.add(new Movement());
        }
        allEngines.get(mapName).addEntity(e);
    }
    public void removeEntity(Entity e, String mapName){
        allEngines.get(mapName).removeEntity(e);
    }

    public void removeAllEntities(){
        for(Engine engine:allEngines.values()){
            engine.removeAllEntities();
        }
        networkIDCounter = 0;

        entityIDs.clear();
        entitiesInMaps.clear();

    }

    public void updateWorld(float deltaTime, String mapName){
        allEngines.get(mapName).update(deltaTime);
    }

    public void updateAllWorlds(float deltaTime){
        for(Engine engine:allEngines.values()){
            engine.update(deltaTime);
        }
    }

    public void addFamilyListenerToEngine(Family family, EntityListener listener, String mapName){
        allEngines.get(mapName).addEntityListener(family,listener);
    }

    public void dumpData(){

        System.out.println("-----------------------------------------");
        for(String mapName : allEngines.keySet()){
            Engine engine = allEngines.get(mapName);
            System.out.println("#"+mapName+": "+engine);
            ImmutableArray<EntitySystem> systems = engine.getSystems();
            for (int i = 0; i < systems.size(); i++) {
                EntitySystem system = systems.get(i);
                System.out.println("  "+i+"#"+system+": "+system.checkProcessing());
                if(system instanceof ListeningEntitySystem){
                    ListeningEntitySystem s = (ListeningEntitySystem) system;
                    for(Entity e:s.getEntities()){
                        System.out.println("    >"+EntityToString.convert(e));
                    }
                }
            }
        }
        System.out.println();
        for(String mapName:entitiesInMaps.keySet()){
            Vector<Entity> entities = entitiesInMaps.get(mapName);
            System.out.println("Map:"+mapName);
            for(Entity e:entities){
                System.out.println(">"+EntityToString.convert(e));
            }
        }
        System.out.println("-----------------------------------------");
    }

	@Override
	public void entityAdded(Entity entity) {
        //FIXME i probably want separate listeners for each engine
        String map = Mappers.mapM.get(entity).map;
        System.out.println("Added " + EntityToString.convert(entity) + " to map " + map);

        entityIDs.put(Mappers.networkidM.get(entity).id, entity);

        if(entitiesInMaps.get(map) == null){
            Vector<Entity> entities = new Vector<>();
            entities.add(entity);
            entitiesInMaps.put(map,entities);
        }else{
            entitiesInMaps.get(map).add(entity);
        }
    }

	@Override
	public void entityRemoved(Entity entity) {
        //FIXME i probably want separate listeners for each engine
        //The target component might have a reference to this removed entity
        //FIXME feels awkward to do this check here maybe the target component should be on the actual target instead of on the entity following
        for(Engine engine:allEngines.values()) {
            ImmutableArray<Entity> entitiesWithATarget = engine.getEntitiesFor(Family.all(Target.class).get());
            for (Entity e : entitiesWithATarget) {
                if (Mappers.targetM.get(e).target.equals(entity)) {
                    System.out.println("Removed target " + entity.getComponent(Name.class).name + " from " + Mappers.nameM.get(e).name);
                    e.remove(Target.class);
                }
            }
        }

        String map = Mappers.mapM.get(entity).map;
        System.out.println("Removed " + EntityToString.convert(entity) + " from map " + map);

        //Remove from id list
        entityIDs.remove(Mappers.networkidM.get(entity).id);

        if(entitiesInMaps.get(map) == null){
            System.out.println("Tried to remove entity from a map that isn't listed this shouldnt happen");
        }else{
            entitiesInMaps.get(map).remove(entity);
        }
	}
}

package core;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.utils.ImmutableArray;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import components.MapName;
import components.Name;
import components.NetworkID;
import components.Position;
import gui.TestAbstract;
import network.Network;
import network.Network.Message;
import network.Network.SyncEntities;
import network.Network.Spawn;
import network.Network.GoToMap;
import systems.AIRandomMovementSystem;

import javax.swing.*;

public class ServerFrame extends TestAbstract<String>{

	Server server;

	int syncsSent = 0;
	int syncsSentPerSecond = 0;
	int syncsSentPerSecondCounter = 0;

	boolean autoSync = true;

	final int writeBufferSize = 16384; //default 16384
	final int objectBufferSize = 4096; //default 2048

	//##GAME DATA
	WorldData worldData;
	HashMap<Connection,Entity> playerList;

	//Key is mapname, value is vector containing all entities in that map
	//HashMap<String,Vector<Entity>> entities;

	public ServerFrame(){
		super();
		infoFrame.setTitle("Server");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		infoFrame.setLocation(dim.width/2, 0);
	}

	private void updateSpecificInfo(){
		int entityCount = worldData.getEntityCount();
		StringBuilder info = new StringBuilder("Entities: " + entityCount + " Syncs sent: " + syncsSent + " Syncs/s: " + syncsSentPerSecond);
		Connection[] connections = server.getConnections();
		for(Connection connection : connections){
			info.append(" " + connection + ": " + connection.getTcpWriteBufferSize() + "B/" + writeBufferSize + "B " + connection.getReturnTripTime() + "ms ");
		}
		infoFrame.setSpecificInfoText(info.toString());
	}

	private void startServer(){
		try {

			//Default constructor creates a Server with a write buffer size of 16384 and an object buffer size of 2048. 
			server = new Server();
			server = new Server(writeBufferSize,objectBufferSize);

			Network.register(server);
			server.start();
			server.bind(Network.port);


			server.addListener(new Listener(){
				public void received (Connection connection, Object object) {
					if (object instanceof Message) {
						Message request = (Message)object;
						System.out.println(request.text);
						infoFrame.addLogLine("Received message: " + request.text);

						if(request.text.equalsIgnoreCase("ping")){
							Message response = new Message();
							response.text = "Pong";
							connection.sendTCP(response);
							infoFrame.addLogLine("Sent message: " + response.text);
						}

						if(request.text.equalsIgnoreCase("sync")){

							SyncEntities sync = new SyncEntities();
							sync.entities = createSyncPacket(connection);

							connection.sendTCP(sync);
							infoFrame.addLogLine("Sent server status");
						}
					}else if (object instanceof Spawn) {
						infoFrame.addLogLine("Spawn");
						Spawn spawn = (Spawn) object;
						connection.setName(spawn.name);
						infoFrame.addLogLine("Spawning player " + spawn.name + " in " + spawn.mapName);

						Entity newPlayer = new Entity();;
						newPlayer.add(new Name(spawn.name));
						newPlayer.add(new MapName(spawn.mapName));
						newPlayer.add(new Position(322,322));
						worldData.addEntity(newPlayer);

						playerList.put(connection, newPlayer);
					}else if (object instanceof GoToMap) {
						infoFrame.addLogLine("GoToMap");
						GoToMap goToMap = (GoToMap) object;
						Entity player = playerList.get(connection);
						if(player == null){
							infoFrame.addLogLine(connection + " is trying to change map without owning an entity");
							return;
						}
						MapName mapComponent = Mappers.mapM.get(player);

						infoFrame.addLogLine("Moving player " + Mappers.nameM.get(player).name + " to " + goToMap.mapName);

						//First remove player from old map
						worldData.entitiesInMaps.get(mapComponent.map).remove(player);

						//Make sure the entity knows what map it is in
						mapComponent.map = goToMap.mapName;

						//Finally add the player to the maps entity list
						//If player moves to a map with no entities we create a new entity list for that map
						if(worldData.entitiesInMaps.get(mapComponent.map) == null){
							Vector<Entity> entitiesInMap = new Vector<Entity>();
							entitiesInMap.add(player);
							worldData.entitiesInMaps.put(mapComponent.map, entitiesInMap);
						}else{
							worldData.entitiesInMaps.get(mapComponent.map).add(player);
						}
					}
				}
			});

		}catch (IOException e) {
			infoFrame.addExceptionLine(e.getMessage());
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i = 0; i < stackTrace.length;i++){
				infoFrame.addExceptionLine(stackTrace[i].toString());
			}
			e.printStackTrace();
		}
	}

	@Override
	protected void parseCommand(String input){
		//Parse common commands for client and server
		super.parseCommand(input);

		//If input is not a command we send it as a message to all clients
		if(input.charAt(0) != '!'){
			Message request = new Message();
			request.text = input;
			infoFrame.addLogLine("Sent message: " + request.text);
			server.sendToAllTCP(request);
			//Parse server commands
		}else{
			String cleanCommand = input.substring(1);
			Scanner scn = new Scanner(cleanCommand);

			String command = scn.next();

			if(command.equals("autosync")){
				commandParsed = true;
				try{
					autoSync = scn.nextBoolean();
					infoFrame.addLogLine("Autosync is now " + (autoSync ? "enabled." : "disabled."));
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument needs to be boolean");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("autosync command needs 1 argument");
				}
			}else if(command.equals("add")){
				commandParsed = true;
				try{
					Entity newEntity = new Entity();
					newEntity.add(new Name(scn.next()));
					newEntity.add(new MapName((scn.next())));
					newEntity.add(new Position(scn.nextFloat(),scn.nextFloat()));
					worldData.addEntity(newEntity);
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument 3 and 4 need to be floats");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("add command needs 4 arguments");
				}
			}else if(command.equals("addbulk")){
				commandParsed = true;
				try{
					int amount = scn.nextInt();
					String mapName = "null";
					if(scn.hasNext()){
						mapName = scn.next();
					}
					Name nameComponent = new Name("Bulk");
					MapName mapNameComponent = new MapName(mapName);
					Position positionComponent = new Position(0,0);
					for(int i = 0; i<amount;i++){
						Entity newEntity = new Entity();
						newEntity.add(nameComponent);
						newEntity.add(mapNameComponent);
						newEntity.add(positionComponent);
						worldData.addEntity(newEntity);
					}
				}catch(InputMismatchException e){
					infoFrame.addLogLine("first argument needs to be an integer, second a String");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("addbulk command needs 1 or 2 arguments");
				}
			}else if(command.equals("loadworld")){
				commandParsed = true;
				try{
					String mapName = scn.next();
					loadWorld(mapName);
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument needs to be a String");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("loadworld command needs 1 argument (untitled.tmx)");
				}
			}else if(command.equals("clear")) {
				commandParsed = true;
				clearEntities();
			}else{
				if(commandParsed){
					commandParsed = false;
				}else{
					infoFrame.addLogLine("Invalid command: " + command);
				}
			}
			scn.close();
		}
	}

	private void clearEntities(){
		//Remove entities from worldData
		if(worldData != null)
		worldData.removeAllEntities();

		//Empty list mapping connections to player entities
		playerList.clear();

		//Send all clients an empty sync update
		SyncEntities sync = new SyncEntities();
		sync.entities = new HashMap<Long,Component[]>();
		server.sendToAllTCP(sync);

		infoFrame.addLogLine("Entities cleared");
	}

	private HashMap<Long,Component[]> createSyncPacket(Connection connection){
		Entity player = playerList.get(connection);
		Vector<Entity> entitiesInPlayersMap = worldData.getEntitiesInMap(Mappers.mapM.get(player).map);
		HashMap<Long,Component[]> entitiesAsComponents = new HashMap<>();

		for(Entity e : entitiesInPlayersMap){
			long id = Mappers.idM.get(e).id;
			ImmutableArray<Component> components = e.getComponents();
			Component[] componentsArray = components.toArray(Component.class);
			entitiesAsComponents.put(id,componentsArray);
		}
		return entitiesAsComponents;
	}

	@Override
	protected void doLogic() {

		//Auto sync clients	
		if(autoSync){
			for(Connection connection : playerList.keySet()){
				SyncEntities sync = new SyncEntities();
				sync.entities = createSyncPacket(connection);
				connection.sendTCP(sync);
				//infoFrame.addLogLine("Sent entity status to " + Mappers.nameM.get(playerList.get(connection)).name);
			}

			syncsSent++;
			syncsSentPerSecondCounter++;
		}
		worldData.updateWorld(1);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				infoFrame.setListItems(worldData.getEntitiesAsString());

				updateSpecificInfo();
			}
			});

		}

	private void loadWorld(String mapName){

		clearEntities();
		worldData = WorldLoader.loadWorld(mapName);
	}

	@Override
	protected void initialize() {
		startServer();
		playerList = new HashMap<Connection,Entity>();
		loadWorld("untitled.tmx");
	}

	public static void main(String[] args){
		new ServerFrame();
	}

	@Override
	protected void oneSecondElapsed() {
		syncsSentPerSecond = syncsSentPerSecondCounter;
		syncsSentPerSecondCounter = 0;
	}
}


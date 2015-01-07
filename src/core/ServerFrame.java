package core;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import components.MapName;
import components.Name;
import components.Position;
import gui.TestAbstract;
import network.Network;
import network.Network.Message;
import network.Network.SyncEntities;
import network.Network.Spawn;
import network.Network.GoToMap;
import network.Network.MoveTo;
import systems.AIRandomMovementSystem;

public class ServerFrame extends TestAbstract<String> implements EntityListener {

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
	HashMap<String,Vector<Entity>> entities;

	public ServerFrame(){
		super();
		infoFrame.setTitle("Server");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		infoFrame.setLocation(dim.width/2, 0);
	}

	private void updateSpecificInfo(){
		StringBuilder info = new StringBuilder("Syncs sent: " + syncsSent + " Syncs/s: " + syncsSentPerSecond);
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
							//Vector<Entity> entitiesInPlayersMap = Mappers.entities.get(playerList.get(connection));
							sync.entities = worldData.entities;
							connection.sendTCP(sync);
							infoFrame.addLogLine("Sent server status");
						}
					}else if (object instanceof Spawn) {
						Spawn spawn = (Spawn) object;
						connection.setName(spawn.name);
						infoFrame.addLogLine("Spawning player " + spawn.name + " in " + spawn.mapName);

						Entity newPlayer = new Entity();
						newPlayer.add(new Name(spawn.name));
						newPlayer.add(new MapName(spawn.mapName));
						newPlayer.add(new Position(322,322));
						worldData.engine.addEntity(newPlayer);

						/*addEntity(spawn.mapName, newPlayer);*/

						playerList.put(connection, newPlayer);
					}else if (object instanceof GoToMap) {
						GoToMap goToMap = (GoToMap) object;
						Entity player = playerList.get(connection);

						//First remove player from old map
						//entities.get(player.map).remove(player);

						//Make sure the entity knows what map it is in
						//player.map = goToMap.mapName;

						//Finally add the player to the maps entity list
						//If player moves to a map with no entities we create a new entity list for that map
						/*if(entities.get(player.map) == null){
							Vector<Entity> entitiesInMap = new Vector<Entity>();
							entitiesInMap.add(player);
							entities.put(player.map, entitiesInMap);
						}else{
							entities.get(player.map).add(player);
						}*/

						//infoFrame.addLogLine("Moving player " + player.name + " to " + goToMap.mapName);
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
			}else if(command.equals("add")){/*
				commandParsed = true;
				try{
					Entity newEntity = new Entity();
					newEntity.name = scn.next();
					newEntity.map = scn.next();
					newEntity.x = scn.nextFloat();
					newEntity.y = scn.nextFloat();
					addEntity(newEntity.map, newEntity);
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
					for(int i = 0; i<amount;i++){
						addEntity(mapName, new Entity("Bulk",mapName,0,0,true));
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
			}else if(command.equals("clear")){
				commandParsed = true;
				clearEntities();
				infoFrame.addLogLine("Entities cleared");
				*/
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
/*
	private void addEntity( String map,Entity entity){
		Vector<Entity> entitiesInMap = entities.get(map);
		if(entitiesInMap == null){
			entitiesInMap = new Vector<Entity>();
			entitiesInMap.add(entity);
			entities.put(map, entitiesInMap);
		}else{
			entitiesInMap.add(entity);
		}
		infoFrame.addListItem(entity);
		infoFrame.addLogLine("Added entity " + entity.name + " to map " + map);
	}

	private void setEntities(HashMap<String,Vector<Entity>> newEntities){
		entities = newEntities;
		infoFrame.clearList();
		for(Vector<Entity> entitiesInMap : entities.values()){
			for(Entity entity : entitiesInMap){
				infoFrame.addListItem(entity);
				infoFrame.addLogLine("Added entity " + entity.name + " to map " + entity.map);
			}
		}
	}
*/

	private void clearEntities(){
		entities.clear();
	}

	@Override
	protected void doLogic() {

		/*
		//Auto sync clients	
		if(autoSync){
			for(Connection connection : playerList.keySet()){
				Entity player = playerList.get(connection);
				Vector<Entity> entitiesInPlayersMap = entities.get(player.map);
				SyncEntities sync = new SyncEntities();
				sync.entities = entitiesInPlayersMap;
				connection.sendTCP(sync);
				//infoFrame.addLogLine("Sent entity status to " + player.name);
			}

			syncsSent++;
			syncsSentPerSecondCounter++;
		}
*/
		worldData.engine.update(1);
		infoFrame.setListItems(worldData.getEntitiesAsString());

		//infoFrame.repaint();
		updateSpecificInfo();
	}
	
	private void removePlayers(){
		playerList.clear();
		SyncEntities sync = new SyncEntities();
		sync.entities = new Vector<Entity>();
		server.sendToAllTCP(sync);
	}

	@Override
	protected void initialize() {
		startServer();
		playerList = new HashMap<Connection,Entity>();
		entities = new HashMap<String,Vector<Entity>>();

		worldData = WorldLoader.loadWorld("untitled.tmx");
		worldData.engine.addSystem(new AIRandomMovementSystem(Family.all(Position.class).get()));
	}

	public static void main(String[] args){
		new ServerFrame();
	}

	@Override
	protected void oneSecondElapsed() {
		syncsSentPerSecond = syncsSentPerSecondCounter;
		syncsSentPerSecondCounter = 0;
	}

	@Override
	public void entityAdded(Entity entity) {

	}

	@Override
	public void entityRemoved(Entity entity) {

	}
}


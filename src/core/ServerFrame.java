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
import components.*;
import gui.TestAbstract;
import network.Network;
import network.Network.Message;
import network.Network.SyncEntities;
import network.Network.Spawn;
import network.Network.GoToMap;
import network.Network.*;
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
				public void connected (Connection connection) {
					sync(connection);
				}
				/** Called when the remote end is no longer connected. There is no guarantee as to what thread will invoke this method. */
				public void disconnected (Connection connection) {
					Entity player = playerList.get(connection);
					if(player != null){
						worldData.removeEntity(player);
						playerList.remove(connection);
					}
				}
				public void received (Connection connection, Object object) {
					if (object instanceof Register) {
						Register register = (Register) object;
						connection.setName(register.connectionName);
					} else if (object instanceof Message) {
						Message message = (Message)object;
						String senderName = getPlayerName(connection);

						if(message.text.equalsIgnoreCase("ping")){
							message.text = "SERVER: Pong";
							connection.sendTCP(message);
							infoFrame.addLogLine("Sent pong to " + senderName);
						}else if(message.text.equalsIgnoreCase("sync")){
							sync(connection);
							message.text = "SERVER: entities synced";
							connection.sendTCP(message);
							infoFrame.addLogLine("Sent server status to " + senderName);
						}else{
							//Normal chat message
							infoFrame.addLogLine(senderName + ": " + message.text);
							message.text = senderName + ": " + message.text;
							server.sendToAllTCP(message);
						}
					}else if (object instanceof Spawn) {
						Spawn spawn = (Spawn) object;
						infoFrame.addLogLine("Spawning player " + spawn.name + " in " + spawn.mapName);

						//We ignore the coordinates the client sent and make up our own
						int x = 322;
						int y = 322;
						long networkID = worldData.getNextNetworkID();

						Entity newPlayer = new Entity();
						newPlayer.add(new NetworkID(networkID));
						newPlayer.add(new Name(spawn.name));
						newPlayer.add(new MapName(spawn.mapName));
						newPlayer.add(new Position(x,y));
						newPlayer.add(new Player());
						worldData.addEntity(newPlayer);

						//We send the spawn packet back with the modified coordinates as confirmation to client
						spawn.x = x;
						spawn.y = x;
						spawn.networkID = networkID;

						connection.sendTCP(spawn);
						playerList.put(connection, newPlayer);

					}else if (object instanceof GoToMap) {
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
							Vector<Entity> entitiesInMap = new Vector<>();
							entitiesInMap.add(player);
							worldData.entitiesInMaps.put(mapComponent.map, entitiesInMap);
						}else{
							worldData.entitiesInMaps.get(mapComponent.map).add(player);
						}

						//Confirm to the player they changed map so they can load it
						//Just sending the same packet back there should be no other reason for a client to get a GoToMap packet
						connection.sendTCP(goToMap);
					}else if (object instanceof UpdateEntity) {
						infoFrame.addLogLine("Received UpdateEntity, not handling.");
					}
					else if (object instanceof UpdateComponent) {
						UpdateComponent updateComponent = (UpdateComponent) object;
						Entity entity = worldData.getEntityWithID(updateComponent.networkID);
						Component updatedComponent = updateComponent.component;
						if(updatedComponent instanceof Position){
							Position updatedPos = (Position) updatedComponent;
							Position entityPos = entity.getComponent(Position.class);
							entityPos.x = updatedPos.x;
							entityPos.y = updatedPos.y;
						}else{
							infoFrame.addLogLine(connection + " is trying to update unsupported component");
						}

					}
				}
			});

		}catch (IOException e) {
			infoFrame.addExceptionLine(e.getMessage());
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(StackTraceElement stack : stackTrace){
				infoFrame.addExceptionLine(stack.toString());
			}
			e.printStackTrace();
		}
	}

	@Override
	protected boolean parseCommand(String input){

		//If input is not a command we send it as a message to all clients
		if(input.charAt(0) != '!'){
			Message message = new Message();
			message.text = "SERVER: " + input;
			infoFrame.addInfoLine("Sent message: " + message.text);
			infoFrame.addLogLine(message.text);
			server.sendToAllTCP(message);
			return true;
		}else{
			//Parse common commands for client and server
			if(super.parseCommand(input)){
				return true;
			}
			String cleanCommand = input.substring(1);
			Scanner scn = new Scanner(cleanCommand);
			boolean commandParsed = false;
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
				scn.close();
			}
			return commandParsed;
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
		sync.entities = new HashMap<>();
		server.sendToAllTCP(sync);

		infoFrame.addLogLine("Entities cleared");
	}

	private HashMap<Long,Component[]> createSyncPacket(Connection connection){
		Entity player = playerList.get(connection);

		MapName playerMapNameComponent = Mappers.mapM.get(player);

		Vector<Entity> entitiesInPlayersMap = worldData.getEntitiesInMap(playerMapNameComponent.map);
		HashMap<Long,Component[]> entitiesAsComponents = new HashMap<>();

		for(Entity e : entitiesInPlayersMap){
			long id = Mappers.networkidM.get(e).id;
			ImmutableArray<Component> components = e.getComponents();
			Component[] componentsArray = components.toArray(Component.class);
			entitiesAsComponents.put(id,componentsArray);
		}
		return entitiesAsComponents;
	}

	private String getPlayerName(Connection connection){
		String name;
		if(playerList.get(connection) == null){
			//Player has no entity yet
			name = connection.toString();
		}else{
			name = connection.toString()+"["+Mappers.nameM.get(playerList.get(connection)).name+"]";
		}
		return name;
	}

	private void sync(Connection connection){
		//Sync playerlist
		SyncPlayerList syncPlayerList = new SyncPlayerList();
		Vector<String> playerNameList = new Vector<>();
		for(Connection c : server.getConnections()){
			playerNameList.add(getPlayerName(c));
		}
		syncPlayerList.playerList = playerNameList;
		connection.sendTCP(syncPlayerList);

		//If player has spawned we sync entities
		if(playerList.get(connection) != null) {
			//Player is not necessarily yet added to the engine so we need to check that too
			if(Mappers.playerM.get(playerList.get(connection)) != null){
				SyncEntities sync = new SyncEntities();
				sync.entities = createSyncPacket(connection);
				connection.sendTCP(sync);
			}else{
				System.out.println("Player is spawned but not yet added to engine.");
			}
		}
	}

	@Override
	protected void doLogic() {

		//Auto sync clients	
		if(autoSync){
			for(Connection connection : server.getConnections()){
				//FIXME Sync only when writebuffer is empty this removes all bufferoverflows and keeps syncrate the same, TcpIdleSender alternative solution which caused much more lag
				if(connection.getTcpWriteBufferSize() == 0){
					sync(connection);
				}else{
					infoFrame.addLogLine(connection + " has data in writebuffer, throttling autosync.");
				}
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
		worldData.addSystem(new AIRandomMovementSystem(Family.all(Position.class).get()));
	}

	@Override
	protected void initialize() {
		startServer();
		playerList = new HashMap<>();
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


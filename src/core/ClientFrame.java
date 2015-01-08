package core;

import com.badlogic.ashley.core.*;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import components.NetworkID;
import gui.TestAbstract;
import network.Network;
import network.Network.GoToMap;
import network.Network.Message;
import network.Network.Spawn;
import network.Network.SyncEntities;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class ClientFrame extends TestAbstract<String>{

	Client client;

	int syncsReceived = 0;
	int syncsReceivedPerSecond = 0;
	int syncsReceivedPerSecondCounter = 0;

	final int writeBufferSize = 8192; //Default 8192
	final int objectBufferSize = 4096; //Default 2048

	//LOCAL GAME DATA
	private WorldData worldData;

	public ClientFrame(){
		super();
		infoFrame.setTitle("Client");

		Message request = new Message();
		request.text = "Ping";
		client.sendTCP(request);
		infoFrame.addLogLine("Sent message: " + request.text);
	}

	private void updateSpecificInfo(){
		int entityCount = worldData.getEntityCount();
		infoFrame.setSpecificInfoText("Entities: " + entityCount + " Syncs received: " + syncsReceived + " Syncs/s: " + syncsReceivedPerSecond
				+ " Buffer: " + client.getTcpWriteBufferSize() + "B/" + writeBufferSize + "B Ping: " + client.getReturnTripTime() + "ms");
	}

	private void startClient(){
		try {
			//Default constructor creates a Client with a write buffer size of 8192 and an object buffer size of 2048.
			client = new Client(writeBufferSize,objectBufferSize);
			Network.register(client);
			client.start();
			client.connect(5000, "127.0.0.1", Network.port);

			client.addListener(new Listener(){
				public void received (Connection connection, Object object) {
					if (object instanceof Message) {
						Message response = (Message)object;
						System.out.println(response.text);
						infoFrame.addLogLine("Received message: " + response.text);
					}
					if (object instanceof SyncEntities) {
						final SyncEntities status = (SyncEntities)object;

						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								worldData.updateEntities(status.entities);
								//FIXME Dont update infoFrame if sync isn't changing anything
								infoFrame.setListItems(worldData.getEntitiesAsString());
							}
						});

						syncsReceived++;
						syncsReceivedPerSecondCounter++;
					}
				}
			});

		} catch (IOException e) {
			infoFrame.addLogLine(e.getMessage());
			StackTraceElement[] stackTrace = e.getStackTrace();
			for(int i = 0; i < stackTrace.length;i++){
				infoFrame.addLogLine(stackTrace[i].toString());
			}
			e.printStackTrace();
		}
	}

	@Override
	protected void parseCommand(String input){
		//Parse common commands for client and server
		super.parseCommand(input);

		//If input is not a command we send it as a message to server
		if(input.charAt(0) != '!'){
			Message request = new Message();
			request.text = input;
			client.sendTCP(request);
			infoFrame.addLogLine("Sent message: " + request.text);
			//Parse client commands
		}else{
			String cleanCommand = input.substring(1);
			Scanner scn = new Scanner(cleanCommand);

			String command = scn.next();

			if(command.equals("spawn")){
				commandParsed = true;
				try{
					Spawn spawn = new Spawn();
					spawn.name = scn.next();
					spawn.mapName = scn.next();
					client.sendTCP(spawn);
				}catch(InputMismatchException e){
					infoFrame.addLogLine("arguments need to be strings");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("spawn command needs 2 arguments");
				}
			}else if(command.equals("map")){
				commandParsed = true;
				try{
					String mapName = scn.next();
					GoToMap goToMap = new GoToMap();
					goToMap.mapName = mapName;
					infoFrame.addLogLine("Moving to " + mapName);
					client.sendTCP(goToMap);
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument need to be string");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("map command needs 1 argument");
				}

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

	@Override
	protected void doLogic() {

		//infoFrame.repaint();
		updateSpecificInfo();
	}

	@Override
	protected void initialize() {
		startClient();
		worldData = new WorldData(new Engine(), null);
	}

	public static void main(String[] args){
		new ClientFrame();
	}

	@Override
	protected void oneSecondElapsed() {
		syncsReceivedPerSecond = syncsReceivedPerSecondCounter;
		syncsReceivedPerSecondCounter = 0;

	}
}


package core;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import components.Position;
import gui.TestAbstract;
import systems.AIRandomMovementSystem;
import util.EntityToString;

import java.util.Vector;

public class FrameTest extends TestAbstract<String>{

	WorldData worldData;
	Engine engine;

	public FrameTest(){

	}

	@Override
	protected void initialize(){
		worldData = WorldLoader.loadWorld("untitled.tmx");
		engine = worldData.engine;
		engine.addSystem(new AIRandomMovementSystem(Family.all(Position.class).get()));
	}

	@Override
	protected void doLogic() {

		engine.update(1);
		infoFrame.setListItems(worldData.getEntitiesAsString());

	}

	@Override
	protected void oneSecondElapsed() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String args[]){
		new FrameTest();
	}
}

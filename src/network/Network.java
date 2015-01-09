package network;

import java.util.HashMap;
import java.util.Vector;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

public class Network {
	
    static public final int port = 54555;

    // This registers objects that are going to be sent over the network.
    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Message.class);
        kryo.register(SyncEntities.class);
        kryo.register(Spawn.class);
        kryo.register(Vector.class);
        kryo.register(MoveTo.class);
        kryo.register(GoToMap.class);

		kryo.register(com.badlogic.ashley.core.Component[].class);
		kryo.register(Component.class);
		kryo.register(com.badlogic.gdx.utils.Array.class);
		kryo.register(Object[].class);
		kryo.register(components.Name.class);
		kryo.register(components.MapName.class);
		kryo.register(components.Position.class);
		kryo.register(components.Velocity.class);
		kryo.register(components.Health.class);
		kryo.register(components.NetworkID.class);
		kryo.register(components.Player.class);
		kryo.register(java.util.HashMap.class);


    }
	
	static public class Message{
		public String text;
	}
	
	static public class SyncEntities{
		public HashMap<Long,Component[]> entities;
	}
	
	static public class MoveTo{
		public int x;
		public int y;
	}
	
	static public class GoToMap{
		public String mapName;
	}
	
	static public class Spawn{
		public String name;
		public String mapName;
	}
}

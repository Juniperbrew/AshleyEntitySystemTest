package network;

import java.util.Vector;

import com.badlogic.ashley.core.Entity;
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
        kryo.register(Entity.class);
        kryo.register(MoveTo.class);
        kryo.register(GoToMap.class);
    }
	
	static public class Message{
		public String text;
	}
	
	static public class SyncEntities{
		public Vector<Entity> entities;
	}
	
	static public class MoveTo{
		int x;
		int y;
	}
	
	static public class GoToMap{
		String mapName;
	}
	
	static public class Spawn{
		public String name;
		public String mapName;
	}
}

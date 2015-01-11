package components;

import com.badlogic.ashley.core.Component;

/*Tile id for texture*/
public class TileID extends Component {
    public int id;

    public TileID(int id) {
        this.id = id;
    }

    public TileID() {

    }
}

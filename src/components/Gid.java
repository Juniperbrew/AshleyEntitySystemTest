package components;

import com.badlogic.ashley.core.Component;

/*Tile id for texture*/
public class Gid extends Component {
    public int gid;

    public Gid(int gid) {
        this.gid = gid;
    }

    public Gid() {

    }
}

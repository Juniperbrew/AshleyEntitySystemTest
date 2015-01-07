package components;

import com.badlogic.ashley.core.Component;

/**
 * Created by Litude on 8.1.2015.
 */
public class MapName extends Component {
    public String map;

    public MapName(String map) {
        this.map = map;
    }
}

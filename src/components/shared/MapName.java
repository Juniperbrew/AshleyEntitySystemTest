package components.shared;

import com.badlogic.ashley.core.Component;

public class MapName extends Component {
    public String map;

    public MapName(String map) {
        this.map = map;
    }

    public MapName() {
    }
}

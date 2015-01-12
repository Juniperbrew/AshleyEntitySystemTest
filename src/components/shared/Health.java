package components.shared;

import com.badlogic.ashley.core.Component;

public class Health extends Component {

    public int health;

    public Health(int health) {
        this.health = health;
    }

    public Health() {
    }
}
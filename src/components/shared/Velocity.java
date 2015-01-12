package components.shared;

import com.badlogic.ashley.core.Component;

public class Velocity extends Component {
    public float vectorX, vectorY;

    public Velocity(float vectorX, float vectorY) {
        this.vectorX = vectorX;
        this.vectorY = vectorY;
    }

    public Velocity() {
    }
}

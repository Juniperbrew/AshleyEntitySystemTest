package components.server;

import com.badlogic.ashley.core.Component;

/**
 * Created by Juniperbrew on 14.1.2015.
 */
public class Movement extends Component {
    public float deltaX;
    public float deltaY;
    public boolean syncClient;

    public Movement() {
    }

    public Movement(float deltaX, float deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }
}
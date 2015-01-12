package components.server;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

/**
 * Created by Juniperbrew on 12.1.2015.
 */
public class Target extends Component{

    public Entity target;

    public Target() {
    }

    public Target(Entity target) {

        this.target = target;
    }
}

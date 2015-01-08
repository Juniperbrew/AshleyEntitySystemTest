package components;

import com.badlogic.ashley.core.Component;

public class NetworkID extends Component {

    public long id;

    public NetworkID() {
    }

    public NetworkID(long id) {
        this.id = id;
    }
}

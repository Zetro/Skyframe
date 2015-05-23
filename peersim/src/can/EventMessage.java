package can;

public class EventMessage {

    public String type;
    public Object o;

	public EventMessage(String type, Object o) {
        this.type = type;
        this.o = o;
	}
}

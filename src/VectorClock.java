//Operating Systems Distributed System package
package osdistributedsystem;

//Need to serialize vector clock to transmit between objects
import java.io.Serializable;

//Base data structure used for the vector clock
import java.util.Vector;

public class VectorClock implements Serializable {
	private static final long serialVersionUID = 1L;
	private int id;

	public VectorClock(int id){
		this.id = id;
	}

    public int getId() {
        return this.id;
    }

	public String toString(){
		if (this == null) return null;
		return "PC"+this.id;
	}
}
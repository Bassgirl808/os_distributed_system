//Operating Systems Distributed System package
package osdistributedsystem;

//Need to serialize vector clock to transmit between objects
import java.io.Serializable;

//Base data structure used for the vector clock
import java.util.Vector;

public class VectorClock implements Serializable, Comparable<VectorClock> { 
	private static final long serialVersionUID = 1L;
	private int id;
	private Vector<Integer> timeStamp = null;

	//For debug purposes and blank comparisons
	public VectorClock() {
		this.id = 0;
		this.timeStamp = new Vector<Integer>();
		for (int i = 0; i < Constants.NUMBER_OF_CLIENTS; i++) this.timeStamp.add(0);
	}

	//Create a VectorClock for PC {id}
	public VectorClock(int id) {
		this.id = id;
		this.timeStamp = new Vector<Integer>();
		for (int i = 0; i < Constants.NUMBER_OF_CLIENTS; i++) this.timeStamp.add(0);
	}

    public int getId() {
        return this.id;
	}

	//gets the time of PC location
	public int getTime(int location) {
		return this.timeStamp.get(location);
	}

	//sets the time of PC location to time
	private void setTime(int location, int time) {
		this.timeStamp.set(location, time);
	}

	//increment time of this PC by 1
	public void increment() {
		this.setTime(this.id - 1, this.getTime(this.id - 1) + 1);
	}

	//Update timeStamp of this class by comparing to otherVectorClock and merging values
	public void merge(VectorClock otherVectorClock) {
		for (int i = 0; i < Constants.NUMBER_OF_CLIENTS; i++) this.setTime(i, Math.max(this.getTime(i), otherVectorClock.getTime(i)));
	}

	//compare my timeStamp to other timeStamp for use in determining ordering
	public int compareTo(VectorClock otherVectorClock) {
		int count = 0;
		int otherCount = 0;

		for (int i = 0; i < Constants.NUMBER_OF_CLIENTS; i++) {		
			int time = this.getTime(i), otherTime = otherVectorClock.getTime(i);
			
			if (time <= otherTime) count++;
			if (time >= otherTime) otherCount++;
		}

		//VectorClocks are parallel/equal: this||other
		if ((count == Constants.NUMBER_OF_CLIENTS && otherCount == Constants.NUMBER_OF_CLIENTS) || (count != Constants.NUMBER_OF_CLIENTS && otherCount != Constants.NUMBER_OF_CLIENTS)) return 0;
		//This VectorClock came before Other VectorClock: this->other
		else if (count == Constants.NUMBER_OF_CLIENTS) return -1;
		//This VectorClock came after Other VectorClock: other->this
		else if (otherCount == Constants.NUMBER_OF_CLIENTS) return 1;
		//No order found: this||other
		else return 0;
	}

	public String toString() {
		return String.format("[PC%d VectorClock]:%s", this.id, this.timeStamp.toString());
	}
}
package interpreter;

import java.util.ArrayList;

public class RuntimeState {
	private ArrayList<Integer> tape;
	private int pointer;
	private ArrayList<Character> inData;
	
	public RuntimeState(char[] inData) {
		this.tape = new ArrayList<>();
		this.tape.add(0);
		this.pointer = 0;
		this.inData = new ArrayList<>();
		for (char c : inData) {
			this.inData.add(c);
		}
	}
	
	public void movePointer(int offset) {
		this.pointer += offset;
		while (this.tape.size() <= this.pointer) {
			this.tape.add(0);
		}
	}
	
	public int readTape() {
		return this.tape.get(this.pointer);
	}
	
	public void changeCell(int value) {
		this.tape.set(this.pointer, this.readTape() + value);
	}
	
	public void setCell(int value) {
		this.tape.set(this.pointer, value);
	}
	
	public int readInData () {
		return (int) this.inData.remove(0);
	}
	
	public void readAndStore() {
		this.tape.set(this.pointer, this.readInData());
	}
}

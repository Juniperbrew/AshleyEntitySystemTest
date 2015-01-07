package gui;

import com.esotericsoftware.minlog.Log;

public class GUILogger extends Log.Logger{
	
	InfoFrame frame;
	
	public GUILogger(InfoFrame frame){
		this.frame = frame;
	}

	@Override
	protected void print (String message) {
		frame.addDebugLine(message);
	}
}
package gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.ListCellRenderer;

import com.esotericsoftware.minlog.Log;
import gui.InfoFrame;

public abstract class TestAbstract<E> {

	protected InfoFrame<E> infoFrame;

	
	//Controlling logic loop speed
	int sleepTime = 500;
	int tickRate = 20;
	boolean lockTickRate = true;
	private long tickStartTime;
	long deltaNano;
	double cumulativeTimingErrorMilli;
	double sleepErrorMilli;
	
	//Logging
	int loopCounter = 0;
	int loopsPerSecond = 0;
	int loopsPerSecondCounter = 0;
	long secondStartTime;
	
	//Logging levels will cause a lot of lag with gui logging and log levels above info
	final static boolean GUI_LOGGING = true;
	int minLogLevel = Log.LEVEL_INFO;

	public TestAbstract(){
		infoFrame = new InfoFrame<E>();
		infoFrame.setVisible(true);
		infoFrame.setCommandListener(new Runnable(){
			public void run(){
				if(!parseCommand(infoFrame.getCommand())){
					infoFrame.addLogLine("Invalid command");
				}
			}
		});

		initLogging();
		initialize();
		startLogicLoop();
	}
	
	public TestAbstract(ListCellRenderer<E> cellRenderer){
		this();
		infoFrame.setListCellRenderer(cellRenderer);
	}

	private void initLogging(){


		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				infoFrame.addExceptionLine(e.toString());
				e.printStackTrace();
			}

		});

		Log.set(minLogLevel);
		if(GUI_LOGGING){
			GUILogger guiLogger = new GUILogger(infoFrame);
			Log.setLogger(guiLogger);
		}

		//Redirect console output to GUI
		OutputStream out = new OutputStream() {
			@Override
			public void write(final int b) throws IOException {

				infoFrame.updateConsole(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				infoFrame.updateConsole(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}



	protected void startLogicLoop(){

		//Logic update thread
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(true){
					tickStartTime = System.nanoTime();
					loopCounter++;
					loopsPerSecondCounter++;

					//Run this once per second for logging purposes
					if(System.nanoTime() - secondStartTime > 1000000000){
						secondStartTime = System.nanoTime();
						loopsPerSecond = loopsPerSecondCounter;
						loopsPerSecondCounter = 0;

						//Allow subclasses to run their own logging every second
						oneSecondElapsed();
					}

					doLogic();
					updateInfo();

					try {
						if(lockTickRate){
							lockTickRate(tickRate);
						}else{
							Thread.sleep(sleepTime);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	private void lockTickRate(int tickRate) throws InterruptedException{
		double timePerTickNano = 1000000000f/tickRate;
		deltaNano = System.nanoTime() - tickStartTime;
		//infoFrame.addInfoLine("Target full tick duration: " + timePerTickNano/1000000f);
		//infoFrame.addInfoLine("Tick duration: " + String.valueOf(deltaNano/1000000f));
		double sleepDurationMilli = ((timePerTickNano - deltaNano)/1000000f)+cumulativeTimingErrorMilli;
		//infoFrame.addInfoLine("Sleep duration: " + sleepDurationMilli);
		
		if(sleepDurationMilli > 0){
			long sleepStart = System.nanoTime();
			long sleepMilli = (long) sleepDurationMilli;
			int sleepNano = (int) ((sleepDurationMilli-sleepMilli)*1000000);
			
			Thread.sleep(sleepMilli, sleepNano);
			double actualSleepDuration = (System.nanoTime() - sleepStart)/1000000d;
			sleepErrorMilli = sleepDurationMilli - actualSleepDuration;
			double fullTickDuration = (deltaNano/1000000f + actualSleepDuration);
			//infoFrame.addInfoLine("Full tick duration: " + fullTickDuration);
			cumulativeTimingErrorMilli += (timePerTickNano - fullTickDuration*1000000)/1000000d;
			//infoFrame.addInfoLine("Actual sleep duration: " + actualSleepDuration);
			//infoFrame.addInfoLine("Sleep error: " +! sleepErrorMilli);
			//infoFrame.addInfoLine("Time error milliseconds: " + (timePerTickNano - fullTickDuration*1000000)/1000000d);
			//infoFrame.addInfoLine("Cumulative time error milliseconds: " + cumulativeTimingErrorMilli);
		}else{
			Thread.yield();
			sleepErrorMilli = 0;
			cumulativeTimingErrorMilli = 0;
			infoFrame.addInfoLine("Sleep duration negative, yielding.");
		}
	}
	
	@SuppressWarnings("unused")
	private void lockTickRateSimple(int tickRate) throws InterruptedException{
		double timePerTickNano = 1000000000f/tickRate;
		deltaNano = System.nanoTime() - tickStartTime;
		long sleepDurationMilli = (long) ((timePerTickNano - deltaNano)/1000000f);
		if(sleepDurationMilli > 0){
			Thread.sleep(sleepDurationMilli);
		}else{
			Thread.yield();
		}
	}

	protected void updateInfo(){
		Runtime r = Runtime.getRuntime();
		int maxMemory = (int) (r.maxMemory()/1000000);
		int freeMemory = (int) (r.freeMemory()/1000000);
		int totalMemory = (int) (r.totalMemory()/1000000);

		infoFrame.setGeneralInfoText("Loops: " + loopCounter + " Thread sleep: " + sleepTime + "ms Loops/s: " + loopsPerSecond  
				+ " Max memory: " + maxMemory + " Free memory: " + freeMemory + " Total memory: " + totalMemory 
			 + " Delta: " + (deltaNano/1000.0)+ "us" );
	}

	protected boolean parseCommand(String input){

		if(input.length() == 0){
			return true;
		}
		if(input.charAt(0) == '!'){
			String cleanCommand = input.substring(1);
			Scanner scn = new Scanner(cleanCommand);

			String command = scn.next();
			if(command.equals("sleep")){
				try{
					sleepTime = scn.nextInt();
					infoFrame.addLogLine("Sleeptime is now " + sleepTime + "ms.");
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument needs to be integer");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("sleep command needs 1 argument");
				}
				return true;
			}else if(command.equals("tickrate")){
				try{
					tickRate = scn.nextInt();
					infoFrame.addLogLine("Tickrate is now " + tickRate);
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument needs to be integer");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("tickrate command needs 1 argument");
				}
				return true;
			}else if(command.equals("tick")){
				try{
					lockTickRate = scn.nextBoolean();
					infoFrame.addLogLine("Tickrate is now " + (lockTickRate ? "locked." : "unlocked."));
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument needs to be boolean");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("tick command needs 1 argument");
				}
				return true;
			}else if(command.equals("minlog")){
				try{
					String logLevel = scn.next();
					
					switch(logLevel){
					case "none": minLogLevel = Log.LEVEL_NONE; infoFrame.addLogLine("Logging level is now: " + logLevel); break;
					case "error": minLogLevel = Log.LEVEL_ERROR; infoFrame.addLogLine("Logging level is now: " + logLevel); break;
					case "warn": minLogLevel = Log.LEVEL_WARN; infoFrame.addLogLine("Logging level is now: " + logLevel); break;
					case "info": minLogLevel = Log.LEVEL_INFO; infoFrame.addLogLine("Logging level is now: " + logLevel); break;
					case "debug": minLogLevel = Log.LEVEL_DEBUG; infoFrame.addLogLine("Logging level is now: " + logLevel); break;
					case "trace": minLogLevel = Log.LEVEL_TRACE; infoFrame.addLogLine("Logging level is now: " + logLevel); break;
					default: infoFrame.addLogLine("Argument must be none, error, warn, info, debug or trace."); break;
					}
					Log.set(minLogLevel);
					
				}catch(InputMismatchException e){
					infoFrame.addLogLine("argument needs to be string");
				}catch(NoSuchElementException e){
					infoFrame.addLogLine("minlog command needs 1 argument");
				}
				return true;
			}
			scn.close();
		}
		return false;
	}
	
	/**This is called once in the constructor*/
	protected abstract void initialize();
	
	/**This is called once per tick in the main loop*/
	protected abstract void doLogic();

	/**This will be called once per second from the main loop*/
	protected abstract void oneSecondElapsed();
}

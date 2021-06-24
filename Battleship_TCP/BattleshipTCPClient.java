/*
 * BattleshipTCPClient.java
 *
 * Version:
 *     $1.0$
 *
 * Revisions:
 *     $Log$
 */


import java.net.Socket;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Two Player Battleship game
 * 
 *
 * @author      Meet Shah
 * @author      Sarvesh Kulkarni
 */

public class BattleshipTCPClient {

	//HostName
	
	private String hostName = "localhost";
	
	//Port to be connected
	
	private int port = 0;

	//BufferedReader
	
	BufferedReader readSocket;

	//PrintWriter
	
	PrintWriter writeSocket;

	//Socket for connection
	
	Socket clientSocket;
	
	//Check whether server plays first

	public boolean isServerFirst;

	//Height of the Ocean

	int oceanHeight;

	//Width of the Ocean

	int oceanWidth;

	//Total Boats in the ocean

	int totalBoatSpace;

	//Initial ocean view

	String[][] ocean;

	//Current ocean view in game 

	String[][] displayOcean; 



	/**
	 * Read file from Scanner and store in ocean Array
	 *
	 * @param    args					Name of File
	 *
	 * @return   
	 */

	private void parseArg( String args ) throws FileNotFoundException {

		Scanner ocean = new Scanner( new File( args ) );
		fillOcean( ocean );
		ocean.close();
	}


	/**
	 * Read position of elements in an ocean and store in Ocean Array
	 *
	 * @param    ocean					Scanner variable
	 *
	 * @return   
	 */

	private void fillOcean( Scanner ocean )	{
		String oceanParam;
		int oceanHeightIndex = -1;
		int oceanWidthIndex = -1;
		while ( ocean.hasNext() ) {
			oceanParam = ocean.next();
			if( oceanParam.equals("width")) {
				this.oceanWidth = ocean.nextInt();
			}
			else if(oceanParam.equals("height")){
				this.oceanHeight = ocean.nextInt();

				/**
				 * Checks if the size of ocean is greater than 0 
				 * and initializing size of ocean
				 * 
				 * 
				 */

				if(this.oceanWidth > 0 && this.oceanHeight > 0) {
					this.ocean = new String[this.oceanHeight][this.oceanWidth];
					this.displayOcean = new String[this.oceanHeight][this.oceanWidth];
					for(String[] s : displayOcean) {
						Arrays.fill(s, ".");
					}
				}
			}
			else {
				if(oceanParam.equals("row")) {
					oceanHeightIndex++;
					oceanWidthIndex = 0;
				}
				else {
					oceanWidthIndex++;
				}
				this.ocean[oceanHeightIndex][oceanWidthIndex] = oceanParam.equals("row") ? ocean.next() : oceanParam;
				totalBoatSpace += this.ocean[oceanHeightIndex][oceanWidthIndex].equals("w") ? 0 : 1;
			}

		}
	}

	/**
	 * Validate the input coordinates 
	 *
	 * @param    column					X Coordinate of hit 
	 * 
	 * @param    row					Y Coordinate of hit
	 *
	 * @return   true or false			Checks whether input coordinates are valid
	 */

	private boolean isValidCoordinate(int column, int row) {
		return ( column >= 0 && column < oceanWidth ) && ( row >= 0 && row < oceanHeight); 
	}

	/**
	 * Play first and get the input coordinates to hit 
	 *
	 * @param    oceanScanner					Scanner variable
	 *
	 * @return   
	 */

	private void inputCoordinate(Scanner oceanScanner) {
		int columnCoordinate = -1;
		int rowCoordinate = -1;
		do {
			while( !isValidCoordinate(columnCoordinate, rowCoordinate) ) {
				System.out.println("column coordinate (0 <= column <" + oceanWidth +"): " );
				columnCoordinate = oceanScanner.nextInt();
				System.out.println("row coordinate (0 <= column <" + oceanHeight +"): " );
				rowCoordinate = oceanScanner.nextInt();
			}
			sendCoordinates(columnCoordinate, rowCoordinate);
			try {
				DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
				int oceanLength = dataInputStream.readInt();
				byte[] getOcean = new byte[oceanLength];
				if(oceanLength > 0) {
					dataInputStream.read(getOcean, 0, getOcean.length);
				}
				readOcean(getOcean);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("HIT");
			String[] coordinate = readCoordinates();
			columnCoordinate = Integer.valueOf(coordinate[0].trim());
			rowCoordinate = Integer.valueOf(coordinate[1].trim());
			battleStrike(columnCoordinate, rowCoordinate);
			if( totalBoatSpace <= 0 ) {
				sendOcean(displayOcean, true);
				break;
			}
			sendOcean(displayOcean, false);

			columnCoordinate = -1;
			rowCoordinate = -1;
		}while(totalBoatSpace > 0);
		try {
			readSocket.close();
			writeSocket.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Play second and get the input coordinates to hit 
	 *
	 * @param    oceanScanner					Scanner variable
	 *
	 * @return   
	 */

	private void secondInputCoordinate(Scanner oceanScanner) {
		int columnCoordinate = -1;
		int rowCoordinate = -1;
		do {
			String[] coordinate = readCoordinates();
			columnCoordinate = Integer.valueOf(coordinate[0].trim());
			rowCoordinate = Integer.valueOf(coordinate[1].trim());
			battleStrike(columnCoordinate, rowCoordinate);
			if( totalBoatSpace <= 0 ) {
				sendOcean(displayOcean, true);
				break;
			}
			sendOcean(displayOcean, false);

			columnCoordinate = -1;
			rowCoordinate = -1;
			while( !isValidCoordinate(columnCoordinate, rowCoordinate) ) {
				System.out.println("column coordinate (0 <= column <" + oceanWidth +"): " );
				columnCoordinate = oceanScanner.nextInt();
				System.out.println("row coordinate (0 <= column <" + oceanHeight +"): " );
				rowCoordinate = oceanScanner.nextInt();
			}
			sendCoordinates(columnCoordinate, rowCoordinate);
			try {
				DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
				int oceanLength = dataInputStream.readInt();
				byte[] getOcean = new byte[oceanLength];
				if(oceanLength > 0) {
					dataInputStream.read(getOcean, 0, getOcean.length);
				}
				readOcean(getOcean);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("HIT");
		}while(totalBoatSpace > 0);
		try {
			writeSocket.close();
			readSocket.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Performs a strike and destroys a ship if it is a hit
	 *
	 * @param    column					X Coordinate of hit 
	 * 
	 * @param    row					Y Coordinate of hit 
	 *
	 * @return   
	 */

	private void battleStrike(int column, int row) {
		boolean isWater = false;
		if(ocean[row][column].equals("w")) {
			isWater = true;
			displayOcean[row][column] = "W";
		}
		for(int rowIndex = 0; rowIndex < ocean.length; rowIndex++) {
			for( int columnIndex = 0; columnIndex < ocean[rowIndex].length; columnIndex++) {
				if(ocean[row][column].equals(ocean[rowIndex][columnIndex]) && !isWater 
						&& !displayOcean[rowIndex][columnIndex].equals("X")) {
					displayOcean[rowIndex][columnIndex] = "X";
					totalBoatSpace--;
				}
			}
		}
	}

	/**
	 * Read the ocean from the other player
	 * 
	 * @param 			getOcean		Byte array of Ocean to be read
	 */

	public void readOcean(byte[] getOcean) {
		byte[] readOceanMap;
		readOceanMap = getOcean;
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(readOceanMap); 
			ObjectInputStream inputStream = new ObjectInputStream(byteStream);
			Ocean ocean = (Ocean)inputStream.readObject();
			System.out.println(ocean);
			if(ocean.isGameFinished == true) {
				System.out.println("HIT");
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send the ocean to the other player
	 * 
	 * @param 		ocean					Ocean to be displayed to other player
	 * 
	 * @param 		isGameFinished			Check whether the game is finished
	 */

	public void sendOcean(String[][] ocean, boolean isGameFinished) {
		byte[] bytes = new byte[1024];
		Ocean transferOcean = new Ocean(ocean);
		transferOcean.isGameFinished = isGameFinished;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(byteStream);
			DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			outputStream.writeObject(transferOcean);
			bytes = byteStream.toByteArray();
			dataOutputStream.writeInt(bytes.length);
			dataOutputStream.write(bytes);
			outputStream.flush();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert coordinates to be sent to other player in String
	 * 
	 * @param 		xcoordinate			X coordinate of target to hit
	 * 
	 * @param 		ycoordinate			Y coordinate of target to hit
	 * 
	 * @return		String				X and Y Coordinates
	 */

	public String getCoordinateToSend(int xcoordinate, int ycoordinate) {
		String coordinates = "";
		coordinates = Integer.toString(xcoordinate) + "," + Integer.toString(ycoordinate);
		return coordinates;
	}

	/**
	 * Coordinates to send to other player
	 * 
	 * @param 		xcoordinate			X coordinate of target to hit
	 * 	
	 * @param 		ycoordinate			Y coordinate of target to hit
	 * 
	 */

	public void sendCoordinates(int xcoordinate, int ycoordinate) {
		String coordinateToSend = getCoordinateToSend(xcoordinate, ycoordinate);
		writeSocket.println(coordinateToSend);
	}

	/**
	 * Read coordinates sent by the other player
	 * 
	 * @return		String Array		Array of X and Y coordinates
	 */

	public String[] readCoordinates() {
		String[] coordinate = null;
		String coordinates = "";
		try {
			coordinates = readSocket.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		coordinate = coordinates.split(",");
		return coordinate;
	}

	/**
	 * Checks which player will start the game
	 * 
	 * @param 		oceanScanner			Scanner variable
	 */

	public void playerSelect(Scanner oceanScanner) { 
		if(isServerFirst == false) {
			writeSocket.println("false");
			inputCoordinate(oceanScanner);
		}
		else {
			writeSocket.println("true");
			secondInputCoordinate(oceanScanner);
		}
	}

	/**
	 * Process arguments from command line
	 * 
	 * @param 		args			Input from command line
	 */
	
	public void parsePort(String args[]) {

		for (int index = 0; index < args.length; index ++) {
			if (args[index].equals("-host")) 
				hostName = args[++index];
			else if (args[index].equals("-port")) 
				port = new Integer(args[++index]).intValue();
			else if (args[index].equals("-first")) 
				isServerFirst = false;
			else if (args[index].equals("-second")) 
				isServerFirst = true;
		}
	}

	/**
	 * Set up the connection with the other player
	 * 
	 */

	private void setIO() {
		try {
			clientSocket = new Socket(hostName, port);
			writeSocket = new PrintWriter(clientSocket.getOutputStream(), true);
			readSocket = new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
		}catch(Exception e) {

		}

	}

	public static void main(String[] args) throws FileNotFoundException {
		BattleshipTCPClient client = new BattleshipTCPClient();
		client.parsePort(args);
		client.setIO();
		Scanner oceanScanner = new Scanner(System.in);
		System.out.println("% java BattleShip");
		client.parseArg(args[0]);
		client.playerSelect(oceanScanner);

	}

}

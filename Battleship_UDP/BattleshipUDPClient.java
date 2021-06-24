/*
 * BattleshipUDPClient.java
 *
 * Version:
 *     $1.0$
 *
 * Revisions:
 *     $Log$
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Two Player Battleship game
 * 
 *
 * @author      Meet Shah
 * @author      Sarvesh Kulkarni
 */

public class BattleshipUDPClient {
	
	//HostName of client
	
	private String hostName = "localhost";
	
	//client port
	
	private int port = 0;
	
	//Datagram Socket
	
	DatagramSocket socket;
	
	//Address of Client
	
	InetAddress address;

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
	 * Get the input coordinates to hit 
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
			byte[] bytes = new byte[1024];
			DatagramPacket dataPacket = new DatagramPacket(bytes, bytes.length);
			try {
				socket.receive(dataPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			readOcean(dataPacket);
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
		socket.close();
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
	 * @param 			dataPacket		Datagram Packet received from the other player
	 */

	public void readOcean(DatagramPacket dataPacket) {
		byte[] readOceanMap;
		readOceanMap = dataPacket.getData();
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
			outputStream.writeObject(transferOcean);
			bytes = byteStream.toByteArray();
			DatagramPacket dataPacket = new DatagramPacket(bytes, bytes.length, address, port);
			socket.send(dataPacket);
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
	 * @return		Byte Array			X and Y Coordinates
	 */

	public byte[] getCoordinateToSend(int xcoordinate, int ycoordinate) {
		String coordinates = "";
		coordinates = Integer.toString(xcoordinate) + "," + Integer.toString(ycoordinate);
		return coordinates.getBytes();
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
		byte[] bytes = new byte[1024];
		bytes = getCoordinateToSend(xcoordinate, ycoordinate);
		DatagramPacket dataPacket = new DatagramPacket(bytes, bytes.length, address, port);
		try {
			socket.send(dataPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read coordinates sent by the other player
	 * 
	 * @return		String Array		Array of X and Y coordinates
	 */

	public String[] readCoordinates() {
		byte[] bytes = new byte[1024];
		String coordinates = "";
		String[] coordinate = null;
		DatagramPacket dataPacket = new DatagramPacket(bytes, bytes.length);
		try {
			socket.receive(dataPacket);
			coordinates = new String(dataPacket.getData());
			coordinate = coordinates.split(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return coordinate;
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
		}
	}
	
	/**
	 * Set up the connection with the other player
	 * 
	 */

	private void setIO() {
		try {
			byte[] buf = new byte[64];
			address = InetAddress.getByName(hostName);
			socket = new DatagramSocket();
			DatagramPacket dataPacket = new DatagramPacket(buf, buf.length, address, port);
			socket.send(dataPacket);
		}catch(Exception e) {
			
		}

	}

	public static void main(String[] args) throws FileNotFoundException {
		BattleshipUDPClient client = new BattleshipUDPClient();
		client.parsePort(args);
		client.setIO();
		Scanner oceanScanner = new Scanner(System.in);
		System.out.println("% java BattleShip");
		client.parseArg(args[0]);
		client.inputCoordinate(oceanScanner);

	}

}
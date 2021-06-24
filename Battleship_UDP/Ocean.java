import java.io.Serializable;

public class Ocean implements Serializable{
	
	//Ocean to be displayed
	
	String[][] displayOcean;
	
	//Check whether game is finished
	
	public boolean isGameFinished;

	public Ocean(String[][] displayOcean) {
		this.displayOcean = displayOcean;
	}
	
	/**
	 * Display ocean after a hit 
	 *
	 *
	 * @return   
	 */

	private void displayOcean() {
		System.out.printf("   ");
		for( int index = 0; index < displayOcean[0].length; index++) {
			System.out.print(index + " ");
		}
		System.out.println();
		for(int rowIndex = 0; rowIndex < displayOcean.length; rowIndex++) {
			System.out.print(rowIndex + ": ");
			for( int columnIndex = 0; columnIndex < displayOcean[rowIndex].length; columnIndex++) {
				System.out.print(displayOcean[rowIndex][columnIndex] + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Call the method and display the ocean
	 * 
	 */
	
	public String toString() {
		displayOcean();
		return "";
	}
	public static void main(String[] args) {
		
	}
}


package WordFrequencies;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * count top 20 most frequent words in a text file
 * @author Kristen
 *
 */

public class WordFrequencies extends Application {

	/**
	 * implements UI and takes in file location
	 */
    @Override
    public void start(Stage stage) throws IOException {

    	/**
    	 * label above input text box
    	 */
        Label inputLabel = new Label("Please enter the location of the file:");
        /**
         * text field to take file location
         */
        TextField inputText = new TextField();
        /**
         * button to signal entered file name
         */
        Button enter = new Button("Enter");

        /**
         * gridPane used for UI
         */
        GridPane gridPane = new GridPane();
        gridPane.setMinSize(550, 75);
        gridPane.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.TOP_CENTER);
        gridPane.add(inputLabel, 0, 1);
        gridPane.add(inputText, 1, 1);
        gridPane.add(enter, 2, 1);

        enter.setStyle("-fx-background-color: lightgreen; -fx-textfill: white;");
        inputLabel.setStyle("-fx-font: normal bold 15px 'serif' ");
        gridPane.setStyle("-fx-background-color: beige;");

        /**
         * scene used for UI
         */
        Scene scene = new Scene(gridPane);
        stage.setTitle("Word Frequency Analyzer");
        stage.setScene(scene);
        stage.show();

        //send file name to be opened
        enter.setOnAction(enterac ->
        {
        	String inputFile = inputText.getText();
        	openFile(inputFile);
        });

    }

    /**
     * tests connection to desired database
     * @return The successful connection or null
     * @throws Exception
     */
    public static Connection getConnection() throws Exception{
		try{
				String driver = "com.mysql.cj.jdbc.Driver";
				String url = "jdbc:mysql://localhost:3308/word_occurences";
				String un = "root";
				String pass = "root";
				Class.forName(driver);

				Connection con = DriverManager.getConnection(url, un, pass);
				System.out.println("Connected");
				return con;
		} catch(Exception e){System.out.println(e);}

		return null;
	}

	/**
	 * deletes all non letter characters to prevent word count interference
	 *
	 * @param Current string taken from input file to be formatted
	 * @return string after being formatted
	 */

    public String format(String Current) {
     Current = Current.toLowerCase();

     Current = Current.replaceAll(" , ", "");
     Current = Current.replaceAll("\\,", "");
     Current = Current.replaceAll(",?", "");
     Current = Current.replaceAll("!", "");
     Current = Current.replaceAll("\\?", "");
     Current = Current.replaceAll("�", "");
     Current = Current.replaceAll(";", "");
     Current = Current.replaceAll("\\.", "");

     return Current;
    }

    /**
     * opens file and sorts words into an array list
     * to count frequency of each
     *
     * @param inputFile string containing name of file to be opened
     * @return arrayList of counted words
     */

    public void openFile(String inputFile){

    	/**
    	 * string is turned into a file variable
    	 */
    	File fileName = new File(inputFile);

    	/**
    	 * fileReader to parse through file
    	 */
        FileReader read = null;

        //opens file
        try {
            read = new FileReader(fileName);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WordFrequencies.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader buff = new BufferedReader(read);

        /**
         * hashmap to compute frequency of each word
         */
        Map<String, Integer> frequency = new HashMap<>();

        /**
         * string to split text file into lines
         */
        String Poem;

        /**
         * string array to split line into individual words
         */
        String[] Word;

        //stores all words in hashmap to keep track of frequencies
        try {
            while((Poem = buff.readLine()) != null){
                Word = Poem.split(" ");
                for (String Current : Word) {
                    Current = format(Current);
                    frequency.compute(Current, (k,v) -> (v == null) ? v = 1 : v + 1);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WordFrequencies.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
        	/**connects to database
        	 */
			Connection con = getConnection();

			/**statement for sending sql query to database
			 */
			Statement stmt = con.createStatement();

			/**string and int variables to use in sql query
			 */
			String query;
			String key;
			int val;

			//stores hashmap values in database
			for(Map.Entry<String, Integer> e: frequency.entrySet()) {
				key = e.getKey();
				val = e.getValue();
				query = "INSERT INTO word (word, count) VALUES ('"+key+"', '"+val+"')";
				stmt.executeUpdate(query);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

        print();

    }

    /**
     * prints the 20 most frequent words found
     * in the arraylist and print to screen
     *
     * @param arrayList sorted arraylist
     */
    public static void print (){

        /**
         * gridpane used in UI
         */
    	GridPane gridPane2 = new GridPane();

    	/**
         * scene used in UI
         */
        Scene secondScene = new Scene(gridPane2, 350, 350);

        /**
         * new window to show results
         */
        Stage newWindow = new Stage();

        newWindow.setTitle("Top 20 Most Frequent Words");
        newWindow.setScene(secondScene);

        newWindow.show();
        gridPane2.setMinSize(300, 300);
        gridPane2.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));
        gridPane2.setVgap(10);
        gridPane2.setHgap(75);
        gridPane2.setAlignment(Pos.TOP_CENTER);

        gridPane2.setStyle("-fx-background-color: beige;");

        /**
         * counting variables
         */
        int i = 0;
        int j = 0;
        int k = 2;

        try (
        		Connection con = getConnection();
        	    PreparedStatement statement = con.prepareStatement("SELECT * FROM word ORDER BY count DESC");
        		ResultSet results = statement.executeQuery();) {

	        		while (results.next()) {

		                String word = results.getString("word");
		                int count = results.getInt("count");
		                Label outputlabel = new Label(word + ", " + count +"\n");
	                    gridPane2.add(outputlabel, j, k);
	                    outputlabel.setStyle("-fx-font: normal bold 15px 'serif' ");

	                    if (i == 9) {
	                        j = 1;
	                        k = 2;
	                        i++;
	                    }
	                    else if (i == 19) {
	                        break;
	                            }
	                    else {
	                        k++;
	                        i++;
	                    }
	        		}

        } catch (SQLException ex) {
        	    System.out.println("Error while communicating with the database");
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
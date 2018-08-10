/* 
    WoW Log Parser
    Derek Johnson
*/
package project3;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.text.Font;
import javax.swing.JOptionPane;


public class WoWLogParser implements Initializable {

    private WoWPlayer[] playerList;
    private long biggestDamage = 0;
    private long biggestHeal = 0;
    private String[] fileNames;
    private int numFiles;
    private int totalRaiders;

    GraphicsContext gcD;
    GraphicsContext gcH;

    @FXML
    private Canvas cvsDamage;
    @FXML
    private Canvas cvsHealing;
    @FXML
    private Button btnLoadLog;
    @FXML
    private ListView<String> lvLogList;
    @FXML
    private ImageView ivLogo;

    
    // This is called when the program starts
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Load the player list
        try {
            loadPlayerList();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "loadPlayerList() call failed in initialize()", JOptionPane.ERROR_MESSAGE);
        }
  
        // Give some memory to our fileNames array
        fileNames = new String[10];
        
        
        // initialize the canvases
        gcD = cvsDamage.getGraphicsContext2D();
        gcH = cvsHealing.getGraphicsContext2D();

        // Blank them out
        gcD.setFill(Color.rgb(0, 0, 0));
        gcH.setFill(Color.rgb(0, 0, 0));

        gcD.fillRect(0, 0, 823, 626);
        gcH.fillRect(0, 0, 823, 626);
        
        // Draw a message that says no log files are loaded just yet
        gcD.setFont(new Font("Arial", 30));
        gcH.setFont(new Font("Arial", 30));
        
        // Give the font a color
        gcD.setFill(Color.WHITE);
        gcH.setFill(Color.WHITE);
        
        // Draw the name and damage amount text
        gcD.fillText("No Log Files Loaded!", 250, 300);
        gcH.fillText("No Log Files Loaded!", 250, 300);

    }

    // Load the log and add it to our list view
    // Only the name of the boss should be shown on the view, but the path will be kept in our fileNames array
    @FXML
    private void btnLoadLog_clicked(ActionEvent event) throws FileNotFoundException, IOException {

        // Grab the file path from a FileChooser
        FileChooser fc = new FileChooser();
        File logFile = fc.showOpenDialog(null);
        
        // Scan the ENCOUNTER_START line to see what encounter name we should add to the listview
        // Declare our readers and set them to null
        FileReader fr = null;
        BufferedReader br = null;

        try {
          
            // Give some memory to our readers.  
            fr = new FileReader(logFile);
            br = new BufferedReader(fr);
            
            // Create a variable to hold each line
            String nextLine;
           
            // Create a variable to hold our line split up by a delimeter
            String[] splitLine; 

            // Read each line
            while((nextLine = br.readLine()) != null){
                
                //System.out.println(nextLine);
                
                // Split out the line using a comma as a delimiter
                splitLine = nextLine.split(",");

                // Check to see if our line contains ENCOUNTER_START
                if(splitLine[0].contains("ENCOUNTER_START")){

                    // If so, add it to the listview's list using
                    // lvLogList.getItems().add( <STRING> ); Where STRING is replaced by the encounter/boss name
                    // Remove the quotes " from the name using string replace 
                    lvLogList.getItems().add(splitLine[2].replace("\"", " "));
                    
                    // Also add it to our fileNames array and increase our numFiles count                  
                    fileNames[numFiles] = logFile.getAbsolutePath();
                    numFiles += 1;
                }
            } 
           
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "File load Error!", "File Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            br.close();
            fr.close();
        }
       
    }

    // When the encounter is clicked on the listview, parse the log selected
    @FXML
    private void lvLogList_Clicked(MouseEvent event) throws IOException {
        resetPlayers();
        parseLog(fileNames[lvLogList.getSelectionModel().getSelectedIndex()]);

    }

    // This is the main parsing function and will mainly deal with string manipulation   
    public void parseLog(String fileName) throws IOException {
        
        // Declare our readers and set them to null
        FileReader fr = null;
        BufferedReader br = null;
        
        // We'll need to parse our string twice using different delimiters.   This is because our log is really split into 2 sections: 
        // The timestamp + Action with a space between them
        // The Action (what we care about) is comma delimited and some of the stuff can have valid spaces in it's names so we don't want to token them out
        String delims1 = ",";
        String delims2 = ",| ";

        try {
            
            // TODO
            // Give some memory to our readers.  A BufferedReader will take the FileReader as an input
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);

            // Declare a variable to hold the line read in
            String nextLine;
            
            // Declare array variables to hold our 2 tokenized (split) strings (what string split() returns)
            String[] commaSplit;
            String[] spaceSplit;
            
            // Read every line of the log file until we get a null (meaning the end of the file)
            while((nextLine = br.readLine()) != null){
                
                // Split the line up by commas first and store it in an array
                commaSplit = nextLine.split(delims1);

                // Next use the first split string from above to parse out the event name
                // The first string from above should have included the timestamp and event
                spaceSplit = commaSplit[0].split(delims2);
                
                // Split the line again and see if it's a damage or healing type we want
                String event = new String(spaceSplit[3]);
                
                // Now that we have the event type, put it through our utility function to 
                // see if it's something we care about
                if(WoWUtilities.getEventType(spaceSplit[3]) != 0){
 
                    // If so loop through all of our raider names to see who to attribute it to (if anyone)
                    // We will ignore names that aren't in our playerlist
                    for(int i = 0; i < totalRaiders; i++){
                        
                        // All events except the SPELL_ABSORBED event use the same location for player names
                        // Because of this we should look at both cases
                                              
                        // First, remove the backslashes from our names 
                        String player1 = commaSplit[2].replace("\"", "");
                        String player2 = commaSplit[10].replace("\"", "");
                        String player3 = commaSplit[13].replace("\"", "");
                        String playerListPlayer = playerList[i].getName().replace("\"", "");
                        
                        boolean match = false;
                        
                        // Next, check to see if either of those names match their location in our split up line
                        if(playerListPlayer.equals(player1)){match = true;}
                        if(playerListPlayer.equals(player2)){match = true;}
                        if(playerListPlayer.equals(player3)){match = true;}
                        
                        if(match == true){
                            
                            // Determine if this is a damage or healing event
                            if(WoWUtilities.getEventType(event) == 1){

                                // WE FOUND A DAMAGE EVENT
                                if(event.equals("SWING_DAMAGE_LANDED")){
                                    
                                    //This is a SWING_DAMAGE_LANDED event, add it's value to our damage count for this player
                                    playerList[i].increaseDamageCount(Long.parseLong(commaSplit[22]));  
                                    
                                }else{
                                    
                                    //This is a damage that isn't SWING_DAMAGE_LANDED, add it's value to our damage count for this player
                                    playerList[i].increaseDamageCount(Long.parseLong(commaSplit[25]));                                    
                                }
                            }else{
                                
                                // WE FOUND A HEALING EVENT
                                // Figure out what number we need to add to the heal count
                                // For SPELL_ABSORBED, we just add the number
                                // This is a SPELL_ABSORBED event, add it's value to our healing count for this player
                                if(event.equals("SPELL_ABSORBED") && commaSplit.length == 17){
                                    
                                    playerList[i].increaseHealCount(Long.parseLong(commaSplit[16]));
                                    
                                }else if(event.equals("SPELL_ABSORBED") && commaSplit.length > 17){
                                    
                                    playerList[i].increaseHealCount(Long.parseLong(commaSplit[19]));
                                    
                                }else{
                                    
                                    // FOR SPELL_HEAL and SPELL_PERIODIC_HEAL
                                    // Subtract overhealing from healing to get number
                                    playerList[i].increaseHealCount(Long.parseLong(commaSplit[25]) - Long.parseLong(commaSplit[26]));
                                }
                            }
                        }                    
                    }                                                 
                }                                                           
            }                                   
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            br.close();
            fr.close();
        }
           
        // Finally, draw our output
        displayDamageAndHealing();

    }

    // This will reset all of our control variables to their initial state
    // This should be called whenever a new log file is opened
    public void resetPlayers() {

        // Reset the largest damage and heal count
        biggestDamage = 0;
        biggestHeal = 0;

        // Reset the heal and damage counts for all raiders in our playerlist
        for (int i = 0; i < totalRaiders; i++) {
            playerList[i].setDamageCount(0);
            playerList[i].setHealCount(0);
        }

    }

    // This loads the list of player names we will parse
    public void loadPlayerList() throws IOException {

        // The name of the file that has the names we want to parse
        String fileName = "WoWPlayers.txt";
        totalRaiders = 23;

        // File reading
        // Read in the file line by line
        FileReader fr = null;
        BufferedReader br = null;
        
        // TODO
        // Give some memory to our WoWPlayer array so it can hold all of our raiders
        playerList = new WoWPlayer[23];
        
        // Best to put this in a try / catch since we're dealing with files
        try {
            // Give our FileReader memory and read in the file
            fr = new FileReader(fileName);
            
            // Give our BufferedReader memory and give it the fileReader 
            br = new BufferedReader(fr);

            // Create a variable to hold the line read in (String)
            String nextLine;
            int i = 0;
            
            // We can either loop through all the lines or since we know the number of players 
            // we can cheat and just read that number of lines
            while(i < 23){
                
                // Read in the line
                nextLine = br.readLine();
                
                // Create a new WoWPlayer instance within our WoWPlayer Array                
                playerList[i] = new WoWPlayer();
                
                // Set the name of the WoWplayer using the name in the logfile               
                playerList[i].setName(nextLine);
                i++;
            }
            
        } catch (Exception ex) {
            // Showing a JOptionPane with an error message
            JOptionPane.showMessageDialog(null, "Error - Player list text file could not be read", "File Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Close the Readers
            br.close();
            fr.close();
        }
    }

    // After the logs are parsed we draw the output on our damage and healing tabs
    public void displayDamageAndHealing() {

        // Drawing our output on the two tabs       
        
        ///////////////////////////////
        // DAMAGE
        ///////////////////////////////
        gcD = cvsDamage.getGraphicsContext2D();
        
        // Clear the background with a black rectangle
        // Set the fill color
        gcD.setFill(Color.BLACK);
        
        // Draw the rectangle
        gcD.fillRect(0, 0, 823, 626);
       
        // Create the 3 Separator lines (blue in my example)
        // Set the stroke color
        gcD.setStroke(Color.BLUE);
        
        // Draw the 3 lines
        gcD.strokeLine(0, 50, 823, 50);
        gcD.strokeLine(250, 0, 250, 626);
        gcD.strokeLine(350, 50, 350, 626);       
        
        // Column headers - Name, Damage Amount
        gcD.setFont(new Font("Arial", 30));
        
        // Give the font a color
        gcD.setFill(Color.WHITE);
        
        // Draw the name and damage amount text
        gcD.fillText("NAME", 70, 40);
        gcD.fillText("DAMAGE AMOUNT", 400, 40);
               
        // Now we need to draw the output for each raider we have
        // First, sort the list from Highest Damage -> Lowest Damage
        WoWUtilities.sortWowPlayers("damage", playerList);
        
        // Set the font again 
        gcD.setFont(new Font("Arial", 15));
        
        int y = 50;
        int maxBar = 450;
        // Loop through all of our raiders
        for(int i = 0; i < totalRaiders; i++){
            
            // Draw the names
            String raiderNumber = Integer.toString(i + 1);
            
            gcD.setFill(Color.WHITE);
            gcD.fillText(raiderNumber, 10, y += 20);
            
            // Next you get the color of the raider name you are drawing (
            gcD.setFill(WoWUtilities.getRaiderColor(playerList[i].getName()));
            
            // And write out the name
            gcD.fillText(playerList[i].getName(), 30, y);
            
            // Next print the damage values using the same color            
            gcD.fillText(Long.toString(playerList[i].getDamageCount()), 260, y);
       
            // Finally draw the bar           
            double ratio = playerList[i].getDamageCount() / (double) playerList[0].getDamageCount();
            
            double currentBar = maxBar * ratio;
            
            gcD.fillRect(360, y - 15, currentBar, 18);

        }

        ///////////////////////////////
        // HEALING
        ///////////////////////////////
        gcH = cvsHealing.getGraphicsContext2D();
        
        // Clear the background with a black rectangle
        // Set the fill color
        gcH.setFill(Color.BLACK);
        
        // Draw the rectangle
        gcH.fillRect(0, 0, 823, 626);
       
        // Create the 3 Separator lines (blue in my example)
        // Set the stroke color
        gcH.setStroke(Color.BLUE);
        
        // Draw the 3 lines
        gcH.strokeLine(0, 50, 823, 50);
        gcH.strokeLine(250, 0, 250, 626);
        gcH.strokeLine(350, 50, 350, 626);
               
        // Column headers - Name, Damage Amount
        gcH.setFont(new Font("Arial", 30));
        
        // Give the font a color
        gcH.setFill(Color.WHITE);
        
        // Draw the name and damage amount text
        gcH.fillText("NAME", 70, 40);
        gcH.fillText("HEALING AMOUNT", 400, 40);
    
        // Now we need to draw the output for each raider we have
        // First, sort the list from Highest Damage -> Lowest Damage
        WoWUtilities.sortWowPlayers("healing", playerList);
        
        // Set the font again
        gcH.setFont(new Font("Arial", 15));
        y = 50;
        // Loop through all of our raiders
        for(int i = 0; i < totalRaiders; i++){
            
            // Draw the names
            String raiderNumber = Integer.toString(i + 1);
            
            gcH.setFill(Color.WHITE);
            gcH.fillText(raiderNumber, 10, y += 20);
            
            // Next you get the color of the raider name you are drawing 
            gcH.setFill(WoWUtilities.getRaiderColor(playerList[i].getName()));
            
            // And write out the name
            gcH.fillText(playerList[i].getName(), 30, y);
            
            // Next print the damage values using the same color            
            gcH.fillText(Long.toString(playerList[i].getHealCount()), 260, y);
       
            // Finally draw the bar     
            double ratio = playerList[i].getHealCount() / (double) playerList[0].getHealCount();
            
            double currentBar = maxBar * ratio;
            
            gcH.fillRect(360, y - 15, currentBar, 18);

        }
    }
}

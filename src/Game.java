//THIS IS WHERE YOU UNDERSTAND WHAT THE COMMANDS MEAN. YOU CAN ALSO ACCESS THINGS LIKE INVENTORY, ITEMS IN ROOMS, ETC. THIS IS WHERE ALL THE GAME STUFF HAPPENS. YOU WILL WRITE MOST OF YOUR CODE HERE.


import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class Game - the main class of the "Zork" game.
 *
 * Author: Michael Kolling Version: 1.1 Date: March 2000
 * 
 * This class is the main class of the "Zork" application. Zork is a very
 * simple, text based adventure game. Users can walk around some scenery. That's
 * all. It should really be extended to make it more interesting!
 * 
 * To play this game, create an instance of this class and call the "play"
 * routine.
 * 
 * This main class creates and initialises all the others: it creates all rooms,
 * creates the parser and starts the game. It also evaluates the commands that
 * the parser returns.
 */
public class Game {
  private Parser parser;
  private Room currentRoom;
  // This is a MASTER object that contains all of the rooms and is easily
  // accessible.
  // The key will be the name of the room -> no spaces (Use all caps and
  // underscore -> Great Room would have a key of GREAT_ROOM
  // In a hashmap keys are case sensitive.
  // masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the Great
  // Room (assuming you have one).
  private HashMap<String, Room> masterRoomMap;

  private void initRooms(String fileName) throws Exception {
    masterRoomMap = new HashMap<String, Room>();
    Scanner roomScanner;
    try {
      HashMap<String, HashMap<String, String>> exits = new HashMap<String, HashMap<String, String>>();
      roomScanner = new Scanner(new File(fileName));
      while (roomScanner.hasNext()) {
        Room room = new Room();
        // Read the Name
        String roomName = roomScanner.nextLine();
        room.setRoomName(roomName.split(":")[1].trim());
        // Read the Description
        String roomDescription = roomScanner.nextLine();
        room.setDescription(roomDescription.split(":")[1].replaceAll("<br>", "\n").trim());
        // Read the Exits
        String roomExits = roomScanner.nextLine();
        // An array of strings in the format E-RoomName
        String[] rooms = roomExits.split(":")[1].split(",");
        HashMap<String, String> temp = new HashMap<String, String>();
        for (String s : rooms) {
          temp.put(s.split("-")[0].trim(), s.split("-")[1]);
        }

        exits.put(roomName.substring(10).trim().toUpperCase().replaceAll(" ", "_"), temp);

        // This puts the room we created (Without the exits in the masterMap)
        masterRoomMap.put(roomName.toUpperCase().substring(10).trim().replaceAll(" ", "_"), room);

        // Now we better set the exits.
      }

      for (String key : masterRoomMap.keySet()) {
        Room roomTemp = masterRoomMap.get(key);
        HashMap<String, String> tempExits = exits.get(key);
        for (String s : tempExits.keySet()) {
          // s = direction
          // value is the room.

          String roomName2 = tempExits.get(s.trim());
          Room exitRoom = masterRoomMap.get(roomName2.toUpperCase().replaceAll(" ", "_"));
          roomTemp.setExit(s.trim().charAt(0), exitRoom);
        }
      }

      roomScanner.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the game and initialise its internal map.
   */


  //CHANGE ROOM YOU START OFF HERE
  public Game() {
    try {
      initRooms("data/rooms.dat");
      currentRoom = masterRoomMap.get("HANGAR");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    parser = new Parser();
  }

  /**
   * Main play routine. Loops until end of play.
   */
  public void play() {
    printWelcome();
    // Enter the main command loop. Here we repeatedly read commands and
    // execute them until the game is over.

    boolean finished = false;
    while (!finished) {
      Command command = parser.getCommand();
      finished = processCommand(command);
    }
    System.out.println("Thank you for playing.  Good bye.");
  }

  /**
   * Print out the opening message for the player.
   */

  //CHANGE WELCOME MESSAGE
  private void printWelcome() {
    System.out.println();
    System.out.println("Welcome to Space Survival!");
    System.out.println("Space Survival is an action adventure game full of mystery and strategy.");
    System.out.println("Type 'help' if you need help.");
    System.out.println();
    System.out.println("You are aboard a space station located on the outer rims of the Milky Ray.");
    System.out.println("You have just woken up, and you are currently gathering some equipment from around the ship to take with you before you take your fighter jet and go on your assigned scouting mission.");
    System.out.println("Your task: Walk around the ship and collect the following items: Meal pack, laser gun, medical kit, tarp. Return to the hanger once these items are collected.");
    System.out.println();
    System.out.println(currentRoom.longDescription());
  }

  /**
   * Given a command, process (that is: execute) the command. If this command ends
   * the game, true is returned, otherwise false is returned.
   */

  //CHANGE RESULTS BASED OFF OF COMMANDS
  private boolean processCommand(Command command) {
    if (command.isUnknown()) {
      System.out.println("I don't know what you mean...");
      return false;
    }
    String commandWord = command.getCommandWord();
    if (commandWord.equals("help"))
      printHelp();
    else if (commandWord.equals("go"))
      goRoom(command);
    else if (commandWord.equals("quit")) {
      if (command.hasSecondWord())
        System.out.println("Quit what?");
      else
        return true; // signal that we want to quit
    } else if (commandWord.equals("eat")) {
      System.out.println("Do you really think you should be eating at a time like this?");
    }else if (commandWord.equals("jump")){
      System.out.println("jump up and down");
    }
    return false;
  }

  // implementations of user commands:
  /**
   * Print out some help information. Here we print some stupid, cryptic message
   * and a list of the command words.
   */
  private void printHelp() {
    System.out.println("You are lost. You are alone. You wander");
    System.out.println("around at Monash Uni, Peninsula Campus.");
    System.out.println();
    System.out.println("Your command words are:");
    parser.showCommands();
  }

  /**
   * Try to go to one direction. If there is an exit, enter the new room,
   * otherwise print an error message.
   */

  //CREATE THIRD WORD FOR COMMAND BY GOING TO COMMAND FILE AND ADDING THIRD WORD
  private void goRoom(Command command) {
    if (!command.hasSecondWord()) {
      // if there is no second word, we don't know where to go...
      System.out.println("Go where?");
      return;
    }
    String direction = command.getSecondWord();
    // Try to leave current room.
    Room nextRoom = currentRoom.nextRoom(direction);
    if (nextRoom == null)
      System.out.println("There is no door!");
    else {
      currentRoom = nextRoom;
      System.out.println(currentRoom.longDescription());
    }
  }
}
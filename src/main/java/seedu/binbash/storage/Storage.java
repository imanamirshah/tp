package seedu.binbash.storage;

import org.apache.commons.cli.ParseException;
import seedu.binbash.ItemList;
import seedu.binbash.command.Command;
import seedu.binbash.exceptions.InvalidCommandException;
import seedu.binbash.item.*;
import seedu.binbash.exceptions.BinBashException;
import seedu.binbash.parser.AddCommandParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage {

    private static final int READING_IN_PROFIT_QUANTITIES = 1;
    private static final int READING_IN_ITEM = 0;
    private static final int TOTAL_UNITS_PURCHASED_INDEX = 0;
    private static final int TOTAL_UNITS_SOLD_INDEX = 1;

    protected String filePath;
    protected String dataDirectoryPath;
    protected String dataFileName;
    protected boolean isCorrupted;
    protected Logger storageLogger;

    public Storage() {
        this.filePath = "data/items.txt";
        this.dataDirectoryPath = "./data/";
        this.dataFileName = "items.txt";
        this.isCorrupted = false; // set to false by default}
        this.storageLogger = Logger.getLogger("storageLogger");
    }

    // TODO: Handle exceptions properly (when the exceptions for AddCommand are settled)
    //  Lumping 3 exceptions together without a custom message for each
    //  exception is bad exception handling!

    /**
     * Loads the data from the file into the itemManager and returns a list of items.
     * If the data file is corrupted or an error occurs during reading, the file is marked as corrupted.
     *
     * @param itemManager The ItemList object to add loaded items to.
     * @return A list of items loaded from the file, or null if the file is corrupted.
     * @throws RuntimeException if an error occurs during file reading.
     */
    public ArrayList<Item> loadData(ItemList itemManager) {
        storageLogger.log(Level.INFO, "Preparing to load data from storage file.");

        ArrayList<Item> itemList = null;

        try {
            ArrayList<String> stringRepresentationOfTxtFile = readTxtFile();
            parseAndAddToList(stringRepresentationOfTxtFile, itemManager);
        } catch (BinBashException | IOException | NumberFormatException | ParseException e) {
            isCorrupted = true;
        }

        assert !isCorrupted : "data file is corrupted";

        storageLogger.log(Level.INFO, "Data loaded successfully.");

        return itemList;
    }

    /**
     * Reads the data file ('items.txt') and returns a list of strings representing each line in the file.
     * If the 'data' directory or 'items.txt' file does not exist, they will be created.
     *
     * @return A list of strings, each representing a line in the data file.
     * @throws BinBashException if the directory or file cannot be created.
     * @throws IOException if an error occurs during file reading.
     */
    private ArrayList<String> readTxtFile() throws BinBashException, IOException {
        File dataDirectory = new File(dataDirectoryPath);
        File dataFile = new File(dataDirectory, dataFileName);

        // Checks if the 'data' directory exists, if not create it
        if (!dataDirectory.exists()) {
            boolean wasDirectoryMade = dataDirectory.mkdirs();
            if (!wasDirectoryMade) {
                throw new BinBashException("Could not create data directory.");
            }
        }

        // Checks if the 'tasks.txt' file exists, if not create it
        if (!dataFile.exists()) {
            boolean wasFileCreated = dataFile.createNewFile();
            if (!wasFileCreated) {
                throw new BinBashException("Could not create items.txt file.");
            }
        }

        assert dataDirectory.exists() : "Data directory should already exist / have been created";
        assert dataFile.exists() : "Data file (items.txt) should already exist / have been created";

        ArrayList<String> dataItems = (ArrayList<String>)
                Files.readAllLines(dataFile.toPath(), Charset.defaultCharset());
        return dataItems;
    }

    /**
     * Parses the string representation of the text file and adds items to the list of items.
     * The method handles both "add" command and "setprofit" strings to populate the item list.
     *
     * @param stringRepresentationOfTxtFile The list of strings representing each line in the text file.
     * @param itemManager The ItemList object containing the empty list of items.
     * @throws InvalidCommandException if the command is invalid.
     * @throws ParseException if there is an error in parsing the command arguments.
     */
    private void parseAndAddToList(ArrayList<String> stringRepresentationOfTxtFile, ItemList itemManager)
            throws InvalidCommandException, ParseException {

        AddCommandParser addCommandParser = new AddCommandParser();
        int parseState = READING_IN_ITEM;

        for (String line : stringRepresentationOfTxtFile) {
            String[] tokens = line.trim().split("\\s+"); // Tokenize user input
            String commandString = tokens[0].toLowerCase();
            String[] commandArgs = Arrays.copyOfRange(tokens, 1, tokens.length); // Takes only options and arguments

            if ("add".equals(commandString)) {
                assert parseState == READING_IN_ITEM;
                handleAddCommand(addCommandParser, commandArgs, itemManager);
                parseState = READING_IN_PROFIT_QUANTITIES;
            } else if ("setprofit".equals(commandString)) {
                assert parseState == READING_IN_PROFIT_QUANTITIES;
                handleSetProfitString(commandArgs, itemManager);
                parseState = READING_IN_ITEM;
            }
        }
    }

    /**
     * Handles the "add" command by parsing the arguments and executing the command to add an item to the item list.
     *
     * @param addCommandParser The parser object used to parse the add command.
     * @param commandArgs The arguments of the add command.
     * @param itemManager The ItemList object to which the new item is added.
     * @throws InvalidCommandException if the command is invalid.
     * @throws ParseException if there is an error in parsing the command arguments.
     */
    private void handleAddCommand(AddCommandParser addCommandParser, String[] commandArgs, ItemList itemManager)
            throws InvalidCommandException, ParseException {
        Command addCommand = addCommandParser.parse(commandArgs);
        addCommand.execute(itemManager);
    }

    /**
     * Handles the "setprofit" string by updating the profit-related properties of the most recently added item.
     *
     * @param commandArgs The arguments of the setprofit command.
     * @param itemManager The ItemList object containing the recently added item.
     */
    private void handleSetProfitString(String[] commandArgs, ItemList itemManager) {
        List<Item> itemList = itemManager.getItemList();
        Item recentlyAddedItem = itemList.get(itemList.size() - 1);

        String totalUnitsPurchased = commandArgs[TOTAL_UNITS_PURCHASED_INDEX];
        recentlyAddedItem.setTotalUnitsPurchased(Integer.parseInt(totalUnitsPurchased));

        if (recentlyAddedItem instanceof RetailItem) {
            String totalUnitsSold = commandArgs[TOTAL_UNITS_SOLD_INDEX];
            ((RetailItem) recentlyAddedItem).setTotalUnitsSold(Integer.parseInt(totalUnitsSold));
        }
    }


    /**
     * Saves the list of items to the storage file in a format suitable for loading.
     *
     * @param itemList The list of items to be saved.
     */
    public void saveToStorage(List<Item> itemList) {
        String textToSave = generateTextToSave(itemList);

        try {
            FileWriter fw = new FileWriter(filePath);
            fw.write(textToSave);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a string representation of the list of items to be saved to the file.
     * Each item is represented in the format of an "add" command followed by a "setprofit" string.
     *
     * @param itemList The list of items to be converted into a string.
     * @return A string representation of the list of items, suitable for saving to a file.
     */
    private static String generateTextToSave(List<Item> itemList) {
        String textToSave = "";

        for (Item item: itemList) {
            if (item != null) {
                textToSave += generateStorageRepresentationOfSingleItem(item)
                        + System.lineSeparator();
            }
        }
        return textToSave;
    }

    /**
     * Generates a string representation of a single item in the format of an "add" command, followed by
     * a "setprofit" string
     *
     * @param item The item to be converted into a string.
     * @return A string representation of the item, suitable for saving to a file.
     */
    private static String generateStorageRepresentationOfSingleItem(Item item) {
        String output = "";

        output += "add "
                + "-n " + item.getItemName() + " "
                + "-d " + item.getItemDescription() + " "
                + "-q " + item.getItemQuantity() + " "
                + "-c " + item.getItemCostPrice() + " ";

        if (item instanceof RetailItem) {
            RetailItem retailItem = (RetailItem) item;
            output += "-s " + retailItem.getItemSalePrice() + " "
                    + "-re ";
        }

        if (item instanceof PerishableRetailItem) {
            PerishableRetailItem perishableRetailItem = (PerishableRetailItem) item;
            output += "-e " + perishableRetailItem.getItemExpirationDate() + " ";
        }

        if (item instanceof OperationalItem) {
            output += "-op ";
        }

        if (item instanceof PerishableOperationalItem) {
            PerishableOperationalItem perishableOperationalItem = (PerishableOperationalItem) item;
            output += "-e " + perishableOperationalItem.getItemExpirationDate() + " ";
        }

        output += System.lineSeparator();

        output += "setprofit "
                + item.getTotalUnitsPurchased() + " ";

        if (item instanceof RetailItem) {
            RetailItem retailItem = (RetailItem) item;
            output += retailItem.getTotalUnitsSold();
        }

        return output;
    }
}

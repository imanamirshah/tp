package seedu.binbash.command;

import org.junit.jupiter.api.Test;
import seedu.binbash.inventory.ItemList;
import seedu.binbash.exceptions.InvalidCommandException;
import seedu.binbash.item.Item;
import seedu.binbash.item.PerishableRetailItem;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpdateCommandTest {

    @Test
    void execute_updateByName_itemUpdated() throws InvalidCommandException {
        ItemList itemList = new ItemList(new ArrayList<Item>());
        itemList.addItem("retail", "testItem", "A test item", 2,
                LocalDate.now(), 4.00, 5.00, 6);

        UpdateCommand updateCommand = new UpdateCommand("testItem");
        updateCommand.setItemDescription("Updated item");
        updateCommand.setItemQuantity(5);
        updateCommand.setItemExpirationDate(LocalDate.of(1999, 1, 1));
        updateCommand.setItemSalePrice(6.00);
        updateCommand.setItemCostPrice(7.00);
        updateCommand.setItemThreshold(8);

        assertTrue(updateCommand.execute(itemList));
        PerishableRetailItem updatedItem = (PerishableRetailItem) itemList.findItemByName("testItem");
        assertEquals("Updated item", updatedItem.getItemDescription());
        assertEquals(5, updatedItem.getItemQuantity());
        assertEquals(LocalDate.of(1999, 1, 1), updatedItem.getLocalDateItemExpirationDate());
        assertEquals(6.00, updatedItem.getItemSalePrice());
        assertEquals(7.00, updatedItem.getItemCostPrice());
        assertEquals(8, updatedItem.getItemThreshold());
    }

    @Test
    void execute_updateByIndex_itemUpdated() {
        ItemList itemList = new ItemList(new ArrayList<>());
        itemList.addItem("retail", "testItem", "A test item", 2,
                LocalDate.now(), 4.00, 5.00, 6);

        UpdateCommand updateCommand = new UpdateCommand(1);
        updateCommand.setItemDescription("Updated item");
        updateCommand.setItemQuantity(5);
        updateCommand.setItemExpirationDate(LocalDate.of(1999, 1, 1));
        updateCommand.setItemSalePrice(6.00);
        updateCommand.setItemCostPrice(7.00);
        updateCommand.setItemThreshold(8);
        updateCommand.setIsIndex();

        assertTrue(updateCommand.execute(itemList));
        PerishableRetailItem updatedItem = (PerishableRetailItem) itemList.getItemList().get(0);
        assertEquals("Updated item", updatedItem.getItemDescription());
        assertEquals(5, updatedItem.getItemQuantity());
        assertEquals(LocalDate.of(1999, 1, 1), updatedItem.getLocalDateItemExpirationDate());
        assertEquals(6.00, updatedItem.getItemSalePrice());
        assertEquals(7.00, updatedItem.getItemCostPrice());
        assertEquals(8, updatedItem.getItemThreshold());
    }

    @Test
    void execute_itemNotFound_updateFailed() {
        ItemList itemList = new ItemList(new ArrayList<>());
        UpdateCommand updateCommand = new UpdateCommand("nonexistentItem");
        updateCommand.setItemDescription("Updated item");
        updateCommand.setItemQuantity(5);
        updateCommand.setItemExpirationDate(LocalDate.now());
        updateCommand.setItemSalePrice(6.00);
        updateCommand.setItemCostPrice(7.00);
        updateCommand.setItemThreshold(8);

        assertTrue(updateCommand.execute(itemList));
        assertEquals("Item with name 'nonexistentItem' not found! " +
                "Consider using the search or the list command to find the exact name of your item!",
                updateCommand.getExecutionUiOutput());
    }

    @Test
    void execute_indexOutOfBounds_updateFailed() {
        ItemList itemList = new ItemList(new ArrayList<>());
        itemList.addItem("retail", "testItem", "A test item", 2,
                LocalDate.now(), 4.00, 5.00, 6);

        UpdateCommand updateCommand = new UpdateCommand(2);
        updateCommand.setItemDescription("Updated item");
        updateCommand.setItemQuantity(5);
        updateCommand.setItemExpirationDate(LocalDate.now());
        updateCommand.setItemSalePrice(6.00);
        updateCommand.setItemCostPrice(7.00);
        updateCommand.setItemThreshold(8);
        updateCommand.setIsIndex();

        assertTrue(updateCommand.execute(itemList));
        assertEquals("Index entered is out of bounds!", updateCommand.getExecutionUiOutput());
    }

    @Test
    void execute_updateItemFieldsIndependently_itemUpdated() throws InvalidCommandException {
        ItemList itemList = new ItemList(new ArrayList<>());
        itemList.addItem("retail", "testItem", "A test item", 2,
                LocalDate.now(), 4.00, 5.00, 6);

        UpdateCommand updateCommand1 = new UpdateCommand("testItem");
        updateCommand1.setItemDescription("Updated Description");
        assertTrue(updateCommand1.execute(itemList));
        assertEquals("Updated Description", itemList.findItemByName("testItem").getItemDescription());

        UpdateCommand updateCommand2 = new UpdateCommand("testItem");
        updateCommand2.setItemQuantity(10);
        assertTrue(updateCommand2.execute(itemList));
        assertEquals(10, itemList.findItemByName("testItem").getItemQuantity());

        UpdateCommand updateCommand5 = new UpdateCommand("testItem");
        updateCommand5.setItemCostPrice(7.50);
        assertTrue(updateCommand5.execute(itemList));
        assertEquals(7.50, itemList.findItemByName("testItem").getItemCostPrice());

        UpdateCommand updateCommand6 = new UpdateCommand("testItem");
        updateCommand6.setItemThreshold(12);
        assertTrue(updateCommand6.execute(itemList));
        assertEquals(12, itemList.findItemByName("testItem").getItemThreshold());
    }
}

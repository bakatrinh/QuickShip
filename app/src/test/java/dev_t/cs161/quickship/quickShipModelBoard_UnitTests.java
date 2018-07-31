package dev_t.cs161.quickship;

import org.junit.Test;


import static org.junit.Assert.*;

public class quickShipModelBoard_UnitTests {
    static final int SHIP_1 = quickShipModelBoardSlot.TWO;
    static final int SHIP_2 = quickShipModelBoardSlot.THREE_A;
    static final int SHIP_3 = quickShipModelBoardSlot.THREE_B;
    static final int SHIP_4 = quickShipModelBoardSlot.FOUR;
    static final int SHIP_5 = quickShipModelBoardSlot.FIVE;

    static final int HORIZONTAL = quickShipModelBoardSlot.HORIZONTAL;
    static final int VERTICAL = quickShipModelBoardSlot.VERTICAL;
    @Test
    public void EmptyBoardTest() throws Exception {
        quickShipModelBoard testBoard = new quickShipModelBoard();

        assertFalse("Failed Empty Board Test; Expected False.", testBoard.checkAllPlayerShipPlaces());
    }
    @Test
    public void SetAllFiveShips() throws Exception {
        quickShipModelBoard testBoard = new quickShipModelBoard();
        testBoard.addShip(0, SHIP_1, VERTICAL);
        testBoard.addShip(1, SHIP_2, VERTICAL);
        testBoard.addShip(2, SHIP_3, VERTICAL);
        testBoard.addShip(3, SHIP_4, VERTICAL);
        testBoard.addShip(4, SHIP_5, VERTICAL);
        assertTrue("Failed to Set All Five Ships; Expected True.", testBoard.checkAllPlayerShipPlaces());
    }
    @Test
    public void AddAndRemove_Ships() throws Exception {
        quickShipModelBoard testBoard = new quickShipModelBoard();
        testBoard.addShip(0, SHIP_1, HORIZONTAL);
        testBoard.addShip(10, SHIP_2, HORIZONTAL);
        testBoard.addShip(20, SHIP_3, HORIZONTAL);
        testBoard.addShip(30, SHIP_4, HORIZONTAL);
        testBoard.addShip(40, SHIP_5, HORIZONTAL);
        testBoard.removeShip(0);
        testBoard.removeShip(10);
        testBoard.removeShip(20);
        testBoard.removeShip(30);
        testBoard.removeShip(40);
        assertFalse("Failed to Remove Ships, Expected False.", testBoard.checkAllPlayerShipPlaces());
    }

    @Test
    public void HorizontalAndVerticalShipCollisionTest() throws Exception {
        quickShipModelBoard testBoard = new quickShipModelBoard();
        assertFalse("Detected Ship Collision in Empty Slot; Expected False.", testBoard.isCollisionExist(0, SHIP_1, HORIZONTAL));
        testBoard.addShip(0, SHIP_1, HORIZONTAL);
        assertTrue("Horizontal Ship Collision Detection Failed; Expected True.", testBoard.isCollisionExist(1, SHIP_2, VERTICAL));
        testBoard.addShip(10, SHIP_2, VERTICAL);
        assertTrue("Vertical Ship Collision Detection Failed; Expected True.", testBoard.isCollisionExist(20, SHIP_3, HORIZONTAL));

    }

    @Test
    public void checkGameOverTest() throws Exception{
        quickShipModelBoard testBoard = new quickShipModelBoard();
        testBoard.addShip(0, SHIP_1, HORIZONTAL);
        testBoard.addShip(10, SHIP_2, HORIZONTAL);
        testBoard.addShip(20, SHIP_3, HORIZONTAL);
        testBoard.addShip(30, SHIP_4, HORIZONTAL);
        testBoard.addShip(40, SHIP_5, HORIZONTAL);
        assertFalse("Detected Game Over; Expected False.", testBoard.checkGameOver());
        for (int i = 0; i < 100; i++) {
            if( testBoard.isOccupied(i) )
                testBoard.setHit(i, true);
        }
        assertTrue("Did NOT Detect Game Over; Expected True.", testBoard.checkGameOver());
    }

    @Test
    public void board2byteArray2boardTest() throws Exception{
        quickShipModelBoard testBoard = new quickShipModelBoard();
        testBoard.addShip(0, SHIP_1, HORIZONTAL);
        testBoard.addShip(10, SHIP_2, HORIZONTAL);
        testBoard.addShip(20, SHIP_3, HORIZONTAL);
        testBoard.addShip(30, SHIP_4, HORIZONTAL);
        testBoard.addShip(40, SHIP_5, HORIZONTAL);

        byte[] testBoardByteArray = testBoard.convertBoard2ByteArray();

        quickShipModelBoard testBoardRestored = new quickShipModelBoard();
        testBoardRestored.convertByteArray2Board( testBoardByteArray );

        boolean boardsMatch = true;
        for (int i = 0; i < 100; i++) {
            if( testBoard.getAnchorIndex(i) != testBoardRestored.getAnchorIndex(i))
                boardsMatch = false;
            if( testBoard.getShipType(i) != testBoardRestored.getShipType(i))
                boardsMatch = false;
        }
        assertTrue("Board to Byte Array to Board Conversion Does Not Match; Expected True", boardsMatch);
    }
}

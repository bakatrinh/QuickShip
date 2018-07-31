package dev_t.cs161.quickship;

import org.junit.Test;


import static org.junit.Assert.*;

public class quickShipBluetoothPacketsToBeSent_UnitTests {
    static final int SHIP_1 = quickShipModelBoardSlot.TWO;
    static final int SHIP_2 = quickShipModelBoardSlot.THREE_A;
    static final int SHIP_3 = quickShipModelBoardSlot.THREE_B;
    static final int SHIP_4 = quickShipModelBoardSlot.FOUR;
    static final int SHIP_5 = quickShipModelBoardSlot.FIVE;

    static final int HORIZONTAL = quickShipModelBoardSlot.HORIZONTAL;

    @Test
    public void SetAndGet_PacketType() throws Exception {
        quickShipBluetoothPacketsToBeSent dataPacket = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.TURN_DONE, true);
        assertEquals("Failed to Set and Get Packet Type; Expected Equal.", quickShipBluetoothPacketsToBeSent.TURN_DONE, dataPacket.getPacketType());
    }
    @Test
    public void SetAndGet_ChatMessage() throws Exception {
        String full_msg = "full message...";
        quickShipBluetoothPacketsToBeSent dataPacket = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.CHAT, full_msg);
        assertTrue("Failed to Set and Get Chat Message; Expected True.", full_msg.equals(dataPacket.getChatMessage()));
    }

    @Test
    public void setAndGet_QuickShipBoard() throws Exception {
        quickShipModelBoard testBoard = new quickShipModelBoard();
        testBoard.addShip(0, SHIP_1, HORIZONTAL);
        testBoard.addShip(10, SHIP_2, HORIZONTAL);
        testBoard.addShip(20, SHIP_3, HORIZONTAL);
        testBoard.addShip(30, SHIP_4, HORIZONTAL);
        testBoard.addShip(40, SHIP_5, HORIZONTAL);

        byte[] testBoardByteArray = testBoard.convertBoard2ByteArray();

        quickShipBluetoothPacketsToBeSent dataPacket = new quickShipBluetoothPacketsToBeSent(quickShipBluetoothPacketsToBeSent.SHIPS_PLACED, testBoardByteArray);



        quickShipModelBoard testBoardRestored = new quickShipModelBoard();
        testBoardRestored.convertByteArray2Board( dataPacket.getBoardv2() );

        boolean boardsMatch = true;
        for (int i = 0; i < 100; i++) {
            if( testBoard.getAnchorIndex(i) != testBoardRestored.getAnchorIndex(i))
                boardsMatch = false;
            if( testBoard.getShipType(i) != testBoardRestored.getShipType(i))
                boardsMatch = false;
        }
        assertTrue("Failed To Restore Game Board from quickShip Packet; Expected True.", boardsMatch);
    }
}

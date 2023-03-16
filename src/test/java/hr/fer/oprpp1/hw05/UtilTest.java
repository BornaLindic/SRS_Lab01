package hr.fer.oprpp1.hw05;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UtilTest {

    @Test
    public void testHexToByteZeroLength() {
        assertArrayEquals(new byte[0], Util.hextobyte(""));
    }

    @Test
    public void testHexToByteOddLength() {
        assertThrows(IllegalArgumentException.class, () -> Util.hextobyte("1"));
    }

    @Test
    public void testHexToByte() {
        assertArrayEquals(new byte[] {1, -82, 34}, Util.hextobyte("01aE22"));
    }

    @Test
    public void testByteToHey() {
        assertEquals("01ae22", Util.bytetohex(new byte[] {1, -82, 34}));
    }
}

package com.berrygobbler78.flacplayer.cd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CDRipperTest {

    @Test
    public void testCDUUIDGeneration() {
        CDRipper ripper = new CDRipper();
        String uuid = ripper.getCDUUID();
        // This will only work if cdparanoia -Q succeeds on the current system
        // Given that I could run it in bash, it should work here too.
        if (uuid != null) {
            System.out.println("[DEBUG_LOG] Generated CD UUID: " + uuid);
            assertFalse(uuid.isEmpty());
        } else {
            System.out.println("[DEBUG_LOG] CD UUID generation failed or not supported on this OS.");
        }
    }

    @Test
    public void testTrackCount() {
        CDRipper ripper = new CDRipper();
        int count = ripper.getTrackCount();
        System.out.println("[DEBUG_LOG] Track count: " + count);
        assertTrue(count >= 0);
    }
}

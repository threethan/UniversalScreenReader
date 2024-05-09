package org.threethan.universalreader.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpeechHelperTest {

    @Test
    @DisplayName("Test that VoiceSpeed.toString() uses provided string")
    void testToString() {
        final String testString = "ThisIsA test String... it is long and UnIqUe!";
        final SpeechHelper.VoiceSpeed vs = new SpeechHelper.VoiceSpeed(0, testString, false);
        assertEquals(vs.toString(), vs.string(), testString);
    }

    @Test
    @DisplayName("Ensure that default voice speed is a member of the speed list")
    void testDefaultPresent() {
        for (SpeechHelper.VoiceSpeed vs : SpeechHelper.VoiceSpeed.SPEEDS) if (vs == SpeechHelper.VoiceSpeed.DEFAULT) return;
        fail();
    }

    @Test
    @DisplayName("Test all voice speeds are unique")
    void testUnique() {
        Set<SpeechHelper.VoiceSpeed> seenSpeeds = new HashSet<>();
        for (SpeechHelper.VoiceSpeed vsA : SpeechHelper.VoiceSpeed.SPEEDS) {
            for (SpeechHelper.VoiceSpeed vsB : seenSpeeds) {
                assertNotEquals(vsA, vsB);
                assertNotEquals(vsA.speed(), vsB.speed());
                assertNotEquals(vsA.toString(), vsB.toString());
                // Note: "noPause" flag may not be unique, that's ok
            }
            seenSpeeds.add(vsA);
        }
    }

    @Test
    @DisplayName("Test all voice speeds are in order from slowest to fastest, and are between -100 and 100")
    // Note: -100 to 100 is a semi-arbitrary range set by the JNativeSpeechAdapter
    void testInOrder() {
        int lastSpeed = -100;
        for (SpeechHelper.VoiceSpeed vs : SpeechHelper.VoiceSpeed.SPEEDS) {
            assertTrue(vs.speed() >= lastSpeed);
            assertTrue(vs.speed() <= 100);
            lastSpeed = vs.speed();
        }
    }
}
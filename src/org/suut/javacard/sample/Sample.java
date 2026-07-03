package org.suut.javacard.sample;

import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.APDU;

public class Sample extends Applet {
    private static final byte INS_HELLO = 0x55;

    private static final byte ENCRYPTION_KEY = 0x42;

    public static void install(byte[] array, short offset, byte length) throws ISOException {
        // check the install parameters

        byte Li = array[offset];
        byte Lc = array[(short)(offset+Li+1)];
        byte La = array[(short)(offset+Li+Lc+2)];

        if (La != 0) { // we do not accept any parameter
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }

        Sample applet = new Sample();
        applet.register();
    }

    protected Sample() {
    }

    public boolean select() {
        return true;
    }

    public void process(APDU apdu) throws ISOException {
        if(selectingApplet() || reSelectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();
        short length = apdu.setIncomingAndReceive();

        if (!apdu.isValidCLA()) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        if (apdu.isCommandChainingCLA()) {
            // Most modern readers do support extended length, which is more convenient
            // to implement for all parties
            ISOException.throwIt(ISO7816.SW_COMMAND_CHAINING_NOT_SUPPORTED);
        }

        if (apdu.isISOInterindustryCLA()) {
            // We only support the proprietary class
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        if (apdu.isSecureMessagingCLA()) {
            // Use the GlobalPlatform SecureChannel API if you want to wrap and unwrap using
            // the associated security domain secure channel keys
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        byte cla = buffer[ISO7816.OFFSET_CLA];
        byte ins = buffer[ISO7816.OFFSET_INS];
        byte p1 = buffer[ISO7816.OFFSET_P1];
        byte p2 = buffer[ISO7816.OFFSET_P2];
        byte lc = buffer[ISO7816.OFFSET_LC];

        if (length != (short)lc) {
            // Incoming data too big for the APDU buffer size.
            // The remaining data could be copied to a RAM buffer,
            // but we're just going to return an error code.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        switch (ins) {
        case INS_HELLO:
            if (p1 != 0x00 || p2 != 0x00) {
                // We do not support special values of P1 and P2
                ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
            }
            if (length == 0) {
                // We want at least one byte of data to act on
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }

            // Apply very secure encryption to the input buffer.
            // For extra convenience, the encryption and decryption
            // are the same operation.
            for (short i = 0; i < length; i++) {
                buffer[(short)(ISO7816.OFFSET_CDATA + i)] = (byte)(ENCRYPTION_KEY ^ buffer[(short)(ISO7816.OFFSET_CDATA + i)]);
            }

            apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, length);
            break;

        default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            break;
        }
    }
}

/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.serialisation.util;

import org.junit.Before;
import org.junit.Test;

import uk.gov.gchq.gaffer.core.exception.GafferCheckedException;
import uk.gov.gchq.gaffer.serialisation.IntegerSerialiser;
import uk.gov.gchq.gaffer.serialisation.ToBytesSerialiser;
import uk.gov.gchq.gaffer.serialisation.implementation.raw.RawIntegerSerialiser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MultiSerialiserStorageTest {

    public static final byte BYTE = (byte) 0;
    public static final Class<? extends ToBytesSerialiser> SERIALISER_CLASS = IntegerSerialiser.class;
    public static final Class<? extends ToBytesSerialiser> SERIALISER_CLASS2 = RawIntegerSerialiser.class;
    public static final Class SUPPORTED_CLASS = Integer.class;
    private MultiSerialiserStorage mss;


    @Before
    public void setUp() throws Exception {
        mss = new MultiSerialiserStorage();
    }

    @Test
    public void shouldPutAndGet() throws Exception {
        //when
        mss.put(BYTE, SERIALISER_CLASS, SUPPORTED_CLASS);
        //then
        checkBasicPut();
    }

    @Test
    public void shouldNotRetainOldSerialiserWhenKeyIsOverWritten() throws Exception {
        //when
        mss.put(BYTE, SERIALISER_CLASS, SUPPORTED_CLASS);
        mss.put(BYTE, SERIALISER_CLASS2, SUPPORTED_CLASS);
        //then
        assertNull("old SerialiserClass value should not be found", mss.getKeyFromSerialiser(SERIALISER_CLASS));
        Byte keyFromSerialiser = mss.getKeyFromSerialiser(SERIALISER_CLASS2);
        assertNotNull("new SerialiserClass value not found", keyFromSerialiser);
        assertEquals("Wrong key for new SerialiserClass", BYTE, (byte) keyFromSerialiser);
        Class<? extends ToBytesSerialiser> actualClassFromByte = mss.getSerialiserFromKey(BYTE).getClass();
        assertNotNull("Byte key not found", actualClassFromByte);
        assertEquals("Wrong new SerialiserClass returned for key", SERIALISER_CLASS2, actualClassFromByte);
        Class<? extends ToBytesSerialiser> actualClassFromValue = mss.getSerialiserFromValue(Integer.MAX_VALUE).getClass();
        assertNotNull("Value class not found", actualClassFromValue);
        assertEquals("Wrong new SerialiserClass returned for value class", SERIALISER_CLASS2, actualClassFromValue);
    }


    @Test
    public void shouldUpdateToNewerValueToSerialiser() throws Exception {
        //give
        byte serialiserEncoding = BYTE + 1;
        //when
        mss.put(serialiserEncoding, SERIALISER_CLASS2, SUPPORTED_CLASS);
        mss.put(BYTE, SERIALISER_CLASS, SUPPORTED_CLASS);
        //then
        checkBasicPut();

        Byte keyFromSerialiser2 = mss.getKeyFromSerialiser(SERIALISER_CLASS2);
        assertNotNull("SerialiserClass value not found", keyFromSerialiser2);
        assertEquals("Wrong key for SerialiserClass", serialiserEncoding, (byte) keyFromSerialiser2);
        Class<? extends ToBytesSerialiser> actualClassFromByte2 = mss.getSerialiserFromKey(serialiserEncoding).getClass();
        assertNotNull("Byte key not found", actualClassFromByte2);
        assertEquals("Wrong SerialiserClass returned for key", SERIALISER_CLASS2, actualClassFromByte2);

        Class<? extends ToBytesSerialiser> actualClassFromValue2 = mss.getSerialiserFromValue(Integer.MAX_VALUE).getClass();
        assertNotNull("Value class not found", actualClassFromValue2);
        assertEquals("Wrong SerialiserClass, should have updated to newer SerialiserClass", SERIALISER_CLASS, actualClassFromValue2);
    }

    @Test
    public void shouldIncKey() throws Exception {
        //when
        mss.put(SERIALISER_CLASS, SUPPORTED_CLASS);
        mss.put(SERIALISER_CLASS2, SUPPORTED_CLASS);
        //then
        byte first = mss.getKeyFromSerialiser(SERIALISER_CLASS);
        byte second = mss.getKeyFromSerialiser(SERIALISER_CLASS2);
        assertEquals((byte) 0, first);
        assertEquals((byte) 1, second);
    }

    private void checkBasicPut() throws GafferCheckedException {
        Byte keyFromSerialiser = mss.getKeyFromSerialiser(SERIALISER_CLASS);
        assertNotNull("SerialiserClass value not found", keyFromSerialiser);
        assertEquals("Wrong key for SerialiserClass", BYTE, (byte) keyFromSerialiser);
        Class<? extends ToBytesSerialiser> actualClassFromByte = mss.getSerialiserFromKey(BYTE).getClass();
        assertNotNull("Byte key not found", actualClassFromByte);
        assertEquals("Wrong SerialiserClass returned for key", SERIALISER_CLASS, actualClassFromByte);
        Class<? extends ToBytesSerialiser> actualClassFromValue = mss.getSerialiserFromValue(Integer.MAX_VALUE).getClass();
        assertNotNull("Value class not found", actualClassFromValue);
        assertEquals("Wrong SerialiserClass returned for value class", SERIALISER_CLASS, actualClassFromValue);
    }

}
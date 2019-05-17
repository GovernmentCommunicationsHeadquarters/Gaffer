/*
 * Copyright 2016-2019 Crown Copyright
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

package uk.gov.gchq.gaffer.serialisation.implementation.raw;

import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.ToBytesSerialiser;

/**
 * For new properties use {@link uk.gov.gchq.gaffer.serialisation.implementation.ordered.OrderedFloatSerialiser}.
 * RawFloatSerialiser serialises Floats into an IEEE floating point little-endian byte array.
 *
 * @see uk.gov.gchq.gaffer.serialisation.implementation.ordered.OrderedFloatSerialiser
 * @deprecated this is unable to preserve object ordering.
 */
@Deprecated
public class RawFloatSerialiser implements ToBytesSerialiser<Float> {
    private static final long serialVersionUID = -8573401558869574875L;

    @Override
    public boolean canHandle(final Class clazz) {
        return Float.class.equals(clazz);
    }

    @Override
    public byte[] serialise(final Float f) throws SerialisationException {
        final byte[] out = new byte[4];
        final int value = Float.floatToRawIntBits(f);
        out[0] = (byte) (value & 255);
        out[1] = (byte) ((value >> 8) & 255);
        out[2] = (byte) ((value >> 16) & 255);
        out[3] = (byte) ((value >> 24) & 255);
        return out;
    }

    @Override
    public Float deserialise(final byte[] allBytes, final int offset, final int length) throws SerialisationException {
        int carriage = offset;
        return Float.intBitsToFloat((int) (allBytes[carriage++] & 255L
                | (allBytes[carriage++] & 255L) << 8
                | (allBytes[carriage++] & 255L) << 16
                | (allBytes[carriage] & 255L) << 24));
    }

    @Override
    public Float deserialise(final byte[] bytes) throws SerialisationException {
        return deserialise(bytes, 0, bytes.length);
    }

    @Override
    public Float deserialiseEmpty() {
        return null;
    }

    @Override
    public boolean preservesObjectOrdering() {
        return true;
    }

    @Override
    public boolean isConsistent() {
        return true;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj != null && this.getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return RawFloatSerialiser.class.getName().hashCode();
    }
}

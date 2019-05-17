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
package uk.gov.gchq.gaffer.operation.impl;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.io.MultiInput;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;

import java.util.Map;

/**
 * The {@code SplitStoreFromIterable} operation is for splitting a store
 * based on an iterable of split points.
 *
 * @param <T> the type of splits
 * @see SplitStoreFromIterable.Builder
 */
@JsonPropertyOrder(value = {"class", "input"}, alphabetic = true)
@Since("1.1.1")
@Summary("Splits a store based on an iterable of split points")
public class SplitStoreFromIterable<T> implements Operation,
        MultiInput<T> {
    private Iterable<? extends T> input;
    private Map<String, String> options;

    @Override
    public SplitStoreFromIterable<T> shallowClone() {
        return new SplitStoreFromIterable.Builder<T>()
                .input(input)
                .options(options)
                .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SplitStoreFromIterable<?> that = (SplitStoreFromIterable<?>) o;

        return new EqualsBuilder()
                .append(input, that.input)
                .append(options, that.options)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(input)
                .append(options)
                .toHashCode();
    }

    @Override
    public Iterable<? extends T> getInput() {
        return input;
    }

    @Override
    public void setInput(final Iterable<? extends T> input) {
        this.input = input;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }


    public static class Builder<T> extends Operation.BaseBuilder<SplitStoreFromIterable<T>, Builder<T>>
            implements MultiInput.Builder<SplitStoreFromIterable<T>, T, Builder<T>> {
        public Builder() {
            super(new SplitStoreFromIterable<>());
        }
    }
}

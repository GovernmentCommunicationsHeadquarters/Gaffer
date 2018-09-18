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

package uk.gov.gchq.gaffer.operation.impl;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.exception.CloneFailedException;

import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.io.InputOutput;
import uk.gov.gchq.gaffer.operation.io.MultiInput;
import uk.gov.gchq.gaffer.operation.serialisation.TypeReferenceImpl;
import uk.gov.gchq.gaffer.operation.util.join.JoinType;
import uk.gov.gchq.gaffer.operation.util.matcher.Matcher;
import uk.gov.gchq.gaffer.operation.util.matcher.MatchingOn;
import uk.gov.gchq.gaffer.operation.util.reducer.Reducer;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;

import java.util.Map;

@Since("1.7.0")
@Summary("Joins two iterables based on a join type")
@JsonPropertyOrder(value = {"input", "operation", "matchFunction", "joinType", "options"}, alphabetic = true)
public class Join<I, O> implements InputOutput<Iterable<? extends I>, Iterable<? extends O>>, MultiInput<I> {
    private Iterable<? extends I> leftSideInput;
    private Operation rightSideOperation;
    private Matcher matcher;
    private MatchingOn matchingOn;
    private Reducer reducer;
    private JoinType joinType;
    private Map<String, String> options;

    @Override
    public Iterable<? extends I> getInput() {
        return leftSideInput;
    }

    @Override
    public void setInput(final Iterable<? extends I> leftSideInput) {
        this.leftSideInput = leftSideInput;
    }

    public Operation getOperation() {
        return rightSideOperation;
    }

    public void setOperation(final Operation rightSideOperation) {
        this.rightSideOperation = rightSideOperation;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public MatchingOn getMatchingOn() {
        return matchingOn;
    }

    public void setMatchingOn(final MatchingOn matchingOn) {
        this.matchingOn = matchingOn;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public Reducer getReducer() {
        return reducer;
    }

    public void setReducer(final Reducer reducer) {
        this.reducer = reducer;
    }

    @Override
    public Join<I, O> shallowClone() throws CloneFailedException {
        return new Join.Builder<I, O>()
                .input(leftSideInput)
                .operation(rightSideOperation)
                .matcher(matcher)
                .matchingOn(matchingOn)
                .joinType(joinType)
                .reducer(reducer)
                .options(options)
                .build();
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    @Override
    public TypeReference<Iterable<? extends O>> getOutputTypeReference() {
        return TypeReferenceImpl.createIterableT();
    }

    public static final class Builder<I, O>
            extends BaseBuilder<Join<I, O>, Builder<I, O>>
            implements InputOutput.Builder<Join<I, O>,
            Iterable<? extends I>, Iterable<? extends O>,
            Builder<I, O>>,
            MultiInput.Builder<Join<I, O>, I, Builder<I, O>> {
        public Builder() {
            super(new Join<>());
        }

        public Builder<I, O> operation(Operation operation) {
            _getOp().setOperation(operation);
            return _self();
        }

        public Builder<I, O> matcher(Matcher matcher) {
            _getOp().setMatcher(matcher);
            return _self();
        }

        public Builder<I, O> joinType(JoinType joinType) {
            _getOp().setJoinType(joinType);
            return _self();
        }

        public Builder<I, O> reducer(Reducer reducer) {
            _getOp().setReducer(reducer);
            return _self();
        }

        public Builder<I, O> matchingOn(MatchingOn matchingOn) {
            _getOp().setMatchingOn(matchingOn);
            return _self();
        }

    }
}
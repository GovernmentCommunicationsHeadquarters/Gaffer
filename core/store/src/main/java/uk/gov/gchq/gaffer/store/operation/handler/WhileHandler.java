/*
 * Copyright 2017 Crown Copyright
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
package uk.gov.gchq.gaffer.store.operation.handler;

import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.While;
import uk.gov.gchq.gaffer.operation.io.Output;
import uk.gov.gchq.gaffer.operation.util.OperationConstants;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;

import static uk.gov.gchq.gaffer.store.operation.handler.util.OperationHandlerUtil.getResultsOrNull;
import static uk.gov.gchq.gaffer.store.operation.handler.util.OperationHandlerUtil.updateOperationInput;

/**
 * An operation handler for {@link While} operations.
 *
 */
public class WhileHandler implements OutputOperationHandler<While, Object> {

    @Override
    public Object doOperation(final While operation, final Context context, final Store store) throws OperationException {
        Object input = operation.getInput();
        final Operation delegate = operation.getOperation();

        final int maxRepeats = Integer.min(OperationConstants.MAX_REPEATS_DEFAULT,
                operation.getRepeats());

        boolean satisfied = null == operation.isCondition() ? true : operation.isCondition();
        int repeatCount = 0;

        while (satisfied && repeatCount < maxRepeats) {
            if (null != operation.getConditional()) {
                final Operation transform = operation.getConditional().getTransform();
                if (null != transform) {
                    updateOperationInput(transform, input);
                    input = getResultsOrNull(transform, context, store);
                }

                try {
                    satisfied = operation.getConditional().getPredicate().test(input);
                } catch (final ClassCastException e) {
                    final String inputType = null != input ? input.getClass().getSimpleName() : "null";
                    throw new OperationException("The predicate '" + operation.getConditional().getPredicate().getClass().getSimpleName()
                            + "' cannot accept an input of type '" + inputType + "'");
                }
            }

            if (delegate instanceof Output) {
                input = store.execute((Output) delegate, context);
            } else {
                store.execute(delegate, context);
            }
            repeatCount++;
        }

        return input;
    }
}

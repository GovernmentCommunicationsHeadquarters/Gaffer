/*
 * Copyright 2021 Crown Copyright
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

package uk.gov.gchq.gaffer.federatedstore.operation;

import com.fasterxml.jackson.annotation.JsonGetter;

import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.koryphe.Since;

/**
 * {@link IFederationOperation} interface is for special operations used to configure/manipulate/control federation.
 * It has no inteded function outside of federation and should only be handled by the {@link uk.gov.gchq.gaffer.federatedstore.FederatedStore}.
 */
@Since("2.0.0")
public interface IFederationOperation extends Operation {
    boolean isUserRequestingAdminUsage();

    @JsonGetter("isUserRequestingAdminUsage")
    default Boolean _isUserRequestingAdminUsageOrNull() {
        return isUserRequestingAdminUsage() ? true : null;
    }

    Operation setIsUserRequestingAdminUsage(final boolean adminRequest);


}

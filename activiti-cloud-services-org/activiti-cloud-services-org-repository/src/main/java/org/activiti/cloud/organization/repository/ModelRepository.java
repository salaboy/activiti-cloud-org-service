/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.organization.repository;

import java.util.Optional;

import org.activiti.cloud.organization.api.Project;
import org.activiti.cloud.organization.api.Model;
import org.activiti.cloud.organization.api.ModelType;
import org.activiti.cloud.services.common.file.FileContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface for {@link Model} entities repository
 */
public interface ModelRepository<P extends Project, M extends Model<P, ?>> {

    Page<M> getModels(P project,
                      ModelType modelTypeFilter,
                      Pageable pageable);

    Optional<M> findModelById(String modelId);

    byte[] getModelContent(M model);

    byte[] getModelExport(M model);

    M createModel(M model);

    M updateModel(M modelToUpdate,
                  M newModel);

    M updateModelContent(M modelToBeUpdate,
                         FileContent fileContent);

    void deleteModel(M model);

    Class<M> getModelType();
}

/*
 * Copyright 2019 Alfresco, Inc. and/or its affiliates.
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
package org.activiti.cloud.services.organization.validation;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;

import org.activiti.bpmn.exceptions.XMLException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.cloud.organization.api.ModelType;
import org.activiti.cloud.organization.api.ModelValidationError;
import org.activiti.cloud.organization.api.ModelValidator;
import org.activiti.cloud.organization.api.ProcessModelType;
import org.activiti.cloud.organization.core.error.SemanticModelValidationException;
import org.activiti.cloud.organization.core.error.SyntacticModelValidationException;
import org.activiti.cloud.services.organization.converter.ProcessModelContentConverter;
import org.activiti.validation.ProcessValidator;
import org.activiti.validation.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.activiti.cloud.services.common.util.ContentTypeUtils.CONTENT_TYPE_XML;

/**
 * {@link ModelValidator} implementation of process models
 */
@Component
public class ProcessModelValidator implements ModelValidator {

    private final Logger log = LoggerFactory.getLogger(ProcessModelValidator.class);

    private final ProcessModelType processModelType;

    private final ProcessValidator processValidator;

    private final ProcessModelContentConverter processModelContentConverter;

    @Autowired
    public ProcessModelValidator(ProcessModelType processModelType,
                                 ProcessValidator processValidator,
                                 ProcessModelContentConverter processModelContentConverter) {
        this.processModelType = processModelType;
        this.processValidator = processValidator;
        this.processModelContentConverter = processModelContentConverter;
    }

    @Override
    public void validateModelContent(byte[] bytes) {
        try {
            BpmnModel bpmnModel = processModelContentConverter.convertToBpmnModel(bytes);

            List<ValidationError> validationErrors = processValidator.validate(bpmnModel);
            if (!validationErrors.isEmpty()) {
                log.error("Semantic process model validation errors encountered: " + validationErrors);
                throw new SemanticModelValidationException(validationErrors
                                                                   .stream()
                                                                   .map(this::toModelValidationError)
                                                                   .collect(Collectors.toList()));
            }
        } catch (IOException | XMLStreamException | XMLException ex) {
            Throwable errorCause = Optional.ofNullable(ex.getCause())
                    .filter(XMLStreamException.class::isInstance)
                    .orElse(ex);
            log.error("Syntactic process model XML validation errors encountered: " + errorCause);
            throw new SyntacticModelValidationException(errorCause);
        }
    }

    private ModelValidationError toModelValidationError(ValidationError validationError) {
        ModelValidationError modelValidationError = new ModelValidationError();
        modelValidationError.setWarning(validationError.isWarning());
        modelValidationError.setProblem(validationError.getProblem());
        modelValidationError.setDescription(validationError.getDefaultDescription());
        modelValidationError.setValidatorSetName(validationError.getValidatorSetName());
        return modelValidationError;
    }

    @Override
    public ModelType getHandledModelType() {
        return processModelType;
    }

    @Override
    public String getHandledContentType() {
        return CONTENT_TYPE_XML;
    }
}

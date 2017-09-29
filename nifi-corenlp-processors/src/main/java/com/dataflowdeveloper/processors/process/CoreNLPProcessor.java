/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dataflowdeveloper.processors.process;

import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.*;
   
@Tags({"corenlpprocessor"})
@CapabilityDescription("Run Stanford CoreNLP Sentiment Analysis")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class CoreNLPProcessor extends AbstractProcessor {

	public static final String ATTRIBUTE_OUTPUT_NAME = "sentiment";
	public static final String ATTRIBUTE_INPUT_NAME = "sentence";
	public static final String PROPERTY_NAME_EXTRA = "Extra Resources";
	
    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor
            .Builder().name(ATTRIBUTE_INPUT_NAME)
            .description("A sentence to parse, such as a Tweet.")
            .required(true)
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();
    
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully determine sentiment.")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Failed to determine sentiment.")
            .build();

    
    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    private SentimentService service;
    
    @Override
    protected void init(final ProcessorInitializationContext context) {
        service = new SentimentService();

        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(MY_PROPERTY);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
    	return;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            getLogger().error("Got empty FlowFile");
        	flowFile = session.create();
        }               
		try {

				flowFile.getAttributes();

	            String sentence = flowFile.getAttribute(ATTRIBUTE_INPUT_NAME);

	            String sentence2 = context.getProperty(ATTRIBUTE_INPUT_NAME).evaluateAttributeExpressions(flowFile).getValue();
	            
	            if ( sentence == null) {   
	            	sentence = sentence2;
	            }
	            if ( sentence == null) {
                    getLogger().error("Unable to process empty sentence");
                    session.transfer(flowFile, REL_FAILURE);
	            	return;
	            }

                String value = null;
	            Object object = service.getSentiment( sentence );
	            if (object.getClass().equals(String.class)) {
                    value = (String) object;
                } else {
	                Exception e = (Exception) object;
                    getLogger().error("Unable to process sentence due to CoreNLP Exception:");
                    getLogger().error("{} failed to process due to {}; Transferring file to failure stream", new Object[]{this, e});
                    session.transfer(flowFile, REL_FAILURE);
                    return;
                }
	        	
	        	if ( value == null) {
                    getLogger().error("An unknown error has occurred! Transferring file to failure stream");
                    session.transfer(flowFile, REL_FAILURE);
                    return;
	        	}

			flowFile = session.putAttribute(flowFile, "mime.type", "application/json");
			flowFile = session.putAttribute(flowFile, ATTRIBUTE_OUTPUT_NAME, value);

			session.transfer(flowFile, REL_SUCCESS);
			session.commit();
		   } catch (final Throwable t) {
			   getLogger().error("Unable to process Sentiment Processor file " + t.getLocalizedMessage()) ;
			   getLogger().error("{} failed to process due to {}; rolling back session", new Object[]{this, t});
	            throw t;
		}
    }



}

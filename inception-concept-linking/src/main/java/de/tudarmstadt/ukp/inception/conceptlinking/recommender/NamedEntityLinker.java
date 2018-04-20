/*
 * Copyright 2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tudarmstadt.ukp.inception.conceptlinking.recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudarmstadt.ukp.clarin.webanno.api.DocumentService;
import de.tudarmstadt.ukp.clarin.webanno.model.AnnotationDocument;
import de.tudarmstadt.ukp.clarin.webanno.model.Project;
import de.tudarmstadt.ukp.clarin.webanno.model.SourceDocument;
import de.tudarmstadt.ukp.clarin.webanno.security.model.User;
import de.tudarmstadt.ukp.inception.conceptlinking.service.ConceptLinkingService;
import de.tudarmstadt.ukp.inception.kb.KnowledgeBaseService;
import de.tudarmstadt.ukp.inception.kb.graph.KBHandle;
import de.tudarmstadt.ukp.inception.kb.model.KnowledgeBase;
import de.tudarmstadt.ukp.inception.recommendation.imls.conf.ClassifierConfiguration;
import de.tudarmstadt.ukp.inception.recommendation.imls.core.classifier.Classifier;
import de.tudarmstadt.ukp.inception.recommendation.imls.core.dataobjects.AnnotationObject;
import de.tudarmstadt.ukp.inception.recommendation.imls.core.dataobjects.Offset;
import de.tudarmstadt.ukp.inception.recommendation.imls.core.dataobjects.TokenObject;

public class NamedEntityLinker

    extends Classifier<Object>
{
    private Logger log = LoggerFactory.getLogger(getClass());

    private int tokenId = 0;
    private User user;
    private Project project;
    private final String feature = "identifier";

    private Set<AnnotationObject> nerAnnotations = new HashSet<>();

    private KnowledgeBaseService kbService;
    private ConceptLinkingService clService;
    private DocumentService documentService;

    public NamedEntityLinker(ClassifierConfiguration<Object> conf, KnowledgeBaseService kbService,
        ConceptLinkingService clService, DocumentService documentService)
    {
        super(conf);
        this.kbService = kbService;
        this.clService = clService;
        this.documentService = documentService;
        this.conf.setNumPredictions(3);
    }

    @Override
    public void reconfigure()
    {

    }

    @Override
    public void setModel(Object aModel)
    {
        if (aModel instanceof Set) {
            nerAnnotations = (Set<AnnotationObject>) aModel;
        }
        else {
            log.error("Expected model type: Set<TokenObject> - but was: [{}]",
                aModel != null ? aModel.getClass() : aModel);
        }
    }

    @Override
    public void setUser(User user)
    {
        this.user = user;
    }

    @Override
    public void setProject(Project project)
    {
        this.project = project;
    }

    /**
     *
     * @param inputData
     *            All sentences to predict annotations for.
     * @param <T>
     * @return Predicted sentence.
     *         Outer list: sentence level
     *         Middle list: word level
     *         Inner list: token level (predictions for each token)
     */
    @Override
    public <T extends TokenObject> List<List<List<AnnotationObject>>> predictSentences(
        List<List<T>> inputData)
    {
        List<List<List<AnnotationObject>>> result = new ArrayList<>();

        for (List<T> sentence : inputData) {
            List<List<AnnotationObject>> annotatedSentence = new ArrayList<>();
            int sentenceIndex = 0;
            while (sentenceIndex < sentence.size() - 1) {
                TokenObject token = sentence.get(sentenceIndex);
                List<AnnotationObject> word;

                if (isNamedEntity(token)) {
                    StringBuilder coveredText = new StringBuilder(token.getCoveredText());
                    int endCharacter = token.getOffset().getEndCharacter();
                    int endToken = token.getOffset().getEndToken();

                    TokenObject nextTokenObject = sentence.get(sentenceIndex + 1);
                    while (isNamedEntity(nextTokenObject)) {
                        coveredText.append(" ").append(nextTokenObject.getCoveredText());
                        endCharacter = nextTokenObject.getOffset().getEndCharacter();
                        endToken = nextTokenObject.getOffset().getEndToken();
                        sentenceIndex++;
                        nextTokenObject = sentence.get(sentenceIndex + 1);
                    }

                    token.setCoveredText(coveredText.toString());
                    token.setOffset(new Offset(token.getOffset().getBeginCharacter(), endCharacter,
                        token.getOffset().getBeginToken(), endToken));
                    word = predictToken(token);
                    annotatedSentence.add(word);
                }
                sentenceIndex++;
            }
            result.add(annotatedSentence);
        }
        return result;
    }

    private List<AnnotationObject> predictToken(TokenObject token)
    {
        List<KBHandle> handles = new ArrayList<>();
        for (KnowledgeBase kb : kbService.getKnowledgeBases(project)) {
            if (kb.isSupportConceptLinking()) {
                handles.addAll(kbService.read(kb, (conn) -> {
                    SourceDocument doc = documentService
                        .getSourceDocument(project, token.getDocumentName());
                    AnnotationDocument annoDoc = documentService
                        .createOrGetAnnotationDocument(doc, user);
                    JCas jCas;
                    try {
                        jCas = documentService.readAnnotationCas(annoDoc);
                        return clService.disambiguate(kb, null, token.getCoveredText(),
                            token.getOffset().getBeginCharacter(), jCas);
                    }
                    catch (IOException e) {
                        log.error("An error occurred while retrieving entity candidates.", e);
                        return Collections.emptyList();
                    }
                }));
            }
        }

        List<AnnotationObject> predictions = new ArrayList<>();

        handles.stream()
            .limit(conf.getNumPredictions())
            .forEach(h -> predictions.add(
            new AnnotationObject(h.getIdentifier(), h.getDescription(), token, null, tokenId++,
                feature, "NamedEntityLinker")));

        return predictions;

    }

    private boolean isNamedEntity(TokenObject token)
    {
        return nerAnnotations.stream()
            .map(TokenObject::getOffset)
            .anyMatch(t -> t.equals(token.getOffset()));
    }
}

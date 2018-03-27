/*
 * Copyright 2017
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
package de.tudarmstadt.ukp.inception.ui.kb.project;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.form.upload.FileUpload;

import de.tudarmstadt.ukp.inception.kb.KnowledgeBase;

/**
 * Utility class which combines relevant inputs in a single class (so that a single Wicket Model can
 * be used for the wizard).
 */
public class EnrichedKnowledgeBase implements Serializable {

    private static final long serialVersionUID = 4639345743242356537L;

    protected KnowledgeBase kb;
    protected String url;
    protected transient List<FileUpload> files;
    
    public KnowledgeBase getKb() {
        return kb;
    }
    
    public void setKb(KnowledgeBase kb) {
        this.kb = kb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<FileUpload> getFiles() {
        return files;
    }

    public void setFiles(List<FileUpload> files) {
        this.files = files;
    }

}

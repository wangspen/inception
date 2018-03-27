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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import de.tudarmstadt.ukp.clarin.webanno.support.lambda.AjaxCallback;

public abstract class ListPanel_ImplBase
    extends Panel
{
    private static final long serialVersionUID = 414323323271754324L;
    
    private AjaxCallback changeAction;
    private AjaxCallback createAction;

    public ListPanel_ImplBase(final String id, final IModel<?> model)
    {
        super(id, model);
    }

    protected void actionCreate(AjaxRequestTarget aTarget) throws Exception
    {
        onCreate(aTarget);
        onChange(aTarget);
    }

    protected void onChange(AjaxRequestTarget aTarget) throws Exception
    {
        if (changeAction != null) {
            changeAction.accept(aTarget);
        }
    }

    public ListPanel_ImplBase setChangeAction(AjaxCallback aAction)
    {
        changeAction = aAction;
        return this;
    }

    protected void onCreate(AjaxRequestTarget aTarget) throws Exception
    {
        if (createAction != null) {
            createAction.accept(aTarget);
        }
    }

    public ListPanel_ImplBase setCreateAction(AjaxCallback aAction)
    {
        createAction = aAction;
        return this;
    }
}

/**
 * eobjects.org DataCleaner
 * Copyright (C) 2010 eobjects.org
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.eobjects.datacleaner.monitor.query.widgets;

import org.eobjects.datacleaner.monitor.shared.model.TenantIdentifier;
import org.eobjects.datacleaner.monitor.shared.widgets.HeadingLabel;
import org.eobjects.datacleaner.monitor.shared.widgets.LoadingIndicator;
import org.eobjects.datacleaner.monitor.util.DCRequestBuilder;
import org.eobjects.datacleaner.monitor.util.DCRequestCallback;
import org.eobjects.datacleaner.monitor.util.ErrorHandler;
import org.eobjects.datacleaner.monitor.util.Urls;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Main panel of the query module - contains the text area for the query, the
 * button to fire it and the area for the resulting table view.
 */
public class QueryPanel extends FlowPanel {

    private final TextArea _queryTextArea;
    private final String _datastoreName;
    private final TenantIdentifier _tenant;
    private final Button _executeQueryButton;
    private final HTML _resultPanel;
    private final LoadingIndicator _loadingIcon;

    public QueryPanel(TenantIdentifier tenant, String datastoreName) {
        super();
        setStyleName("QueryPanel");

        _tenant = tenant;
        _datastoreName = datastoreName;
        _queryTextArea = new TextArea();
        _queryTextArea.setText("SELECT *\nFROM [TABLE]\nLIMIT 50");

        _executeQueryButton = new Button("Ok");
        _executeQueryButton.addStyleName("ExecuteQueryButton");

        _loadingIcon = new LoadingIndicator();
        _loadingIcon.setVisible(false);
        _resultPanel = new HTML();
        _resultPanel.addStyleName("QueryResultPanel");

        final String url = Urls.createRepositoryUrl(_tenant, "datastores/" + _datastoreName + ".query");

        _executeQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final DCRequestBuilder requestBuilder = new DCRequestBuilder(RequestBuilder.POST, url);
                final String query = getCurrentQuery();
                _resultPanel.setVisible(false);
                _loadingIcon.setVisible(true);
                requestBuilder.send(query, new DCRequestCallback() {
                    @Override
                    protected void onSuccess(Request request, Response response) {
                        final String resultText = response.getText();
                        _resultPanel.setHTML(resultText);
                        _resultPanel.setVisible(true);
                    }

                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        _loadingIcon.setVisible(false);
                        if (response.getStatusCode() == 400) {
                            ErrorHandler.showErrorDialog("Failed to handle query!", response.getStatusText(), (String) null);
                            return;
                        }
                        super.onResponseReceived(request, response);
                    }
                });
            }
        });

        add(new HeadingLabel("Query datastore: " + _datastoreName));
        add(new Label("Please fill in your query below and click the 'Ok' button to execute it on the server."));
        add(_queryTextArea);
        add(_executeQueryButton);
        add(_loadingIcon);
        add(_resultPanel);
    }

    private String getCurrentQuery() {
        String query = _queryTextArea.getText();
        query = query.trim();
        query = query.replaceAll("\r", " ");
        query = query.replaceAll("\n", " ");
        return query;
    }

}

/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.gwt.client.ui.contenteditor;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.I_CmsEditableData;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsCancelCloseException;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.FormElement;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * XML content editor dialog.<p>
 * 
 * @since 8.0.0
 */
public final class CmsContentEditorDialog {

    /** Name of exported dialog close function. */
    private static final String CLOSING_METHOD_NAME = "cms_ade_closeEditorDialog";

    /** Name attribute value for editor iFrame. */
    private static final String EDITOR_IFRAME_NAME = "cmsAdvancedDirectEditor";

    /** The dialog instance. */
    private static CmsContentEditorDialog INSTANCE;

    /** The popup instance. */
    private CmsPopup m_dialog;

    /** The currently edit element data. */
    private I_CmsEditableData m_editableData;

    /** The editor handler. */
    private I_CmsContentEditorHandler m_editorHandler;

    /** The form element. */
    private FormElement m_form;

    /** Flag indicating if a new resource needs to created. */
    private boolean m_isNew;

    /**
     * Hiding constructor.<p>
     */
    private CmsContentEditorDialog() {

        exportClosingMethod();
    }

    /**
     * Returns the dialogs instance.<p>
     * 
     * @return the dialog instance
     */
    public static CmsContentEditorDialog get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsContentEditorDialog();
        }
        return INSTANCE;
    }

    /**
     * Closes the dialog.<p>
     */
    static void closeEditDialog() {

        get().close();

    }

    /**
     * Opens the content editor dialog for the given element.<p>
     * 
     * @param editableData the editable data
     * @param isNew <code>true</code> when creating a new resource
     * @param editorHandler the editor handler
     */
    public void openEditDialog(
        final I_CmsEditableData editableData,
        boolean isNew,
        I_CmsContentEditorHandler editorHandler) {

        if ((m_dialog != null) && m_dialog.isShowing()) {
            CmsDebugLog.getInstance().printLine("Dialog is already open, cannot open another one.");
            return;
        }
        m_isNew = isNew;
        m_editableData = editableData;
        m_editorHandler = editorHandler;
        if (m_isNew || (editableData.getStructureId() == null)) {
            openDialog();
        } else {
            CmsRpcAction<String> action = new CmsRpcAction<String>() {

                @Override
                public void execute() {

                    show(true);
                    CmsCoreProvider.getVfsService().getSitePath(getEditableData().getStructureId(), this);
                }

                @Override
                protected void onResponse(String result) {

                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(result)) {
                        getEditableData().setSitePath(result);
                        openDialog();
                    } else {
                        CmsAlertDialog alert = new CmsAlertDialog(
                            Messages.get().key(Messages.ERR_TITLE_ERROR_0),
                            Messages.get().key(Messages.ERR_RESOURCE_UNAVAILABLE_1, getEditableData().getSitePath()));
                        alert.center();
                    }
                    stop(false);
                }
            };
            action.execute();
        }

    }

    /**
     * Closes the dialog.<p>
     */
    protected void close() {

        if (m_dialog != null) {
            m_dialog.hide();
            m_dialog = null;
            m_editorHandler.onClose(m_editableData.getSitePath(), m_isNew);
            m_editorHandler = null;
        }
        if (m_form != null) {
            m_form.removeFromParent();
            m_form = null;
        }
    }

    /**
     * Returns the editable data.<p>
     * 
     * @return the editable data
     */
    protected I_CmsEditableData getEditableData() {

        return m_editableData;
    }

    /**
     * Opens the dialog for the given sitepath.<p>
     */
    protected void openDialog() {

        m_dialog = new CmsPopup(Messages.get().key(Messages.GUI_DIALOG_CONTENTEDITOR_TITLE_0)
            + " - "
            + (m_isNew ? m_editableData.getNewTitle() : m_editableData.getSitePath()));
        m_dialog.addStyleName(I_CmsLayoutBundle.INSTANCE.contentEditorCss().contentEditor());

        // calculate width
        int width = Window.getClientWidth();
        width = (width < 1350) ? width - 50 : 1300;
        m_dialog.setWidth(width);

        // calculate height
        int height = Window.getClientHeight() - 50;
        height = (height < 645) ? 645 : height;
        m_dialog.setHeight(height);

        m_dialog.setGlassEnabled(true);
        m_dialog.setUseAnimation(false);
        CmsIFrame editorFrame = new CmsIFrame(EDITOR_IFRAME_NAME, "");
        m_dialog.addDialogClose(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                CmsConfirmDialog confirmDlg = new CmsConfirmDialog(Messages.get().key(
                    Messages.GUI_EDITOR_CLOSE_CAPTION_0), Messages.get().key(Messages.GUI_EDITOR_CLOSE_TEXT_0));
                confirmDlg.setHandler(new I_CmsConfirmDialogHandler() {

                    public void onClose() {

                        // do nothing
                    }

                    public void onOk() {

                        CmsContentEditorDialog.this.close();
                    }
                });
                confirmDlg.center();
                // Let the confirm dialog handle the closing
                throw new CmsCancelCloseException();
            }
        });

        m_dialog.add(editorFrame);
        m_dialog.center();
        m_form = generateForm();
        RootPanel.getBodyElement().appendChild(m_form);
        m_form.submit();
    }

    /**
     * Exports the close method to the window object, so it can be accessed from within the content editor iFrame.<p>
     */
    private native void exportClosingMethod() /*-{
        $wnd[@org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog::CLOSING_METHOD_NAME] = function() {
            @org.opencms.gwt.client.ui.contenteditor.CmsContentEditorDialog::closeEditDialog()();
        };
    }-*/;

    /**
     * Generates the form to post to the editor frame.<p>
     * 
     * @return the form element
     */
    private FormElement generateForm() {

        // create a form to submit a post request to the editor JSP
        Map<String, String> formVaules = new HashMap<String, String>();
        if (m_editableData.getSitePath() != null) {
            formVaules.put("resource", m_editableData.getSitePath());
        }
        if (m_editableData.getElementLanguage() != null) {
            formVaules.put("elementlanguage", m_editableData.getElementLanguage());
        }
        if (m_editableData.getElementName() != null) {
            formVaules.put("elementname", m_editableData.getElementName());
        }
        formVaules.put("backlink", CmsCoreProvider.get().getContentEditorBacklinkUrl());
        formVaules.put("redirect", "true");
        formVaules.put("directedit", "true");
        if (m_isNew) {
            formVaules.put("newlink", m_editableData.getNewLink());
            formVaules.put("editortitle", m_editableData.getNewTitle());
        }
        FormElement formElement = CmsDomUtil.generateHiddenForm(CmsCoreProvider.get().link(
            CmsCoreProvider.get().getContentEditorUrl()), "post", EDITOR_IFRAME_NAME, formVaules);
        return formElement;
    }
}

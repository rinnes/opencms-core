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

package org.opencms.ade.containerpage.client.ui;

import org.opencms.ade.containerpage.client.CmsContainerpageController;
import org.opencms.ade.containerpage.client.CmsContainerpageHandler;
import org.opencms.ade.containerpage.client.Messages;
import org.opencms.ade.containerpage.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsContainerElementData;
import org.opencms.ade.containerpage.shared.CmsGroupContainer;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsPositionBean;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The group-container editor.<p>
 * 
 * @since 8.0.0
 */
public final class CmsGroupcontainerEditor extends Composite {

    /** The ui-binder interface for this widget. */
    interface I_CmsGroupcontainerEditorUiBinder extends UiBinder<HTMLPanel, CmsGroupcontainerEditor> {
        // GWT interface, nothing to do here
    }

    /** The current group container instance. */
    private static CmsGroupcontainerEditor INSTANCE;

    /** The ui-binder for this widget. */
    private static I_CmsGroupcontainerEditorUiBinder uiBinder = GWT.create(I_CmsGroupcontainerEditorUiBinder.class);

    /** The dialog element. */
    @UiField
    protected HTMLPanel m_dialogContent;

    /** The description input. */
    @UiField
    protected CmsTextBox m_inputDescription;

    /** The title input. */
    @UiField
    protected CmsTextBox m_inputTitle;

    /** The descriptionLabel. */
    @UiField
    protected CmsLabel m_labelDescription;

    /** The title label. */
    @UiField
    protected CmsLabel m_labelTitle;

    /** The overlay div element. */
    @UiField
    protected DivElement m_overlayDiv;

    /** The container marker div element. */
    @UiField
    protected DivElement m_containerMarker;

    /** List of elements when editing started, use to restore on cancel. */
    private List<CmsContainerPageElement> m_backUpElements;

    /** The container-page controller. */
    private CmsContainerpageController m_controller;

    /** The group-container place-holder. */
    private Element m_editingPlaceholder;

    /** The editor popup dialog. */
    private CmsConfirmDialog m_editorDialog;

    /** The editor HTML-id. */
    private String m_editorId;

    /** The editor widget. */
    private HTMLPanel m_editorWidget;

    /** The container element data. */
    private CmsContainerElementData m_elementData;

    /** The group-container. */
    private CmsGroupContainerElement m_groupContainer;

    /** The group container bean. */
    private CmsGroupContainer m_groupContainerBean;

    /** The group container element position. */
    private CmsPositionBean m_groupContainerPosition;

    /** The index position of the group-container inside it's parent. */
    private int m_indexPosition;

    /** The parent container. */
    private CmsContainerPageContainer m_parentContainer;

    /**
     * Constructor.<p>
     * 
     * @param groupContainer the group-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    private CmsGroupcontainerEditor(
        CmsGroupContainerElement groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        m_controller = controller;
        m_editorWidget = uiBinder.createAndBindUi(this);
        initWidget(m_editorWidget);
        m_overlayDiv.getStyle().setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexHighlighting());
        m_labelDescription.setText(Messages.get().key(Messages.GUI_GROUPCONTAINER_LABEL_DESCRIPTION_0));
        m_labelTitle.setText(Messages.get().key(Messages.GUI_GROUPCONTAINER_LABEL_TITLE_0));
        m_editorId = HTMLPanel.createUniqueId();
        m_editorWidget.getElement().setId(m_editorId);
        m_groupContainer = groupContainer;
        m_backUpElements = new ArrayList<CmsContainerPageElement>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                m_backUpElements.add((CmsContainerPageElement)w);
            }
        }
        m_parentContainer = (CmsContainerPageContainer)m_groupContainer.getParentTarget();
        m_groupContainerPosition = CmsPositionBean.generatePositionInfo(m_groupContainer);
        m_editingPlaceholder = createPlaceholder(m_groupContainer.getElement());
        m_groupContainer.setEditingPlaceholder(m_editingPlaceholder);
        m_groupContainer.setEditingMarker(m_containerMarker);
        m_indexPosition = m_parentContainer.getWidgetIndex(m_groupContainer);
        // inserting placeholder element
        m_parentContainer.getElement().insertBefore(m_editingPlaceholder, m_groupContainer.getElement());
        m_editorWidget.add(m_groupContainer, m_editorId);
        Style style = m_groupContainer.getElement().getStyle();
        style.setPosition(Position.ABSOLUTE);
        style.setLeft(m_groupContainerPosition.getLeft(), Unit.PX);
        style.setTop(m_groupContainerPosition.getTop(), Unit.PX);
        style.setWidth(m_groupContainerPosition.getWidth(), Unit.PX);
        style.setZIndex(I_CmsLayoutBundle.INSTANCE.constants().css().zIndexPopup());
        m_containerMarker.getStyle().setLeft(m_groupContainerPosition.getLeft() - 3, Unit.PX);
        m_containerMarker.getStyle().setTop(m_groupContainerPosition.getTop() - 4, Unit.PX);
        m_containerMarker.getStyle().setWidth(m_groupContainerPosition.getWidth() + 4, Unit.PX);
        m_containerMarker.getStyle().setHeight(m_groupContainerPosition.getHeight() + 4, Unit.PX);
        m_groupContainer.getElementOptionBar().setVisible(false);
        m_groupContainer.getElementOptionBar().removeStyleName(
            I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().cmsHovering());

        RootPanel.get().addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerEditing());

        // Loading data of all contained elements including group-container element
        m_controller.getElements(getElementIds(), new I_CmsSimpleCallback<Map<String, CmsContainerElementData>>() {

            public void execute(Map<String, CmsContainerElementData> arg) {

                setGroupContainerData(arg);
            }
        });
    }

    /**
     * Opens the group-container editor.<p>
     * 
     * @param groupContainer the group-container
     * @param controller the container-page controller
     * @param handler the container-page handler
     */
    public static void openGroupcontainerEditor(
        CmsGroupContainerElement groupContainer,
        CmsContainerpageController controller,
        CmsContainerpageHandler handler) {

        // making sure only a single instance of the group-container editor is open
        if (INSTANCE != null) {
            CmsDebugLog.getInstance().printLine("group-container editor already open");
        } else {
            if (controller.startEditingGroupcontainer(groupContainer)) {
                INSTANCE = new CmsGroupcontainerEditor(groupContainer, controller, handler);
                RootPanel.get().add(INSTANCE);
                INSTANCE.openDialog();
            }
        }
    }

    /**
     * On click function for cancel button.<p>
     */
    protected void cancelEdit() {

        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                w.removeFromParent();
            }
        }
        for (CmsContainerPageElement element : m_backUpElements) {
            m_groupContainer.add(element);
        }
        if (m_backUpElements.size() == 0) {
            m_groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
        }
        closeDialog();
    }

    /**
     * Creates a place-holder for the group-container.<p>
     * 
     * @param element the element
     * 
     * @return the place-holder widget
     */
    protected Element createPlaceholder(Element element) {

        Element result = CmsDomUtil.clone(element);
        result.addClassName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerPlaceholder());
        result.getStyle().setBackgroundColor("transparent");
        return result;
    }

    /**
     * On click function for save button.<p>
     */
    protected void saveEdit() {

        m_groupContainerBean.setTitle(m_inputTitle.getFormValueAsString());
        m_groupContainerBean.setDescription(m_inputDescription.getFormValueAsString());
        m_groupContainerBean.setElements(getElements());
        m_controller.saveGroupcontainer(m_groupContainerBean, m_groupContainer);
        closeDialog();
    }

    /**
     * Sets the data of the group-container to edit.<p>
     * 
     * @param elementsData the data of all contained elements and the group-container itself
     */
    protected void setGroupContainerData(Map<String, CmsContainerElementData> elementsData) {

        m_elementData = elementsData.get(m_groupContainer.getId());
        if (m_elementData != null) {
            if (m_editorDialog != null) {
                m_editorDialog.getOkButton().enable();
            }
            m_groupContainerBean = new CmsGroupContainer();
            m_groupContainerBean.setClientId(m_elementData.getClientId());
            m_groupContainerBean.setResourceType(m_groupContainer.getNewType());
            m_groupContainerBean.setNew(m_groupContainer.isNew());
            m_groupContainerBean.setSitePath(m_elementData.getSitePath());
            if (m_elementData.getTypes().isEmpty()) {
                Set<String> types = new HashSet<String>();
                types.add(((CmsContainerPageContainer)m_groupContainer.getParentTarget()).getContainerType());
                m_elementData.setTypes(types);
                m_groupContainerBean.setTypes(types);
            } else {
                m_groupContainerBean.setTypes(m_elementData.getTypes());
            }
            m_inputDescription.setFormValueAsString(m_elementData.getDescription());
            m_inputTitle.setFormValueAsString(m_elementData.getTitle());
            m_groupContainerBean.setTitle(m_elementData.getTitle());
            m_groupContainerBean.setDescription(m_elementData.getDescription());
        } else {
            CmsDebugLog.getInstance().printLine("Loading groupcontainer error.");
        }
    }

    /**
     * Closes the dialog.<p>
     */
    private void closeDialog() {

        m_controller.stopEditingGroupcontainer();
        m_groupContainer.clearEditingPlaceholder();
        m_editingPlaceholder.removeFromParent();
        Style style = m_groupContainer.getElement().getStyle();
        style.clearPosition();
        style.clearTop();
        style.clearLeft();
        style.clearZIndex();
        style.clearWidth();
        m_parentContainer.insert(m_groupContainer, m_indexPosition);
        RootPanel.get().removeStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().groupcontainerEditing());
        m_groupContainer.getElementOptionBar().setVisible(true);
        if (!m_groupContainer.iterator().hasNext()) {
            // group-container is empty, mark it
            m_groupContainer.addStyleName(I_CmsLayoutBundle.INSTANCE.containerpageCss().emptyGroupContainer());
        }
        INSTANCE = null;
        this.removeFromParent();
    }

    /**
     * Returns the ids of the contained elements and group-container itself.<p>
     * 
     * @return the element ids
     */
    private Set<String> getElementIds() {

        Set<String> subItems = new HashSet<String>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                subItems.add(((CmsContainerPageElement)w).getId());
            }
        }
        subItems.add(m_groupContainer.getId());
        return subItems;
    }

    /**
     * Returns the element data of the contained elements.<p>
     * 
     * @return the contained elements data
     */
    private List<CmsContainerElement> getElements() {

        List<CmsContainerElement> subItems = new ArrayList<CmsContainerElement>();
        Iterator<Widget> it = m_groupContainer.iterator();
        while (it.hasNext()) {
            Widget w = it.next();
            if (w instanceof CmsContainerPageElement) {
                CmsContainerPageElement elementWidget = (CmsContainerPageElement)w;
                CmsContainerElement element = new CmsContainerElement();
                element.setClientId(elementWidget.getId());
                element.setResourceType(elementWidget.getNewType());
                element.setNew(elementWidget.isNew());
                element.setSitePath(elementWidget.getSitePath());
                subItems.add(element);
            }
        }
        return subItems;
    }

    /**
     * Opens the group container edit dialog.<p>
     */
    private void openDialog() {

        m_editorDialog = new CmsConfirmDialog(Messages.get().key(Messages.GUI_GROUPCONTAINER_CAPTION_0));
        int contentHeight = m_dialogContent.getOffsetHeight();
        m_editorDialog.setMainContent(m_dialogContent);
        m_editorDialog.setHandler(new I_CmsConfirmDialogHandler() {

            public void onClose() {

                cancelEdit();
            }

            public void onOk() {

                saveEdit();
            }
        });
        if (m_elementData == null) {
            m_editorDialog.getOkButton().disable(Messages.get().key(Messages.GUI_GROUPCONTAINER_LOADING_DATA_0));
        }
        m_editorDialog.setGlassEnabled(false);
        m_editorDialog.setModal(false);
        m_editorDialog.setWidth(500);
        m_editorDialog.addDialogClose(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                cancelEdit();
            }
        });
        if (m_groupContainerPosition != null) {
            if (m_groupContainerPosition.getLeft() > 600) {
                // place left of the group container if there is enough space
                m_editorDialog.setPopupPosition(
                    m_groupContainerPosition.getLeft() - 530,
                    m_groupContainerPosition.getTop() - 1);
            } else if (m_groupContainerPosition.getTop() > contentHeight + 103 + 40) {
                // else place above if there is enough space
                m_editorDialog.setPopupPosition(m_groupContainerPosition.getLeft(), m_groupContainerPosition.getTop()
                    - (contentHeight + 103));
            } else {
                // else on the right
                m_editorDialog.setPopupPosition(m_groupContainerPosition.getLeft()
                    + m_groupContainerPosition.getWidth()
                    + 20, m_groupContainerPosition.getTop() - 1);
            }
            m_editorDialog.show();
        } else {
            // should never happen
            m_editorDialog.center();
        }
    }
}

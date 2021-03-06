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

package org.opencms.gwt.shared.rpc;

import org.opencms.gwt.shared.CmsAvailabilityInfoBean;
import org.opencms.gwt.shared.CmsDeleteResourceBean;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsLockReportInfo;
import org.opencms.gwt.shared.CmsVfsEntryBean;
import org.opencms.gwt.shared.property.CmsPropertiesBean;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.util.CmsUUID;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An asynchronous service interface for retrieving information about the VFS tree.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsVfsServiceAsync {

    /**
     * Deletes a resource from the VFS.<p>
     * 
     * @param sitePath the site path of the resource to delete
     * @param callback the callback
     */
    void deleteResource(String sitePath, AsyncCallback<Void> callback);

    /**
     * Forces a resource to be unlocked. In case the given resource is a folder, all sub-resources are also unlocked.<p>
     * 
     * @param structureId the structure id of the resource to unlock
     * @param callback the callback
     */
    void forceUnlock(CmsUUID structureId, AsyncCallback<Void> callback);

    /**
     * Gets a {@link CmsAvailabilityInfoBean} for a given resource.<p>
     * 
     * @param structureId the structure id to create the {@link CmsAvailabilityInfoBean} for
     * @param callback the asynchronous callback
     */
    void getAvailabilityInfo(CmsUUID structureId, AsyncCallback<CmsAvailabilityInfoBean> callback);

    /**
     * Gets a {@link CmsAvailabilityInfoBean} for a given resource.<p>
     * 
     * @param vfsPath the vfs path to create the {@link CmsAvailabilityInfoBean} for
     * @param callback the asynchronous callback
     */
    void getAvailabilityInfo(String vfsPath, AsyncCallback<CmsAvailabilityInfoBean> callback);

    /**
     * Returns a list of potentially broken links, if the given resource was deleted.<p>
     * 
     * @param sitePath the resource site-path
     * @param callback the callback
     */
    void getBrokenLinks(String sitePath, AsyncCallback<CmsDeleteResourceBean> callback);

    /**
     * Fetches the list of children of a path.<p>
     * 
     * @param path the path for which the list of children should be retrieved
     * @param callback the asynchronous callback 
     */
    void getChildren(String path, AsyncCallback<List<CmsVfsEntryBean>> callback);

    /**
     * Returns the lock report info.<p>
     * 
     * @param structureId the structure id of the resource to get the report for
     * @param callback the callback
     */
    void getLockReportInfo(CmsUUID structureId, AsyncCallback<CmsLockReportInfo> callback);

    /**
     * Gets a {@link CmsListInfoBean} for a given resource.<p>
     * 
     * @param structureId the structure id to create the {@link CmsListInfoBean} for
     * @param callback the asynchronous callback
     */
    void getPageInfo(CmsUUID structureId, AsyncCallback<CmsListInfoBean> callback);

    /**
     * Gets a {@link CmsListInfoBean} for a given resource.<p>
     * 
     * @param vfsPath the vfs path to create the {@link CmsListInfoBean} for
     * @param callback the asynchronous callback
     */
    void getPageInfo(String vfsPath, AsyncCallback<CmsListInfoBean> callback);

    /**
     * Returns the root entries of the VFS.<p>
     * 
     * @param callback the asynchronous callback
     */
    void getRootEntries(AsyncCallback<List<CmsVfsEntryBean>> callback);

    /**
     * Returns the site-path for the resource with the given id.<p>
     * 
     * @param structureId the structure id
     * @param callback the asynchronous callback
     */
    void getSitePath(CmsUUID structureId, AsyncCallback<String> callback);

    /**
     * Load the data necessary to edit the properties of a resource.<p>
     * 
     * @param id the structure id of a resource
     * @param callback the asynchronous callback  
     */
    void loadPropertyData(CmsUUID id, AsyncCallback<CmsPropertiesBean> callback);

    /**
     * Saves a set of property changes.<p>
     * 
     * @param changes the property changes
     *  
     * @param callback the asynchronous callback 
     */
    void saveProperties(CmsPropertyChangeSet changes, AsyncCallback<Void> callback);

}

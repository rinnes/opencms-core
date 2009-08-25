/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEElementManager.java,v $
 * Date   : $Date: 2009/08/24 13:34:59 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsContainerPageLoader;
import org.opencms.loader.CmsTemplateLoaderFacade;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.list.NodeCachingLinkedList;
import org.apache.commons.logging.Log;

/**
 * Maintains recent and favorite element lists.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 7.6
 */
public final class CmsADEElementManager {

    /** HTML id prefix constant. */
    protected static final String ADE_ID_PREFIX = "ade_";

    /** User additional info key constant. */
    private static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** User additional info key constant. */
    private static final String ADDINFO_ADE_RECENTLIST_SIZE = "ADE_RECENTLIST_SIZE";

    /** default recent list size constant. */
    private static final int DEFAULT_RECENT_LIST_SIZE = 10;

    /** Container page loader reference. */
    private static final CmsContainerPageLoader LOADER = (CmsContainerPageLoader)OpenCms.getResourceManager().getLoader(
        CmsContainerPageLoader.RESOURCE_LOADER_ID);

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEElementManager.class);

    /** Singleton instance. */
    private static CmsADEElementManager m_instance;

    /** Recent list cache. */
    private Map<String, List<CmsUUID>> m_recentListCache;

    /**
     * Creates a new instance.<p>
     */
    private CmsADEElementManager() {

        m_recentListCache = new HashMap<String, List<CmsUUID>>();
    }

    /**
     * Returns the singleton instance.<p>
     * 
     * @return the singleton instance
     */
    public static CmsADEElementManager getInstance() {

        if (m_instance == null) {
            m_instance = new CmsADEElementManager();
        }
        return m_instance;
    }

    /**
     * Returns all types of containers from the given container page.<p>
     * 
     * @param cms the current cms context
     * @param containerPageUri the container page uri
     * 
     * @return a collection of types as strings
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if there is a problem with the json manipulation
     */
    public Collection getContainerPageTypes(CmsObject cms, String containerPageUri) throws CmsException, JSONException {

        Set types = new HashSet();
        // get the container page itself
        CmsResource containerPage = cms.readResource(containerPageUri);
        JSONObject containers = LOADER.getCache(cms, containerPage, cms.getRequestContext().getLocale());
        Iterator itKeys = containers.keys();
        while (itKeys.hasNext()) {
            String containerName = (String)itKeys.next();
            JSONObject container = containers.getJSONObject(containerName);
            // get the type
            String type = container.getString(CmsContainerPageLoader.N_TYPE);
            types.add(type);
        }
        return types;
    }

    /**
     * Returns the content of an element when rendered with the given formatter.<p> 
     * 
     * @param cms the cms context
     * @param resource the element resource
     * @param formatterUri the formatter uri
     * @param req the http request
     * @param res the http response
     * 
     * @return generated html code
     * 
     * @throws CmsException if an cms related error occurs
     * @throws ServletException if a jsp related error occurs
     * @throws IOException if a jsp related error occurs
     */
    public String getElementContent(
        CmsObject cms,
        CmsResource resource,
        String formatterUri,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException, ServletException, IOException {

        CmsResource resFormatter = cms.readResource(formatterUri);

        CmsTemplateLoaderFacade loaderFacade = new CmsTemplateLoaderFacade(OpenCms.getResourceManager().getLoader(
            resFormatter), resource, resFormatter);

        String oldUri = cms.getRequestContext().getUri();
        try {
            cms.getRequestContext().setUri(cms.getSitePath(resource));
            CmsResource loaderRes = loaderFacade.getLoaderStartResource();
            // TODO: is this going to be cached?
            return new String(loaderFacade.getLoader().dump(cms, loaderRes, null, null, req, res));
        } finally {
            cms.getRequestContext().setUri(oldUri);
        }
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param cms the cms context
     * @param resource the resource
     * @param types the types supported by the container page
     * @param req the http request
     * @param res the http response
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(
        CmsObject cms,
        CmsResource resource,
        Collection types,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException, JSONException {

        // create new json object for the element
        JSONObject resElement = new JSONObject();
        resElement.put(CmsADEServer.P_ID, CmsADEElementManager.ADE_ID_PREFIX + resource.getStructureId().toString());
        resElement.put(CmsADEServer.P_FILE, cms.getSitePath(resource));
        resElement.put(CmsADEServer.P_DATE, resource.getDateLastModified());
        resElement.put(CmsADEServer.P_USER, cms.readUser(resource.getUserLastModified()).getName());
        resElement.put(CmsADEServer.P_NAVTEXT, cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_NAVTEXT,
            false).getValue(""));
        resElement.put(CmsADEServer.P_TITLE, cms.readPropertyObject(
            resource,
            CmsPropertyDefinition.PROPERTY_TITLE,
            false).getValue(""));
        // TODO: fix this info
        resElement.put(CmsADEServer.P_TYPE, "news");
        resElement.put(CmsADEServer.P_SUBITEMS, (String)null);
        resElement.put(CmsADEServer.P_ALLOWMOVE, true);
        resElement.put(CmsADEServer.P_ALLOWEDIT, true);
        resElement.put(CmsADEServer.P_LOCKED, false);
        resElement.put(CmsADEServer.P_STATUS, "u");
        // add formatted elements
        JSONObject resContents = new JSONObject();
        resElement.put(CmsADEServer.P_CONTENTS, resContents);
        // add formatter uris
        JSONObject formatters = new JSONObject();
        resElement.put(CmsADEServer.P_FORMATTERS, formatters);
        // TODO: this may not be performing well
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
        Iterator it = content.getContentDefinition().getContentHandler().getFormatters().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String type = (String)entry.getKey();
            if (!types.contains(type) && !type.equals(CmsDefaultXmlContentHandler.DEFAULT_FORMATTER_TYPE)) {
                // skip not supported types
                continue;
            }
            String formatterUri = (String)entry.getValue();
            formatters.put(type, formatterUri);
            // execute the formatter jsp for the given element
            try {
                String jspResult = getElementContent(cms, resource, formatterUri, req, res);
                // set the results
                resContents.put(type, jspResult);
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(
                    Messages.ERR_GENERATE_FORMATTED_ELEMENT_3,
                    cms.getSitePath(resource),
                    formatterUri,
                    type), e);
            }
        }

        return resElement;
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param cms the cms context
     * @param structureId the element structure id
     * @param containerPageUri the container page uri
     * @param req the http request
     * @param res the http response
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(
        CmsObject cms,
        CmsUUID structureId,
        String containerPageUri,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException, JSONException {

        return getElementData(
            cms,
            cms.readResource(structureId),
            getContainerPageTypes(cms, containerPageUri),
            req,
            res);
    }

    /**
     * Returns the data for an element.<p>
     * 
     * @param cms the cms context
     * @param elementUri the element uri
     * @param containerPageUri the container page uri
     * @param req the http request
     * @param res the http response
     * 
     * @return the data for an element
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONObject getElementData(
        CmsObject cms,
        String elementUri,
        String containerPageUri,
        HttpServletRequest req,
        HttpServletResponse res) throws CmsException, JSONException {

        return getElementData(cms, cms.readResource(elementUri), getContainerPageTypes(cms, containerPageUri), req, res);
    }

    /**
     * Returns the current user's favorites list.<p>
     * 
     * @param cms the cms context 
     * @param resElements the current page's element list
     * @param containerPageUri the container page uri
     * @param req the http request
     * @param res the http response
     * 
     * @return the current user's favorites list
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONArray getFavoriteList(
        CmsObject cms,
        JSONObject resElements,
        String containerPageUri,
        HttpServletRequest req,
        HttpServletResponse res) throws JSONException, CmsException {

        JSONArray result = getFavoriteListFromStore(cms);

        // iterate the list and create the missing elements
        for (int i = 0; i < result.length(); i++) {
            String id = result.optString(i);
            if ((resElements != null) && !resElements.has(id)) {
                resElements.put(id, getElementData(cms, parseId(id), containerPageUri, req, res));
            }
        }

        return result;
    }

    /**
     * Returns the current user's recent list.<p>
     * 
     * @param cms the cms context 
     * @param resElements the current page's element list
     * @param containerPageUri the container page uri
     * @param req the http request
     * @param res the http response
     * 
     * @return the current user's recent list
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONArray getRecentList(
        CmsObject cms,
        JSONObject resElements,
        String containerPageUri,
        HttpServletRequest req,
        HttpServletResponse res) throws JSONException, CmsException {

        JSONArray result = new JSONArray();

        // get the cached list
        List<CmsUUID> recentList = getRecentListFromCache(cms);
        // iterate the list and create the missing elements
        for (CmsUUID structureId : recentList) {
            String id = CmsADEElementManager.ADE_ID_PREFIX + structureId.toString();
            result.put(id);
            if ((resElements != null) && !resElements.has(id)) {
                resElements.put(id, getElementData(cms, structureId, containerPageUri, req, res));
            }
        }

        return result;
    }

    /**
     * Sets the favorite list.<p>
     * 
     * @param cms the cms context
     * @param list the element id list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void setFavoriteList(CmsObject cms, JSONArray list) throws CmsException {

        CmsUser user = cms.getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, list.toString());
        cms.writeUser(user);
    }

    /**
     * Sets the recent list.<p>
     * 
     * @param cms the cms context
     * @param list the element id list
     */
    public void setRecentList(CmsObject cms, JSONArray list) {

        List<CmsUUID> recentList = getRecentListFromCache(cms);
        recentList.clear();
        for (int i = 0; i < list.length(); i++) {
            try {
                recentList.add(parseId(list.optString(i)));
            } catch (CmsIllegalArgumentException t) {
                LOG.warn(Messages.get().container(Messages.ERR_INVALID_ID_1, list.optString(i)), t);
            }
        }
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the cached recent list
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONArray getFavoriteListFromStore(CmsObject cms) throws JSONException {

        CmsUser user = cms.getRequestContext().currentUser();
        String favListStr = (String)user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);
        JSONArray favoriteList = new JSONArray();
        if (favListStr != null) {
            favoriteList = new JSONArray(favListStr);
        }
        return favoriteList;
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the cached recent list
     */
    protected List<CmsUUID> getRecentListFromCache(CmsObject cms) {

        CmsUser user = cms.getRequestContext().currentUser();
        List<CmsUUID> recentList = m_recentListCache.get(user.getId().toString());
        if (recentList == null) {
            Integer maxElems = (Integer)user.getAdditionalInfo(ADDINFO_ADE_RECENTLIST_SIZE);
            if (maxElems == null) {
                maxElems = new Integer(DEFAULT_RECENT_LIST_SIZE);
            }
            recentList = new NodeCachingLinkedList(maxElems.intValue());
            m_recentListCache.put(user.getId().toString(), recentList);
        }
        return recentList;
    }

    /**
     * Parses an element id.<p>
     * 
     * @param id the element id
     * 
     * @return the corresponding structure id
     * 
     * @throws CmsIllegalArgumentException if the id has not the right format
     */
    protected CmsUUID parseId(String id) throws CmsIllegalArgumentException {

        if ((id == null) || (!id.startsWith(ADE_ID_PREFIX))) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
        try {
            return new CmsUUID(id.substring(ADE_ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
    }
}
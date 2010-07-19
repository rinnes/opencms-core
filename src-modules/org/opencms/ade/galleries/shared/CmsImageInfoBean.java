/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsImageInfoBean.java,v $
 * Date   : $Date: 2010/07/19 07:45:28 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.shared;

/**
 * Bean holding image info to be display in the resource preview dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsImageInfoBean extends CmsResourceInfoBean {

    /** The image copyright information. */
    private String m_copyright;

    /** The image height. */
    private int m_height;

    /** The selected image path (used for editor and widget mode). */
    private String m_selectedPath;

    /** The image width. */
    private int m_width;

    /**
     * Returns the image copyright information.<p>
     *
     * @return the image copyright information
     */
    public String getCopyright() {

        return m_copyright;
    }

    /**
     * Returns the height.<p>
     *
     * @return the height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the selected path.<p>
     *
     * @return the selected path
     */
    public String getSelectedPath() {

        return m_selectedPath;
    }

    /**
     * Returns the width.<p>
     *
     * @return the width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Sets the image copyright information.<p>
     *
     * @param copyright the image copyright information to set
     */
    public void setCopyright(String copyright) {

        m_copyright = copyright;
    }

    /**
     * Sets the height.<p>
     *
     * @param height the height to set
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the selected path.<p>
     *
     * @param selectedPath the selected path to set
     */
    public void setSelectedPath(String selectedPath) {

        m_selectedPath = selectedPath;
    }

    /**
     * Sets the width.<p>
     *
     * @param width the width to set
     */
    public void setWidth(int width) {

        m_width = width;
    }

}

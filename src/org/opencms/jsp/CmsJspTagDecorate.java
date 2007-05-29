/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagDecorate.java,v $
 * Date   : $Date: 2007/05/29 10:53:53 $
 * Version: $Revision: 1.2.4.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.flex.CmsFlexController;
import org.opencms.jsp.decorator.CmsDecoratorConfiguration;
import org.opencms.jsp.decorator.CmsHtmlDecorator;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implements the <code>&lt;cms:decorate&gt;&lt;/cms:decorate&gt;</code> 
 * tag to decorate HTML content with configurated decoration maps.<p>
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.2.4.2 $ 
 * 
 * @since 6.1.3 
 */
public class CmsJspTagDecorate extends BodyTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagDecorate.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 3072561342127379294L;

    /** The configuration. */
    private String m_file;

    /** The decoration locale. */
    private String m_locale;

    /** List of upper case tag name strings of tags that should not be auto-corrected if closing divs are missing. */
    private List m_noAutoCloseTags;

    /**
     * Internal action method.<p>
     * 
     * DEcorates a HTMl content block.<p>
     * 
     * @param content the content to be decorated
     * @param configFile the config file
     * @param locale the locale to use for decoration or NOLOCALE if not locale should be used
     * @param req the current request
     * @return the decorated content
     * 
     * @see org.opencms.staticexport.CmsLinkManager#substituteLink(org.opencms.file.CmsObject, String)
     */
    public String decorateTagAction(String content, String configFile, String locale, ServletRequest req) {

        try {
            Locale loc = null;
            CmsFlexController controller = CmsFlexController.getController(req);
            if (CmsStringUtil.isEmpty(locale)) {
                loc = controller.getCmsObject().getRequestContext().getLocale();
            } else {
                loc = new Locale(locale);
            }

            String encoding = controller.getCmsObject().getRequestContext().getEncoding();
            CmsDecoratorConfiguration config = new CmsDecoratorConfiguration(controller.getCmsObject(), configFile, loc);
            CmsHtmlDecorator decorator = new CmsHtmlDecorator(config);
            decorator.setNoAutoCloseTags(m_noAutoCloseTags);
            return decorator.doDecoration(content, encoding);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "decoration"), e);
            }
            return content;
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     * @return EVAL_PAGE
     * @throws JspException in case soemthing goes wrong
     */
    public int doEndTag() throws JspException {

        ServletRequest req = pageContext.getRequest();

        // This will always be true if the page is called through OpenCms 
        if (CmsFlexController.isCmsRequest(req)) {
            try {
                String content = decorateTagAction(getBodyContent().getString(), getFile(), getLocale(), req);
                getBodyContent().clear();
                getBodyContent().print(content);
                getBodyContent().writeOut(pageContext.getOut());

            } catch (Exception ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_PROCESS_TAG_1, "decoration"), ex);
                }
                throw new JspException(ex);
            }
        }
        return EVAL_PAGE;
    }

    /**
     * Returns the file name.<p>
     * 
     * @return the file name
     */
    public String getFile() {

        return m_file;
    }

    /**
     * Returns the locale name.<p>
     * 
     * @return the locale name
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Getter for the attribute "noAutoCloseTags" of the &lt;cms:parse&gt; tag.<p>
     *  
     * Returns a <code>String</code> that consists of the comma-separated upper case tag names for which this 
     * tag will not correct missing closing tags. <p>
     * 
     * 
     * @return a String that consists of the comma-separated upper case tag names for which this 
     *      tag will not correct missing closing tags. 
     */
    public String getNoAutoCloseTags() {

        StringBuffer result = new StringBuffer();
        if ((m_noAutoCloseTags != null) && (m_noAutoCloseTags.size() > 0)) {
            Iterator it = m_noAutoCloseTags.iterator();
            while (it.hasNext()) {
                result.append(it.next()).append(',');
            }
        }
        return result.toString();
    }

    /**
     * Sets the file name.<p>
     * 
     * @param file the file name
     */
    public void setFile(String file) {

        if (file != null) {
            m_file = file.toLowerCase();
        }
    }

    /**
     * Sets the locale name.<p>
     * 
     * @param locale the locale name
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Setter for the attribute "noAutoCloseTags" of the &lt;cms:parse&gt; tag.<p>
     *  
     * Awaits a <code>String</code> that consists of the comma-separated upper case tag names for which this 
     * tag should not correct missing closing tags.<p>
     * 
     * @param noAutoCloseTagList a <code>String</code> that consists of the comma-separated upper case tag names for which this 
     *      tag should not correct missing closing tags.
     */
    public void setNoAutoCloseTags(String noAutoCloseTagList) {

        m_noAutoCloseTags = CmsStringUtil.splitAsList(noAutoCloseTagList, ',');

    }
}

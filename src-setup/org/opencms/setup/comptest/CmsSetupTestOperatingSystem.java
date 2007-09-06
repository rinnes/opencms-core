/*
 * File   : $Source: /alkacon/cvs/opencms/src-setup/org/opencms/setup/comptest/CmsSetupTestOperatingSystem.java,v $
 * Date   : $Date: 2007/08/22 11:11:46 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup.comptest;

import org.opencms.setup.CmsSetupBean;

/**
 * Tests the operating system.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 6.1.8 
 */
public class CmsSetupTestOperatingSystem implements I_CmsSetupTest {

    /** The test name. */
    public static final String TEST_NAME = "Operating System";

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#getName()
     */
    public String getName() {

        return TEST_NAME;
    }

    /**
     * @see org.opencms.setup.comptest.I_CmsSetupTest#execute(org.opencms.setup.CmsSetupBean)
     */
    public CmsSetupTestResult execute(CmsSetupBean setupBean) {

        CmsSetupTestResult testResult = new CmsSetupTestResult(this);

        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        testResult.setResult(osName + " " + osVersion);
        testResult.setHelp("Your operating system version");

        // there is still no handling to test the operating system
        testResult.setGreen();

        return testResult;
    }
}
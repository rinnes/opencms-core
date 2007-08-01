/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupDBWrapper.java,v $
 * Date   : $Date: 2007/07/26 09:03:25 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Wrapper to encapsulate, connection, statment and result set for the setup
 * and update wizard.<p>
 */
public class CmsSetupDBWrapper {

    /** the statement to use in the db wrapper. */
    private Statement m_statement;

    /** the connection to use in the db wrapper. */
    private Connection m_connection;

    /** the result set returned by the db wrapper. */
    private ResultSet m_resultset;

    /**
     * Constructor, creates a new CmsSetupDBWrapper.<p>
     * @param con the connection to use in this db wrapper. 
     */
    public CmsSetupDBWrapper(Connection con) {

        m_connection = con;
    }

    /** 
     * Closes result set, and statement. <p>
     */
    public void close() {

        // result set
        if (m_resultset != null) {
            try {
                m_resultset.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        // statement
        if (m_statement != null) {
            try {
                m_statement.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

    }

    /** 
     * Creates a new SQL Statement on the connetcion of this db wrapper.<p>
     * @throws SQLException if statement cannot be created
     */
    public void createStatement() throws SQLException {

        m_statement = m_connection.createStatement();
    }

    /** 
     * Excecutes a query on the connetcion and statement of this db wrapper.<p>
     * @param query the query to excecute
     * @throws SQLException if statement cannot be created
     */
    public void excecuteQuery(String query) throws SQLException {

        m_resultset = m_statement.executeQuery(query);
    }


    /**
     * Returns the res.<p>
     *
     * @return the res
     */
    public ResultSet getResultSet() {

        return m_resultset;
    }

}
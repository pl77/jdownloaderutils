/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.orm
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.orm;

import org.appwork.utils.storage.DBException;

/**
 * @author coalado
 * 
 */
public class ConflictTableException extends DBException {

    /**
     * 
     */
    private static final long serialVersionUID = 7283240644372384726L;

    /**
     * @param e
     */
    public ConflictTableException(Exception e) {
        super(e);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param string
     */
    public ConflictTableException(String string) {
        super(string);
    }

}

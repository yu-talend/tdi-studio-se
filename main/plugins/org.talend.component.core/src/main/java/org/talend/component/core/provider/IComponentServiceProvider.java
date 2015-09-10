// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.component.core.provider;

import org.talend.components.api.service.ComponentService;

/**
 * created by ycbai on 2015年9月9日 Detailled comment
 *
 */
public interface IComponentServiceProvider {

    public ComponentService getComponentService();

}

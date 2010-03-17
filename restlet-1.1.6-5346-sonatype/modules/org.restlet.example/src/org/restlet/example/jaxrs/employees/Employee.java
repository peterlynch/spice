/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.jaxrs.employees;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Contains all data about an employee.
 * 
 * @author Stephan Koops
 */
@XmlRootElement
public class Employee extends AbstractEmployee {

    private String sex;

    private String department;

    private URI departmentUri;

    public String getDepartment() {
        return this.department;
    }

    public URI getDepartmentUri() {
        return this.departmentUri;
    }

    public String getSex() {
        return this.sex;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDepartmentUri(URI departmentUri) {
        this.departmentUri = departmentUri;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
/**
 * Generated by Gas3 v1.1.0 (Granite Data Services) on Sat Jul 26 17:58:20 CEST 2008.
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERRIDDEN EACH TIME YOU USE
 * THE GENERATOR. CHANGE INSTEAD THE INHERITED CLASS (Person.as).
 */

package org.granite.test.tide {

    import flash.utils.flash_proxy;

    import org.granite.tide.Component;
    import org.granite.tide.ITideResponder;

    use namespace flash_proxy;


    [RemoteClass(alias="org.granite.test.tide.PersonServiceFold")]
    public class PersonServiceFold extends Component {
        
        public function PersonServiceFold():void {
            super();
        }

        [Lazy]
        public function modifyPerson(arg0:Person, resultHandler:Object = null, faultHandler:Function = null):void {
            if (faultHandler != null)
                callProperty("modifyPerson", arg0, resultHandler, faultHandler);
            else if (resultHandler is Function || resultHandler is ITideResponder)
                callProperty("modifyPerson", arg0, resultHandler);
            else if (resultHandler == null)
                callProperty("modifyPerson", arg0);
            else
                throw new Error("Illegal argument to remote call (last argument should be Function or ITideResponder): " + resultHandler);
        }
    }
}

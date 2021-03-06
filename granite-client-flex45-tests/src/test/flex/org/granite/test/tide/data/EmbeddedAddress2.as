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
 * Generated by Gas3 v2.1.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (EmbAddress.as).
 */

package org.granite.test.tide.data {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;

    [Bindable]
	[RemoteClass(alias="org.granite.tide.test.EmbeddedAddress2")]
    public class EmbeddedAddress2 implements IExternalizable {

        private var _address1:String;
        private var _address2:String;
		private var _location:EmbeddedLocation;

        public function set address1(value:String):void {
            _address1 = value;
        }
        public function get address1():String {
            return _address1;
        }

        public function set address2(value:String):void {
            _address2 = value;
        }
        public function get address2():String {
            return _address2;
        }

        public function set location(value:EmbeddedLocation):void {
            _location = value;
        }
        public function get location():EmbeddedLocation {
            return _location;
        }

        public function readExternal(input:IDataInput):void {
            _address1 = input.readObject() as String;
            _address2 = input.readObject() as String;
            _location = input.readObject() as EmbeddedLocation;
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(_address1);
            output.writeObject(_address2);
            output.writeObject(_location);
        }
    }
}
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
package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.fluint.uiImpersonation.UIImpersonator;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestComponentObservers2
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testComponentObservers2():void {
        	var myComponentObserverNoCreate:MyComponentObserverNoCreate = new MyComponentObserverNoCreate();
        	_ctx.myComponentObserverNoCreate = myComponentObserverNoCreate;
        	
        	_ctx.raiseEvent("someEvent");
        	Assert.assertTrue("Observer no create", myComponentObserverNoCreate.triggered);        	
        	
        	myComponentObserverNoCreate.triggered = false;
        	_ctx.myComponentObserverNoCreate = null;
        	_ctx.raiseEvent("someEvent");
        	
        	Assert.assertFalse("Observer no create", myComponentObserverNoCreate.triggered);
        	
        	var panel2:MyPanel2 = new MyPanel2();
        	UIImpersonator.addChild(panel2);
        	
        	_ctx.raiseEvent("someEvent2");
        	Assert.assertTrue("Observer UI triggered", panel2.triggered);
        	
        	panel2.triggered = false;
			UIImpersonator.removeChild(panel2);
        	
        	_ctx.raiseEvent("someEvent2");
        	Assert.assertFalse("Observer UI non triggered", panel2.triggered);
        }
    }
}

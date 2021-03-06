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
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    
    
    public class TestComponentNestedConversations
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([MyComponentConversation, MyComponentConversation2]);
        }
        
        
        [Test]
        public function testComponentNestedConversations():void {
        	var ctx1:BaseContext = Tide.getInstance().getContext("1", null, true);
        	var ctx2:BaseContext = Tide.getInstance().getContext("2", null, true);
        	var ctx11:BaseContext = Tide.getInstance().getContext("1.1", "1", true);
        	var ctx12:BaseContext = Tide.getInstance().getContext("1.2", "1", true);
        	var ctx21:BaseContext = Tide.getInstance().getContext("2.1", "2", true);
        	var ctx22:BaseContext = Tide.getInstance().getContext("2.2", "2", true);
        	
        	_ctx.application.dispatchEvent(new TideUIConversationEvent("1", "start"));
        	
        	Assert.assertNotNull("Context 1 component", ctx1.myComponent);
        	Assert.assertStrictlyEquals("Context 1.1 component", ctx1.myComponent, ctx11.myComponent);
        	
        	Assert.assertNotNull("Context 2.1 component", ctx21.myComponent);
        	Assert.assertFalse("Context 2 component", ctx2.myComponent === ctx21.myComponent);
        	
        	ctx1.myComponent.dispatchEvent(new TideUIConversationEvent("1.1", "next"));
        	
        	Assert.assertTrue("Context 1.1 component 2 triggered", ctx11.myComponent2.triggered);
        	Assert.assertFalse("Context 1.2 component 2 not created", ctx12.meta_getInstance("myComponent2", false, true));
        	
        	ctx2.myComponent.dispatchEvent(new TideUIConversationEvent("2.3", "next"));
        	
        	var ctx23:BaseContext = Tide.getInstance().getContext("2.3", null, false);
        	Assert.assertNotNull("Context 2.3 created", ctx23);
        	Assert.assertStrictlyEquals("Context 2.3 parent", ctx2, ctx23.meta_parentContext);
        	
        	Assert.assertTrue("Context 2.3 component 2 triggered", ctx23.myComponent2.triggered);
        	Assert.assertFalse("Context 2.1 component 2 not triggered", ctx21.myComponent2.triggered);
			
			ctx1.meta_end(false);
			
			Assert.assertNull("Context 1.1 destroyed", _ctx.meta_tide.getContext("1.1", "1", false));
			Assert.assertNull("Context 1.2 destroyed", _ctx.meta_tide.getContext("1.2", "1", false));
			Assert.assertNotNull("Context 2.1 not destroyed", _ctx.meta_tide.getContext("2.1", "2", false));
			
			ctx2.meta_end(false);
			
			Assert.assertNull("Context 2.1 destroyed", _ctx.meta_tide.getContext("2.1", "2", false));
			Assert.assertNull("Context 2.2 destroyed", _ctx.meta_tide.getContext("2.2", "2", false));
			Assert.assertNull("Context 2.3 destroyed", _ctx.meta_tide.getContext("2.3", "2", false));
        }
    }
}

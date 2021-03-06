/**
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
/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.
 
  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.
 
  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.test.tide.data;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Version;

import org.granite.tide.data.DataPublishListener;

@MappedSuperclass
@EntityListeners({DataPublishListener.class})
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue 
    private Long id;
    
    /* "UUID" and "UID" are Oracle reserved keywords -> "ENTITY_UID" */
    @Column(name="ENTITY_UID", unique=true, nullable=false, updatable=false, length=36)
    private String uid;

    @Version
    private Long version;
    
    
    public AbstractEntity() {    	
    }
    
    public AbstractEntity(Long id, Long version, String uid) {
    	this.id = id;
    	this.version = version;
    	this.uid = uid;
    }
    
    public Long getId() {
        return id;
    }
    
    public void initUid() {
    	uid();
    }
    public void initIdUid(Long id, String uid) {
        this.id = id;
        if (uid == null)
            uid = "Person:" + id;
        this.uid = uid;
    }

    public String getUid() {
        return this.uid;
    }
    
    public Long getVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object o) {
        return (o == this || (o instanceof AbstractEntity && uid().equals(((AbstractEntity)o).uid())));
    }
    
    @Override
    public int hashCode() {
        return uid().hashCode();
    }
    
	@PrePersist
    private void onPrePersist() {
        uid();
    }
    
    private String uid() {
        if (uid == null)
            uid = UUID.randomUUID().toString();
        return uid;
    }
}

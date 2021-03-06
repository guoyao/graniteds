/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.javafx.persistence.collection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentSortedMap;
import org.granite.client.persistence.collection.UnsafePersistentCollection;

import com.sun.javafx.collections.ObservableMapWrapper;

/**
 * @author Franck WOLFF
 */
public class ObservablePersistentSortedMap<K, V> extends ObservableMapWrapper<K, V> implements UnsafePersistentCollection<PersistentSortedMap<K, V>> {
	
    private static final long serialVersionUID = 1L;
	
	private final PersistentSortedMap<K, V> persistentSortedMap;

	public ObservablePersistentSortedMap(PersistentSortedMap<K, V> persistentSortedMap) {
		super(persistentSortedMap);
		
		this.persistentSortedMap = persistentSortedMap;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		persistentSortedMap.writeExternal(out);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		persistentSortedMap.readExternal(in);
	}

	@Override
	public boolean wasInitialized() {
		return persistentSortedMap.wasInitialized();
	}

	@Override
	public void uninitialize() {
		persistentSortedMap.uninitialize();
	}

	@Override
	public void initialize() {
		persistentSortedMap.initialize();
	}

	@Override
	public void initializing() {
		persistentSortedMap.initializing();
	}

	@Override
	public PersistentCollection clone(boolean uninitialize) {
		return persistentSortedMap.clone(uninitialize);
	}

	@Override
	public Loader<PersistentCollection> getLoader() {
		return persistentSortedMap.getLoader();
	}

	@Override
	public void setLoader(Loader<PersistentCollection> loader) {
		persistentSortedMap.setLoader(loader);
	}

	@Override
	public boolean isDirty() {
		return persistentSortedMap.isDirty();
	}

	@Override
	public void dirty() {
		persistentSortedMap.dirty();
	}

	@Override
	public void clearDirty() {
		persistentSortedMap.clearDirty();
	}

    @Override
    public void addListener(ChangeListener listener) {
        persistentSortedMap.addListener(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) {
        persistentSortedMap.removeListener(listener);
    }

    @Override
    public void addListener(InitializationListener listener) {
        persistentSortedMap.addListener(listener);
    }

    @Override
    public void removeListener(InitializationListener listener) {
        persistentSortedMap.removeListener(listener);
    }

	@Override
	public void withInitialized(InitializationCallback callback) {
		persistentSortedMap.withInitialized(callback);
	}
	
	@Override
	public PersistentSortedMap<K, V> internalPersistentCollection() {
		return persistentSortedMap;
	}
}
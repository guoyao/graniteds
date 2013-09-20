/**
 * Generated by Gas3 v1.1.0 (Granite Data Services) on Sat Jul 26 17:58:20 CEST 2008.
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERRIDDEN EACH TIME YOU USE
 * THE GENERATOR. CHANGE INSTEAD THE INHERITED CLASS (Person.as).
 */

package org.granite.test.tide.data {

    import flash.utils.ByteArray;
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    
    import mx.collections.ListCollectionView;
    import mx.controls.List;
    
    import org.granite.meta;
    import org.granite.test.tide.AbstractEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;
    import org.granite.util.Enum;

    use namespace meta;

    [Managed]
    [RemoteClass(alias="org.granite.test.tide.Patient7")]
    public class Patient7 extends AbstractEntity {

		private var _providers:ListCollectionView;
        private var _name:String;
        
        
        public function set providers(value:ListCollectionView):void {
			_providers = value;
        }
        public function get providers():ListCollectionView {
            return _providers;
        }

        public function set name(value:String):void {
            _name = value;
        }
        public function get name():String {
            return _name;
        }

        override meta function merge(em:IEntityManager, obj:*):void {
            var src:Patient7 = Patient7(obj);
            super.meta::merge(em, obj);
            if (meta::isInitialized()) {
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
				em.meta_mergeExternal(src._providers, _providers, null, this, 'providers', function setter(o:*):void{_providers = o as ListCollectionView}) as ListCollectionView;
            }
        }

        override public function readExternal(input:IDataInput):void {
            super.readExternal(input);
            if (meta::isInitialized()) {
                _name = input.readObject() as String;
				_providers = input.readObject() as ListCollectionView;
            }
        }

        override public function writeExternal(output:IDataOutput):void {
            super.writeExternal(output);
            if (meta::isInitialized()) {
                output.writeObject((_name is IPropertyHolder) ? IPropertyHolder(_name).object : _name);
				output.writeObject((_providers is IPropertyHolder) ? IPropertyHolder(_providers).object : _providers);
            }
        }
    }
}

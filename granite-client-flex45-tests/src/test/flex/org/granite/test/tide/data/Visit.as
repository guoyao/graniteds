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
    
    import org.granite.meta;
    import org.granite.test.tide.AbstractEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IPropertyHolder;
    import org.granite.util.Enum;

    use namespace meta;

    [Managed]
    [RemoteClass(alias="org.granite.test.tide.Visit")]
    public class Visit extends AbstractEntity {

        private var _name:String;
		private var _patient:Patient3;
		private var _tests:ListCollectionView;
        
        		
		public function set name(value:String):void {
			_name = value;
		}
		public function get name():String {
			return _name;
		}
		
		public function set patient(value:Patient3):void {
			_patient = value;
		}
		public function get patient():Patient3 {
			return _patient;
		}
		
        public function set tests(value:ListCollectionView):void {
			_tests = value;
        }
		[Lazy]
        public function get tests():ListCollectionView {
            return _tests;
        }

        override meta function merge(em:IEntityManager, obj:*):void {
            var src:Visit = Visit(obj);
            super.meta::merge(em, obj);
            if (meta::isInitialized()) {
                em.meta_mergeExternal(src._name, _name, null, this, 'name', function setter(o:*):void{_name = o as String}) as String;
				em.meta_mergeExternal(src._patient, _patient, null, this, 'patient', function setter(o:*):void{_patient = o as Patient3}) as Patient3;
				em.meta_mergeExternal(src._tests, _tests, null, this, 'tests', function setter(o:*):void{_tests = o as ListCollectionView}) as ListCollectionView;
            }
        }

        override public function readExternal(input:IDataInput):void {
            super.readExternal(input);
            if (meta::isInitialized()) {
                _name = input.readObject() as String;
				_patient = input.readObject() as Patient3;
				_tests = input.readObject() as ListCollectionView;
            }
        }

        override public function writeExternal(output:IDataOutput):void {
            super.writeExternal(output);
            if (meta::isInitialized()) {
                output.writeObject((_name is IPropertyHolder) ? IPropertyHolder(_name).object : _name);
				output.writeObject((_patient is IPropertyHolder) ? IPropertyHolder(_patient).object : _patient);
				output.writeObject((_tests is IPropertyHolder) ? IPropertyHolder(_tests).object : _tests);
            }
        }
    }
}

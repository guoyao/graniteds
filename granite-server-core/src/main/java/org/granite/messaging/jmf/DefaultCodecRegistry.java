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
package org.granite.messaging.jmf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.messaging.jmf.codec.BijectiveCodec;
import org.granite.messaging.jmf.codec.ConditionalObjectCodec;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.PrimitiveCodec;
import org.granite.messaging.jmf.codec.StandardCodec;
import org.granite.messaging.jmf.codec.std.BooleanCodec;
import org.granite.messaging.jmf.codec.std.ByteCodec;
import org.granite.messaging.jmf.codec.std.CharacterCodec;
import org.granite.messaging.jmf.codec.std.DoubleCodec;
import org.granite.messaging.jmf.codec.std.FloatCodec;
import org.granite.messaging.jmf.codec.std.IntegerCodec;
import org.granite.messaging.jmf.codec.std.LongCodec;
import org.granite.messaging.jmf.codec.std.NullCodec;
import org.granite.messaging.jmf.codec.std.ShortCodec;
import org.granite.messaging.jmf.codec.std.StringCodec;
import org.granite.messaging.jmf.codec.std.impl.ArrayCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ArrayListCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.BigDecimalCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.BigIntegerCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.BooleanCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ByteCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.CharacterCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ClassCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.DateCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.DoubleCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.EnumCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.FloatCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.HashMapCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.HashSetCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.IntegerCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.LongCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.NullCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ObjectCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.ShortCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.SqlDateCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.SqlTimeCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.SqlTimestampCodecImpl;
import org.granite.messaging.jmf.codec.std.impl.StringCodecImpl;
import org.granite.messaging.reflect.Property;

/**
 * @author Franck WOLFF
 */
public class DefaultCodecRegistry implements CodecRegistry {

	private static final int[] UNPARAMETERIZED_JMF_TYPES = new int[256];
	static {
		for (int parameterizedJmfType = 0; parameterizedJmfType < 256; parameterizedJmfType++) {
			int jmfType;
			
			if ((parameterizedJmfType & 0x08) == 0x00)
				jmfType = (parameterizedJmfType & 0x07);
			else if ((parameterizedJmfType & 0x18) == 0x08)
				jmfType = (parameterizedJmfType & 0x0F);
			else if ((parameterizedJmfType & 0x38) == 0x18)
				jmfType = (parameterizedJmfType & 0x1F);
			else if ((parameterizedJmfType & 0x78) == 0x38)
				jmfType = (parameterizedJmfType & 0x3F);
			else
				jmfType = parameterizedJmfType;
			
			UNPARAMETERIZED_JMF_TYPES[parameterizedJmfType] = jmfType;
		}
	}

	private NullCodec nullCodec;

	private BooleanCodec booleanCodec;
	private CharacterCodec characterCodec;
	private ByteCodec byteCodec;
	private ShortCodec shortCodec;
	private IntegerCodec integerCodec;
	private LongCodec longCodec;
	private FloatCodec floatCodec;
	private DoubleCodec doubleCodec;
	private StringCodec stringCodec;
	
	private final Map<Integer, StandardCodec<?>> typeToCodec = new HashMap<Integer, StandardCodec<?>>();
	private final Map<Class<?>, StandardCodec<?>> classToCodec = new HashMap<Class<?>, StandardCodec<?>>();
	private final List<ConditionalObjectCodec> conditionalObjectCodecs = new ArrayList<ConditionalObjectCodec>();
	private final Map<Class<?>, PrimitivePropertyCodec> primitivePropertyCodecs = new HashMap<Class<?>, PrimitivePropertyCodec>();

	private final List<ExtendedObjectCodec> extendedCodecs;
	
	public DefaultCodecRegistry() {
		this(null);
	}
		
	public DefaultCodecRegistry(List<ExtendedObjectCodec> extendedCodecs) {
		this.extendedCodecs = (extendedCodecs != null ? extendedCodecs : new ArrayList<ExtendedObjectCodec>());

		List<StandardCodec<?>> standardCodecs = getStandardCodecs();
		for (StandardCodec<?> codec : standardCodecs) {
			
			if (codec instanceof BijectiveCodec) {
				if (codec instanceof PrimitiveCodec) {
					assertNull(classToCodec.put(((PrimitiveCodec<?>)codec).getPrimitiveClass(), codec));
					assertNull(typeToCodec.put(((PrimitiveCodec<?>)codec).getPrimitiveType(), codec));
					
					switch (((PrimitiveCodec<?>)codec).getPrimitiveType()) {
						case JMF_BOOLEAN: initBooleanCodec((BooleanCodec)codec); break;
						case JMF_CHARACTER: initCharacterCodec((CharacterCodec)codec); break;
						case JMF_BYTE: initByteCodec((ByteCodec)codec); break;
						case JMF_SHORT: initShortCodec((ShortCodec)codec); break;
						case JMF_INTEGER: initIntegerCodec((IntegerCodec)codec); break;
						case JMF_LONG: initLongCodec((LongCodec)codec); break;
						case JMF_FLOAT: initFloatCodec((FloatCodec)codec); break;
						case JMF_DOUBLE: initDoubleCodec((DoubleCodec)codec); break;
					}
				}
				
				assertNull(classToCodec.put(((BijectiveCodec<?>)codec).getObjectClass(), codec));
				assertNull(typeToCodec.put(codec.getObjectType(), codec));
				
				if (codec.getObjectType() == JMF_STRING)
					initStringCodec((StringCodec)codec);
				else if (codec.getObjectType() == JMF_NULL)
					initNullCodec((NullCodec)codec);
			}
			else if (codec instanceof ConditionalObjectCodec) {
				assertNull(typeToCodec.put(codec.getObjectType(), codec));
				conditionalObjectCodecs.add((ConditionalObjectCodec)codec);
			}
			else
				throw new JMFConfigurationException("Codec must implement BijectiveCodec or ConditionalObjectCodec: " + codec);
		}
		
		checkPrimitiveCodecs();
	}

	public NullCodec getNullCodec() {
		return nullCodec;
	}

	public BooleanCodec getBooleanCodec() {
		return booleanCodec;
	}

	public CharacterCodec getCharacterCodec() {
		return characterCodec;
	}

	public ByteCodec getByteCodec() {
		return byteCodec;
	}

	public ShortCodec getShortCodec() {
		return shortCodec;
	}

	public IntegerCodec getIntegerCodec() {
		return integerCodec;
	}

	public LongCodec getLongCodec() {
		return longCodec;
	}

	public FloatCodec getFloatCodec() {
		return floatCodec;
	}

	public DoubleCodec getDoubleCodec() {
		return doubleCodec;
	}

	public StringCodec getStringCodec() {
		return stringCodec;
	}

	@SuppressWarnings("unchecked")
	public <T> StandardCodec<T> getCodec(int jmfType) {
		return (StandardCodec<T>)typeToCodec.get(jmfType);
	}

	@SuppressWarnings("unchecked")
	public <T> StandardCodec<T> getCodec(Object v) {
		Class<?> cls = (v != null ? v.getClass() : null);
		StandardCodec<T> codec = (StandardCodec<T>)classToCodec.get(cls);
		if (codec == null) {
			for (ConditionalObjectCodec condCodec : conditionalObjectCodecs) {
				if (condCodec.canEncode(v)) {
					codec = (StandardCodec<T>)condCodec;
					break;
				}
			}
		}
		return codec;
	}

	public ExtendedObjectCodec findExtendedEncoder(ExtendedObjectOutput out, Object v) {
		for (ExtendedObjectCodec c : extendedCodecs) {
			if (c.canEncode(out, v))
				return c;
		}
		return null;
	}

	public ExtendedObjectCodec findExtendedDecoder(ExtendedObjectInput in, String className) {
		for (ExtendedObjectCodec c : extendedCodecs) {
			try {
				if (c.canDecode(in, className))
					return c;
			}
			catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	public PrimitivePropertyCodec getPrimitivePropertyCodec(Class<?> propertyCls) {
		return primitivePropertyCodecs.get(propertyCls);
	}
	
	public int extractJmfType(int parameterizedJmfType) {
		return UNPARAMETERIZED_JMF_TYPES[parameterizedJmfType];
	}

	public int jmfTypeOfPrimitiveClass(Class<?> cls) {
		if (!cls.isPrimitive())
			return -1;
		StandardCodec<?> codec = classToCodec.get(cls);
		return (codec instanceof PrimitiveCodec ? ((PrimitiveCodec<?>)codec).getPrimitiveType() : -1);
	}

	public Class<?> primitiveClassOfJmfType(int jmfType) {
		StandardCodec<?> codec = typeToCodec.get(Integer.valueOf(jmfType));
		return (codec instanceof PrimitiveCodec && ((PrimitiveCodec<?>)codec).getPrimitiveType() == jmfType ? ((PrimitiveCodec<?>)codec).getPrimitiveClass() : null);
	}
	
	protected List<StandardCodec<?>> getStandardCodecs() {
		return Arrays.asList((StandardCodec<?>)
			new NullCodecImpl(),
				
			new BooleanCodecImpl(),
			new CharacterCodecImpl(),
			new ByteCodecImpl(),
			new ShortCodecImpl(),
			new IntegerCodecImpl(),
			new LongCodecImpl(),
			new FloatCodecImpl(),
			new DoubleCodecImpl(),

			new BigIntegerCodecImpl(),
			new BigDecimalCodecImpl(),

			new StringCodecImpl(),

			new DateCodecImpl(),
			new SqlDateCodecImpl(),
			new SqlTimeCodecImpl(),
			new SqlTimestampCodecImpl(),

			new ArrayListCodecImpl(),
			new HashSetCodecImpl(),
			new HashMapCodecImpl(),

			new EnumCodecImpl(),
			new ArrayCodecImpl(),
			new ClassCodecImpl(),
			new ObjectCodecImpl()
		);
	}
	
	private void assertNull(StandardCodec<?> codec) {
		if (codec != null)
			throw new JMFConfigurationException("Codec conflict with: " + codec);
	}
	
	private void checkPrimitiveCodecs() {
		if (nullCodec == null)
			throw new JMFConfigurationException("No Null codec");
		
		if (booleanCodec == null)
			throw new JMFConfigurationException("No Boolean codec");
		if (characterCodec == null)
			throw new JMFConfigurationException("No Character codec");
		if (byteCodec == null)
			throw new JMFConfigurationException("No Byte codec");
		if (shortCodec == null)
			throw new JMFConfigurationException("No Short codec");
		if (integerCodec == null)
			throw new JMFConfigurationException("No Integer codec");
		if (longCodec == null)
			throw new JMFConfigurationException("No Long codec");
		if (floatCodec == null)
			throw new JMFConfigurationException("No Float codec");
		if (doubleCodec == null)
			throw new JMFConfigurationException("No Double codec");
		
		if (stringCodec == null)
			throw new JMFConfigurationException("No String codec");
	}

	private void initBooleanCodec(BooleanCodec codec) {
		booleanCodec = codec;
		primitivePropertyCodecs.put(booleanCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				booleanCodec.encodePrimitive(ctx, property.getBoolean(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setBoolean(holder, booleanCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initCharacterCodec(CharacterCodec codec) {
		characterCodec = codec;
		primitivePropertyCodecs.put(characterCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				characterCodec.encodePrimitive(ctx, property.getChar(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setChar(holder, characterCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initByteCodec(ByteCodec codec) {
		byteCodec = codec;
		primitivePropertyCodecs.put(byteCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				byteCodec.encodePrimitive(ctx, property.getByte(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setByte(holder, byteCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initShortCodec(ShortCodec codec) {
		shortCodec = codec;
		primitivePropertyCodecs.put(shortCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				shortCodec.encodePrimitive(ctx, property.getShort(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setShort(holder, shortCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initIntegerCodec(IntegerCodec codec) {
		integerCodec = codec;
		primitivePropertyCodecs.put(integerCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				integerCodec.encodePrimitive(ctx, property.getInt(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setInt(holder, integerCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initLongCodec(LongCodec codec) {
		longCodec = codec;
		primitivePropertyCodecs.put(longCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				longCodec.encodePrimitive(ctx, property.getLong(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setLong(holder, longCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initFloatCodec(FloatCodec codec) {
		floatCodec = codec;
		primitivePropertyCodecs.put(floatCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				floatCodec.encodePrimitive(ctx, property.getFloat(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setFloat(holder, floatCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initDoubleCodec(DoubleCodec codec) {
		doubleCodec = codec;
		primitivePropertyCodecs.put(doubleCodec.getPrimitiveClass(), new PrimitivePropertyCodec() {
			public void encodePrimitive(OutputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				doubleCodec.encodePrimitive(ctx, property.getDouble(holder));
			}
			public void decodePrimitive(InputContext ctx, Object holder, Property property) throws IllegalAccessException, IOException, InvocationTargetException {
				property.setDouble(holder, doubleCodec.decodePrimitive(ctx));
			}
		});
	}

	private void initStringCodec(StringCodec codec) {
		stringCodec = codec;
	}

	private void initNullCodec(NullCodec codec) {
		nullCodec = codec;
	}
}

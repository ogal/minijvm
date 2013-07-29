package minijvm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/*
 * see http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html
 * 
   ClassFile {
    	u4 magic;
    	u2 minor_version;
    	u2 major_version;
    	u2 constant_pool_count;
    	cp_info constant_pool[constant_pool_count-1];
    	u2 access_flags;
    	u2 this_class;
    	u2 super_class;
    	u2 interfaces_count;
    	u2 interfaces[interfaces_count];
    	u2 fields_count;
    	field_info fields[fields_count];
    	u2 methods_count;
    	method_info methods[methods_count];
    	u2 attributes_count;
    	attribute_info attributes[attributes_count];
    }

 * 
 */
public class JavaClassFile {
	private String magicNumber;
	private int constantPoolCount;
	private ArrayList<Map<ConstantPoolTag, Object>> constantPool;
	
	public byte[] read(String filename) throws IOException {
		return Files.readAllBytes(Paths.get(filename));
	}

	public static void main(String[] args) throws IOException {
		JavaClassFile classFile = new JavaClassFile();
		
//		byte[] cl = new JavaClassFile().read("C:\\Users\\gal02669\\projects\\MBS\\workspace_oss\\minijvm\\bin\\minijvm\\JavaClassFile.class");
		byte[] cl = new JavaClassFile().read("C:\\Temp\\HotSpotVirtualMachine.class");
		System.out.println("Magic number:");
		System.out.println(Integer.toHexString((int) cl[0] & 0xff)
				+ Integer.toHexString((int) cl[1] & 0xff)
				+ Integer.toHexString((int) cl[2] & 0xff)
				+ Integer.toHexString((int) cl[3] & 0xff));
		
		System.out.println("minor version");
		System.out.println(cl[4]);
		System.out.println(cl[5]);
		System.out.println("major version");
		System.out.println(cl[6]);
		System.out.println(cl[7]);
		
		int constantPoolCount = getIntValue(cl[8], cl[9]);
		System.out.println("constant pool count:" + constantPoolCount);
		classFile.constantPoolCount = constantPoolCount;
		classFile.constantPool = new ArrayList<>(constantPoolCount);
		classFile.constantPool.add(null);
		
		int idx = 10;
		
		for (int i = 1; i < constantPoolCount; i++) {
			System.out.println("i=" + i);
			int tag = (int) cl[idx++];
			HashMap hm = new HashMap();
			if(tag == ConstantPoolTag.CONSTANT_Class.tag) {
				int table_index = getIntValue(cl[idx++], cl[idx++]);
				System.out.println("CONSTANT_Class table index = " + table_index);
				hm.put(ConstantPoolTag.CONSTANT_Class, table_index);
			}
			else if(tag == ConstantPoolTag.CONSTANT_Utf8.tag) {
				int length = getIntValue(cl[idx++], cl[idx++]);
				String s = new String(cl, idx, length, "UTF-8");
				System.out.println("CONSTANT_Utf8 '" + s + "'");
				idx += length;
				hm.put(ConstantPoolTag.CONSTANT_Utf8, s);
			}
			else if(tag == ConstantPoolTag.CONSTANT_Methodref.tag) {
				int class_index = getIntValue(cl[idx++], cl[idx++]);
				int name_and_type_index = getIntValue(cl[idx++], cl[idx++]);
				System.out.println("CONSTANT_Methodref class_index=" + class_index + " name_and_type_index=" + name_and_type_index);
				hm.put(ConstantPoolTag.CONSTANT_Methodref, name_and_type_index);
			}
			else if(tag == ConstantPoolTag.CONSTANT_NameAndType.tag) {
				int name_index = getIntValue(cl[idx++], cl[idx++]);
				int descriptor_index = getIntValue(cl[idx++], cl[idx++]);
				System.out.println("CONSTANT_NameAndType class_index=" + name_index + " name_and_type_index=" + descriptor_index);
				hm.put(ConstantPoolTag.CONSTANT_NameAndType, new int[] { descriptor_index, descriptor_index});
			}
			else if(tag == ConstantPoolTag.CONSTANT_String.tag) {
				int string_index = getIntValue(cl[idx++], cl[idx++]);
				hm.put(ConstantPoolTag.CONSTANT_String, string_index);
				System.out.println("CONSTANT_String string_index=" + string_index);
			}
			else if(tag == ConstantPoolTag.CONSTANT_Fieldref.tag) {
				int class_index = getIntValue(cl[idx++], cl[idx++]);
				int name_and_type_index = getIntValue(cl[idx++], cl[idx++]);
				System.out.println("CONSTANT_Fieldref class_index=" + class_index + " name_and_type_index=" + name_and_type_index);
				hm.put(ConstantPoolTag.CONSTANT_Fieldref, new int[] { class_index, name_and_type_index});
			}
			else if(tag == ConstantPoolTag.CONSTANT_InterfaceMethodref.tag) {
				int class_index = getIntValue(cl[idx++], cl[idx++]);
				int name_and_type_index = getIntValue(cl[idx++], cl[idx++]);
				System.out.println("CONSTANT_InterfaceMethodref class_index=" + class_index + " name_and_type_index=" + name_and_type_index);
				hm.put(ConstantPoolTag.CONSTANT_InterfaceMethodref, new int[] { class_index, name_and_type_index});
			}
			else if(tag == ConstantPoolTag.CONSTANT_Integer.tag) {
				int val = cl[idx++] << 24 | cl[idx++] << 16 | cl[idx++] << 8 | cl[idx++];
				System.out.println("CONSTANT_Integer val=" + val);
				hm.put(ConstantPoolTag.CONSTANT_Integer, val);
			}
			else if(tag == ConstantPoolTag.CONSTANT_Long.tag) {
				int highbytes = ((cl[idx++] & 0xff) << 24 | (cl[idx++] & 0xff) << 16 | (cl[idx++] & 0xff) << 8 | (cl[idx++] & 0xff));
				int lowbytes = ((cl[idx++] & 0xff) << 24 | (cl[idx++] & 0xff) << 16 | (cl[idx++] & 0xff) << 8 | (cl[idx++] & 0xff));
				System.out.println("highbytes=" + highbytes + " " + lowbytes);
				long val = (highbytes << 32) | lowbytes;
				System.out.println("CONSTANT_Long val=" + val);
				i++;
				classFile.constantPool.add(null);
				hm.put(ConstantPoolTag.CONSTANT_Long, val);
			}
			else if(tag == ConstantPoolTag.CONSTANT_Double.tag) {
				int highbytes = cl[idx++] << 24 | cl[idx++] << 16 | cl[idx++] << 8 | cl[idx++];
				int lowbytes = cl[idx++] << 24 | cl[idx++] << 16 | cl[idx++] << 8 | cl[idx++];
				System.out.println("CONSTANT_Double val=TODO");
			}
			else if(tag == ConstantPoolTag.CONSTANT_Float.tag) {
				int bytes = cl[idx++] << 24 | cl[idx++] << 16 | cl[idx++] << 8 | cl[idx++];
				System.out.println("CONSTANT_Float val=TODO");
			}
			else {
				System.out.println("!!! UNKNOWN tag=" + tag);
			}
			classFile.constantPool.add(i, hm);


		}
		
		
//		System.out.println();
//		System.out.println(cl[11]);
//		System.out.println(cl[12]);
//		System.out.println(cl[13]);
//		System.out.println(cl[14]);
//		System.out.println(cl[15]);
//		System.out.println(cl[16]);
//		System.out.println(cl[17]);
//		System.out.println(cl[18]);
//		System.out.println(cl[19]);
//		System.out.println(cl[20]);
//		System.out.println(cl[21]);
//		System.out.println(cl[22]);
//		System.out.println(cl[23]);
//		System.out.println(cl[24]);
		
//		ConstantPoolTag.CONSTANT_Class.
		
//		byte bb = 1;
//		int aa = bb << 8;
//		System.out.println(aa);
		
//		idx = 10 + constantPoolCount;
//		System.out.println("idx=" + idx);
		System.out.println("----");
		System.out.println(cl[idx]);
		System.out.println(cl[idx + 1]);
		System.out.println(cl[idx + 2]);
		System.out.println(cl[idx + 3] & 0xff);
		System.out.println(cl[idx + 4]);
		System.out.println(cl[idx + 5]);
		System.out.println(cl[idx + 6]);
		System.out.println(cl[idx + 7]);
		System.out.println(cl[idx + 8]);
		System.out.println(cl[idx + 9]);
		System.out.println(cl[idx + 10]);
		int accessFlags = getIntValue(cl[idx++], cl[idx++]);
		System.out.println("accessFlags=0x" + Integer.toHexString(accessFlags) +  " " + accessFlags);
		
		int this_class_index = getIntValue(cl[idx++], cl[idx++]);
		System.out.println("this_class_index=" + this_class_index);

		int super_class_index = getIntValue(cl[idx++], cl[idx++]);
		System.out.println("super_class_index=" + super_class_index);

		int interface_count = getIntValue(cl[idx++], cl[idx++]);
		System.out.println("interface_count=" + interface_count);

		for (int i = 0; i < interface_count; i++) {
			int constant_pool_index = getIntValue(cl[idx++], cl[idx++]);
			
		}

		
		int fields_count = getIntValue(cl[idx++], cl[idx++]);
		System.out.println("fields_count=" + fields_count);

		for (int i = 0; i < fields_count; i++) {
			idx += classFile.getFieldInfo(cl, idx);
		}
		
		
		System.out.println();
		int methods_count = getIntValue(cl[idx++], cl[idx++]);
		System.out.println("methods_count=" + methods_count);

		for (int i = 0; i < methods_count; i++) {
			System.out.println("getMethodInfo");
			idx += classFile.getMethodInfo(cl, idx);
		}
		
//		byte bb = -1;
//		int ii = (bb & 0xff) << 1;
//		int iii = bb << 1;
//		System.out.println(ii);
//		System.out.println(iii);
	}
	
	public enum ConstantPoolTag {
		CONSTANT_Class(7),
		CONSTANT_Fieldref(9),
		CONSTANT_Methodref(10),
		CONSTANT_InterfaceMethodref(11),
		CONSTANT_String(8),
		CONSTANT_Integer(3),
		CONSTANT_Float(4),
		CONSTANT_Long(5),
		CONSTANT_Double(6),
		CONSTANT_NameAndType(12),
		CONSTANT_Utf8(1);
		
		private ConstantPoolTag(int tag) {
			this.tag = tag;
		}
		
		public int tag;
	}

	public static int getIntValue(byte b1, byte b2) {
		return Integer.parseInt( String.valueOf(b1 & 0xFF) + String.valueOf(b2 & 0xFF));
	}

	public static int getIntValue(byte b1, byte b2, byte b3, byte b4) {
		return (b1 & 0xff << 24 | b2 & 0xff << 16 | b3 & 0xff << 8 | b4 & 0xff);
	}

	public int getFieldInfo(byte[] a, int start) {
		int i = start;
		
		int accessFlags = getIntValue(a[i++], a[i++]);
		System.out.println("accessFlags=" + accessFlags);
		int nameIndex =  getIntValue(a[i++], a[i++]);
		System.out.println("nameIndex=" + nameIndex);
		int descriptorIndex =  getIntValue(a[i++], a[i++]);
		System.out.println("descriptorIndex=" + descriptorIndex);
		int attributesCount =  getIntValue(a[i++], a[i++]);
		System.out.println("attributesCount=" + attributesCount);
		
		for (int j = 0; j < attributesCount; j++) {
			i += getAttributeInfo(a, i);
		}
		
		return i - start;
	}
	
	public int getAttributeInfo(byte[] a, int start) {
		int i = start;
		int attributeNameIndex = getIntValue(a[i++], a[i++]);
		System.out.println("attributeNameIndex: " + attributeNameIndex);
		String attrName = (String) constantPool.get(attributeNameIndex).get(ConstantPoolTag.CONSTANT_Utf8);
		int attribute_length = a[i++] & 0xff << 24 | a[i++] & 0xff << 16 | a[i++] & 0xff << 8 | a[i++] & 0xff;
		System.out.println("attribute_length: " + attribute_length);
		if(attrName.equals("Code")) {
			System.out.println("Code");
			int maxStack = getIntValue(a[i++], a[i++]);
			int max_locals = getIntValue(a[i++], a[i++]);
			System.out.println("max stack=" + maxStack);
			System.out.println("max_locals=" + max_locals);
			int code_length = getIntValue(a[i++], a[i++], a[i++], a[i++]);
			System.out.println("code_length=" + code_length);
			for (int j = 0; j < code_length; j++) {
				System.out.println(Integer.toHexString(a[i++] & 0xff));
			}
			int exception_table_length = getIntValue(a[i++], a[i++]);
			System.out.println("exception_table_length=" + exception_table_length);
			for (int j = 0; j < exception_table_length; j++) {
				int start_pc = getIntValue(a[i++], a[i++]);
				int end_pc = getIntValue(a[i++], a[i++]);
				int handler_pc = getIntValue(a[i++], a[i++]);
				int catch_type = getIntValue(a[i++], a[i++]);
				System.out.println("start_pc=" + start_pc + " end_pc=" + end_pc + " handler_pc=" + handler_pc + "  catch_type=" + catch_type);
			}
			int attributes_count = getIntValue(a[i++], a[i++]);
			System.out.println("attributes_count=" + attributes_count);
			for (int j = 0; j < attributes_count; j++) {
				i += getAttributeInfo(a, i);
			}
		}
		else if(attrName.equals("LineNumberTable")) {
			System.out.println("LineNumberTable");
//			int attribute_name_index = getIntValue(a[i++], a[i++]);
//			attribute_length = getIntValue(a[i++], a[i++], a[i++], a[i++]);
			int line_number_table_length = getIntValue(a[i++], a[i++]);
			System.out.println("line_number_table_length=" + line_number_table_length);
			for (int j = 0; j < line_number_table_length; j++) {
				int start_pc = getIntValue(a[i++], a[i++]);
				int line_number = getIntValue(a[i++], a[i++]);
				System.out.println("start_pc=" + start_pc + " line_number=" + line_number);
			}
		}
		else {
			for (int j = 0; j < attribute_length; j++) {
				byte b = a[i++];
			}
		}
		
		return i - start;
	}
	
	public int getMethodInfo(byte[] a, int start) {
		int i = start;
		int accessFlags = getIntValue(a[i++], a[i++]);
		System.out.println("accessFlags=" + accessFlags);
		int nameIndex = getIntValue(a[i++], a[i++]);
		System.out.println("nameIndex=" + nameIndex);
		System.out.println("'" + constantPool.get(nameIndex).get(ConstantPoolTag.CONSTANT_Utf8) + "'");
		int descriptorIndex = getIntValue(a[i++], a[i++]);
		System.out.println("descriptorIndex=" + descriptorIndex);
		System.out.println("'" + constantPool.get(descriptorIndex).get(ConstantPoolTag.CONSTANT_Utf8) + "'");
	
		int attributesCount = getIntValue(a[i++], a[i++]);
		System.out.println("attributesCount=" + attributesCount);

		for (int j = 0; j < attributesCount; j++) {
			i += getAttributeInfo(a, i);
		}
		return i - start;
	}
	
}


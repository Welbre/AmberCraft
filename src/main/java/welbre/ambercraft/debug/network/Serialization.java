package welbre.ambercraft.debug.network;

import org.apache.commons.lang3.NotImplementedException;
import sun.reflect.ReflectionFactory;//fixme refactor this to don't use the Reflection factory

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Serialization
{
    private record Header(int index, Class<?> c){}

    public static byte[] serialize(Object... varg) throws Exception
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Collect all objects
        List<Object> references = new ArrayList<>();
        for (Object object : varg)
            COLLECT_ALL_OBJECTS(references, object);

        // Create the header
        WRITE_INT(stream, references.size());
        for (int i = 0; i < references.size(); i++)
            WRITE_OBJECT_HEADER(stream, references.get(i), i);
        WRITE_INT(stream, varg.length);//write the numbers of roots
        for (Object object : varg)
            WRITE_REFERENCE(stream, object, references);

        // Write the objects
        for (Object obj : references)
            if (obj.getClass().isArray())
                WRITE_GENERIC_ARRAY(stream, obj, references);
            else if (obj.getClass() == String.class)
                WRITE_UTF8(stream, (String) obj);
            else
                for (Field field : getNestedFields(obj.getClass()))
                    if (field.getType().isPrimitive())
                        WRITE_PRIMITIVE(stream, field.getType(), FORCED_GET(field, obj));
                    else
                        WRITE_REFERENCE(stream, FORCED_GET(field, obj), references);//writes reference to be recovered in the unSerialization
        stream.close();

        return stream.toByteArray();
    }

    public static Object[] deserialize(byte[] data) throws Exception
    {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        ArrayList<Object> references = new ArrayList<>();

        //read the header and create all references, don't serialize yet!, only create all references in the heap.
        final int head_size = READ_INT(stream);
        for (int i = 0; i < head_size; i++)
        {
            Header header = READ_OBJECT_HEADER(stream);
            if (i != header.index)
                throw new IllegalStateException("Index %d out of bonds in the deserialization!".formatted(i));
            Object obj = CREATE_HEADER_OBJ(stream, header);
            references.add(obj);
        }
        final int rootLength = READ_INT(stream);//read root length.
        final int[] root = new int[rootLength];
        for (int i = 0; i < rootLength; i++)
            root[i] = READ_INT(stream);

        //start unserialization
        for (Object obj : references)
            if (obj.getClass().isArray())
                READ_GENERIC_ARRAY(stream, obj, references);
            else if (obj.getClass() == String.class)
                CHANGE_REFERENCE(references, obj, READ_UTF8(stream));
            else
                for (Field field : getNestedFields(obj.getClass()))
                    if (field.getType().isPrimitive())
                        FORCED_SET(field, obj, READ_PRIMITIVE(stream, field.getType()));
                    else
                        FORCED_SET(field, obj, READ_REFERENCE(stream, references));

        return Arrays.stream(root).mapToObj(references::get).toArray(Object[]::new);
    }

    /// Iterate in all objects to change the pointer from {@code old} to {@code newOn}
    private static void CHANGE_REFERENCE(ArrayList<Object> references, Object old, Object newOn) throws IllegalAccessException {
        for (int i = 0; i < references.size(); i++)
        {
            Object obj = references.get(i);
            if (obj == old)
                references.set(i, newOn);//update the object in the reference list.

            for (Field field : getNestedFields(obj.getClass()))
            {
                if (field.getType() == old.getClass())//only an optimization to avoid getting the data in the field
                {
                    Object got = FORCED_GET(field, obj);
                    if (got == old)// found a reference to this object, so change to newOn
                        FORCED_SET(field, obj, newOn);
                }
            }
        }
    }

    private static Object READ_PRIMITIVE(InputStream stream, Class<?> type) throws IOException {
        DataInputStream data = new DataInputStream(stream);
        if (type == byte.class)
            return data.readByte();
        else if (type == char.class)
            return data.readChar();
        else if (type == short.class)
            return data.readShort();
        else if (type == int.class)
            return data.readInt();
        else if (type == long.class)
            return data.readLong();
        else if (type == float.class)
            return data.readFloat();
        else if (type == double.class)
            return data.readDouble();
        else if (type == boolean.class)
            return data.readBoolean();
        else throw new NotImplementedException("Primitive type " + type.getName() + " isn't supported yet!");
    }

    private static void WRITE_PRIMITIVE(OutputStream stream, Class<?> type, Object obj) throws IOException
    {
        DataOutputStream data = new DataOutputStream(stream);
        if (type == byte.class)
            data.writeByte((byte) obj);
        else if (type == char.class)
            data.writeChar((char) obj);
        else if (type == short.class)
            data.writeShort((short) obj);
        else if (type == int.class)
            data.writeInt((int) obj);
        else if (type == long.class)
            data.writeLong((long) obj);
        else if (type == float.class)
            data.writeFloat((float) obj);
        else if (type == double.class)
            data.writeDouble((double) obj);
        else if (type == boolean.class)
            data.writeBoolean((boolean) obj);
        else
            throw new NotImplementedException("Primitive type " + type.getName() + " isn't supported yet!");
    }

    private static void READ_GENERIC_ARRAY(InputStream stream, Object object, List<Object> referenceList) throws IOException
    {
        final int length = READ_INT(stream);
        final Class<?> c = object.getClass().getComponentType();

        if (c.isPrimitive())
            for (int i = 0; i < length; i++)
                Array.set(object, i, READ_PRIMITIVE(stream, c));
        else
            for (int i = 0; i < length; i++)
                Array.set(object, i, READ_REFERENCE(stream, referenceList));
    }

    private static void WRITE_GENERIC_ARRAY(OutputStream stream, Object obj, List<Object> referenceList) throws IOException {
        final int length = Array.getLength(obj);
        WRITE_INT(stream, length);
        if (obj.getClass().getComponentType().isPrimitive())
            for (int i = 0; i < length; i++)
                WRITE_PRIMITIVE(stream, obj.getClass().getComponentType(), Array.get(obj, i));
        else
            for (Object object : (Object[]) obj)//writes all references in the array
                WRITE_REFERENCE(stream, object, referenceList);
    }

    private static Object CREATE_HEADER_OBJ(InputStream stream, Header header) throws InvocationTargetException, InstantiationException, IllegalAccessException, IOException {
        if (header.c.isArray())
            return Array.newInstance(header.c.getComponentType(), READ_INT(stream));//alloc the array

        ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
        var constructor = rf.newConstructorForSerialization(header.c);
        constructor.setAccessible(true);

        return constructor.newInstance();
    }

    private static Header READ_OBJECT_HEADER(InputStream stream) throws IOException, ClassNotFoundException {
        final int index = READ_INT(stream);
        String class_name = READ_UTF8(stream);
        Class<?> c = Class.forName(class_name);
        return new Header(index, c);
    }

    private static void WRITE_OBJECT_HEADER(OutputStream stream, Object value, int index) throws IOException {
        WRITE_INT(stream, index);
        WRITE_UTF8(stream, value.getClass().getName());
        if (value.getClass().isArray())
            WRITE_INT(stream, Array.getLength(value));
    }

    private static String READ_UTF8(InputStream stream) throws IOException
    {
        int len = READ_INT(stream);
        final byte[] data = new byte[len];
        int result = stream.read(data, 0, len);
        if (result != len) throw new IOException("Unexpected end of stream!");
        return new String(data, StandardCharsets.UTF_8);
    }

    private static void WRITE_UTF8(OutputStream stream, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        WRITE_INT(stream, bytes.length);
        stream.write(bytes, 0, bytes.length);
    }

    private static int READ_INT(InputStream stream) throws IOException {
        final byte[] data = new byte[4];
        int result = stream.read(data, 0, 4);
        if (result != 4) throw new IOException("Unexpected end of stream!");
        return (data[0] << 24) | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[3] & 0xFF);
    }

    private static void WRITE_INT(OutputStream stream, int i) throws IOException {
        stream.write((i >> 24) & 0xFF);
        stream.write((i >> 16) & 0xFF);
        stream.write((i >> 8) & 0xFF);
        stream.write(i & 0xFF);
    }

    private static Object READ_REFERENCE(InputStream stream, List<Object> objects) throws IOException
    {
        final int index = READ_INT(stream);
        if (index == -1)
            return null;
        else
        if (index >= 0 && index < objects.size())
            return objects.get(index);
        else
            throw new IllegalStateException("Index %d out of bonds in the deserialization!".formatted(index));
    }

    /// Writes a reference to an object in the object list
    private static void WRITE_REFERENCE(OutputStream stream, Object obj, List<Object> referenceList) throws IOException
    {
        if (obj == null)
            WRITE_INT(stream, -1);
        else
        {
            final int index = referenceList.indexOf(obj);
            if (index == -1)
                throw new IllegalStateException("Object " + obj + " isn't registered in the serialization!");
            WRITE_INT(stream, index);
        }
    }

    /// Recursively search of objects in all filed and store then in the {@code referenceList}
    private static void COLLECT_ALL_OBJECTS(List<Object> referenceList, Object obj) throws IllegalAccessException {
        if (obj == null || referenceList.contains(obj))
            return;
        referenceList.add(obj);

        if (obj.getClass().isArray())
        {
            if (obj.getClass().getComponentType().isPrimitive())
                return;
            Object[] array = (Object[]) obj;
            for (Object o : array)
                COLLECT_ALL_OBJECTS(referenceList, o);
        } else {
            if (obj instanceof String)//don't search in the array fields!
                return;
            for (Field field : getNestedFields(obj.getClass()))
            {
                if (field.getType().isPrimitive())
                    continue;
                COLLECT_ALL_OBJECTS(referenceList, FORCED_GET(field, obj));
            }
        }
    }

    /// Get all non-static fields in this class and subclasses.
    private static List<Field> getNestedFields(Class<?> c)
    {
        List<Field> fields = new ArrayList<>();
        Queue<Class<?>> queue = new ArrayDeque<>(List.of(c));
        while (!queue.isEmpty())
        {
            var next = queue.poll();
            Arrays.stream(next.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))//filter static
                    .filter(f -> !Modifier.isTransient(f.getModifiers()))//remove transient
                    .forEach(fields::add);
            queue.addAll(Arrays.asList(next.getInterfaces()));
            if (next.getSuperclass() != null)
                queue.add(next.getSuperclass());
        }

        return fields.stream().sorted(Comparator.comparing(Field::getName)).toList();
    }

    private static void FORCED_SET(Field field, Object obj, Object a) throws IllegalAccessException {
        final boolean flag = field.canAccess(obj);
        field.setAccessible(true);
        field.set(obj, a);
        field.setAccessible(flag);
    }

    private static Object FORCED_GET(Field field, Object obj) throws IllegalAccessException {
        try
        {
            final boolean flag = field.canAccess(obj);
            field.setAccessible(true);
            Object get = field.get(obj);
            field.setAccessible(flag);
            return get;
        } catch (Exception e)
        {
            throw new RuntimeException("Fail to get filed in object of type " + obj.getClass().getName() + " and field " + field.getName() + "!", e);
        }
    }
}

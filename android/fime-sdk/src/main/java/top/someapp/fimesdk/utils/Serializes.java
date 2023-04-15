/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.utils;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author zwz
 * Created on 2023-02-06
 */
public class Serializes {

    private static SerializerFactory factory;

    private Serializes() {
        // no instance
    }

    public static void serialize(Object obj, OutputStream target) throws IOException {
        hessianSerialize(obj, target, null);
    }

    public static <T> T deserialize(InputStream source) throws IOException {
        return hessianDeserialize(source, null);
    }

    public static void serialize(Object obj, OutputStream target, String head) throws IOException {
        hessianSerialize(obj, target, head);
    }

    public static <T> T deserialize(InputStream source, String headReg) throws IOException {
        return hessianDeserialize(source, headReg);
    }

    public static AbstractHessianOutput createOutput(OutputStream source) {
        AbstractHessianOutput out = new Hessian2Output(source);
        if (factory == null) factory = new SerializerFactory();
        out.setSerializerFactory(factory);
        return out;
    }

    public static AbstractHessianInput createInput(InputStream source) {
        AbstractHessianInput in = new Hessian2Input(source);
        if (factory == null) factory = new SerializerFactory();
        in.setSerializerFactory(factory);
        return in;
    }

    private static void hessianSerialize(Object obj, OutputStream target, String head) throws IOException {
        AbstractHessianOutput out = new Hessian2Output(target);
        if (factory == null) factory = new SerializerFactory();
        out.setSerializerFactory(factory);
        if (head != null) out.writeString(head);
        out.writeObject(obj);
        out.close();
        target.close();
    }

    @SuppressWarnings("unchecked")
    private static <T> T hessianDeserialize(InputStream source, String headReg) throws IOException {
        AbstractHessianInput in = new Hessian2Input(source);
        if (factory == null) factory = new SerializerFactory();
        in.setSerializerFactory(factory);
        if (headReg != null) {
            String s = in.readString();
            if (!s.matches(headReg)) return null;
        }
        T obj;
        obj = (T) in.readObject();
        in.close();
        source.close();
        return obj;
    }
}

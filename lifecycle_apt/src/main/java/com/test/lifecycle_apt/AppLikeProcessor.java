package com.test.lifecycle_apt;

import com.test.lifecycle_annotation.AppLifeCycle;

import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

/**
 * 实现注解处理器
 * <p>
 * 核心的注解处理类，在这里我们可以扫描源代码里所有的注解，找到我们需要的注解，然后作出相应处理
 */
public class AppLikeProcessor extends AbstractProcessor {

    private static final String INTERFACE_NAME = "";
    private Elements elementUtils;
    private Map<String, AppLikeProxyClassCreator> map = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
    }


    /**
     * 返回该注解处理器要解析的注解
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(AppLifeCycle.class.getCanonicalName());
        return set;
    }


    //支持的源代码 Java 版本号
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }


    //所有逻辑都在这里完成
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        //这里返回所有使用了AppLifeCycle 注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AppLifeCycle.class);
        map.clear();

        //遍历所有使用了该注解的元素
        for (Element element : elements) {
            //如果该注解不是用在类上面，直接抛出异常，该注解用在方法，字段等上面，为我们是不支持的
            if (!element.getKind().isClass()) {
                throw new RuntimeException("Annotation AppLifeCycle can only be used in class.");
            }

            //强制转换为TypeElement，也就是类元素，可获取使用该注解的类的相关信息
            TypeElement typeElement = (TypeElement) element;

            //这里检查一下，使用了该注解的类，同时必须实现INTERFACE_NAME 接口，否则会报错，因为我们要实现一个代理类
            List<? extends TypeMirror> mirrorsList = typeElement.getInterfaces();
            if (mirrorsList.isEmpty()) {
                throw new RuntimeException(typeElement.getQualifiedName() + "must implement interface " + INTERFACE_NAME);
            }

            boolean checkInterfaceFlag = false;
            for (TypeMirror mirror : mirrorsList) {
                if (INTERFACE_NAME.equals(mirror.toString())) {
                    checkInterfaceFlag = true;
                }
            }

            if (!checkInterfaceFlag) {
                throw new RuntimeException(typeElement.getQualifiedName() + "must implement interface " + INTERFACE_NAME);
            }

            //该类的全限定类名
            String fullClassName = typeElement.getQualifiedName().toString();
            if (!map.containsKey(fullClassName)) {
                System.out.println("process class name:" + fullClassName);
                //创建代理类生成器
                AppLikeProxyClassCreator creator = new AppLikeProxyClassCreator(elementUtils, typeElement);
                map.put(fullClassName, creator);
            }

            System.out.println("start to generate proxy class");
            //开始生成代理类
            Set<Map.Entry<String, AppLikeProxyClassCreator>> entries = map.entrySet();
            for (Map.Entry<String, AppLikeProxyClassCreator> entry : entries) {
                String className = entry.getKey();
                AppLikeProxyClassCreator creator = entry.getValue();

                System.out.println("generate proxy class for" + className);

                /*
                 *生成代理类，并写入到文件中，生成逻辑都在{@link AppLikeProxyClassCreator} 里实现
                 */
                try {
                    JavaFileObject jfo = processingEnv.getFiler().createSourceFile(creator.getProxyClassFullName());
                    Writer writer = jfo.openWriter();
                    writer.write(creator.generateJavaCode());
                    writer.flush();
                    writer.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}

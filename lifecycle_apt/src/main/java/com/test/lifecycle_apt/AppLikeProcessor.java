package com.test.lifecycle_apt;

import com.google.auto.service.AutoService;
import com.test.lifecycle_annotation.AppLifeCycle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 实现注解处理器
 * <p>
 * 核心的注解处理类，在这里我们可以扫描源代码里所有的注解，找到我们需要的注解，然后作出相应处理
 * <p>
 * AutoService本身就是一个静态注解，在build/META-INF文件夹下生成了一个service指定文件
 *
 * 目前使用javassist 方式  不需要生成代理类
 */
//@AutoService(Processor.class)
public class AppLikeProcessor extends AbstractProcessor {

    private String TAG = AppLikeProcessor.class.getSimpleName();

    private static final String INTERFACE_NAME = "com.test.lifecycle_api.IAppLike";
    private Elements elementUtils;
    private Map<String, AppLikeProxyClassCreator> map = new HashMap<>();

    /**
     * 用于打印日志
     * <p>
     * 也可以使用 System.out.println(）打印日志，在Build下看到对应的日志
     */
    private Messager messager;

    //用于文件处理
    private Filer filer;

    /**
     * 初始化方法会被注解处理工具调用，并传入参数processingEnvironment，该参数提供了很多有用的工具类，
     * 如：Elements、Types、Filter等等
     *
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        messager.printMessage(Diagnostic.Kind.NOTE, "--AppLikeProcessor----init()------");
    }


    /**
     * 该处理器支持的所有注解类集合，在这里可以添加自定义注解
     * <p>
     * 可以用注解 @SupportedAnnotationTypes("com.test.lifecycle_annotation.AppLifeCycle")
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AppLifeCycle.class.getCanonicalName());
    }


    /**
     * 该处理器支持的JDK版本，例如： SourceVersion.RELEASE_7
     * 一般返回 SourceVersion.latestSupported()
     * 也可以使用 @SupportedSourceVersion(SourceVersion.RELEASE_7)
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }


    /**
     * 相当于main函数，在这个方法中处理注解与生成新的文件，所有逻辑都在这里完成
     *
     * @param set              该方法需要处理的注解类型
     * @param roundEnvironment 关于一轮遍历中提供给我们调用的信息.
     * @return 改轮注解是否处理完成 true 下轮或者其他的注解处理器将不会接收到次类型的注解.用处不大
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        messager.printMessage(Diagnostic.Kind.NOTE, "----AppLikeProcessor -----process--");


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
                messager.printMessage(Diagnostic.Kind.NOTE, "process class name:" + fullClassName);

                //创建代理类生成器
                AppLikeProxyClassCreator creator = new AppLikeProxyClassCreator(elementUtils, typeElement);
                map.put(fullClassName, creator);
            }
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "start to generate proxy class");

        //开始生成代理类
        Set<Map.Entry<String, AppLikeProxyClassCreator>> entries = map.entrySet();
        for (Map.Entry<String, AppLikeProxyClassCreator> entry : entries) {
            String className = entry.getKey();
            AppLikeProxyClassCreator creator = entry.getValue();

            messager.printMessage(Diagnostic.Kind.NOTE, "generate proxy class for" + className);

            /*
             * 由于这个文件是在build 过程中创建的，所以只有build成功之后才可以查看到它，对应的在一下目录
             * app/build/generated/source/apt/debug/<package>/XXX.java
             *生成代理类，并写入到文件中，生成逻辑都在{@link AppLikeProxyClassCreator} 里实现
             */
            BufferedWriter writer = null;
            try {
                JavaFileObject jfo = filer.createSourceFile(creator.getProxyClassFullName());
                //Writer writer = jfo.openWriter();
                writer = new BufferedWriter(jfo.openWriter());
                writer.write(creator.generateJavaCode());
//                writer.flush();
//                writer.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }
}

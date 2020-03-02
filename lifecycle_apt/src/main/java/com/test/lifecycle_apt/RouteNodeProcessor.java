package com.test.lifecycle_apt;

import com.google.auto.service.AutoService;
import com.test.lifecycle_annotation.AutoWired;
import com.test.lifecycle_annotation.RouteNode;
import com.test.lifecycle_annotation.enums.NodeType;
import com.test.lifecycle_annotation.model.Node;
import com.test.lifecycle_apt.utils.Constants;
import com.test.lifecycle_apt.utils.Logger;
import com.test.lifecycle_apt.utils.TypeUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

/**
 * 实现注解处理器
 * <p>
 * 核心的注解处理类，在这里我们可以扫描源代码里所有的注解，找到我们需要的注解，然后作出相应处理
 * <p>
 * AutoService本身就是一个静态注解，在build/META-INF文件夹下生成了一个service指定文件
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.ANNOTATION_TYPE_ROUTE)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RouteNodeProcessor extends AbstractProcessor {

    private static final String INTERFACE_NAME = "";
    private Elements elementUtils;

    //用于文件处理
    private Filer filer;

    private List<Node> routeNodes;

    /**
     * 用于打印日志，简单将getMessager()封装
     * <p>
     * 也可以使用 System.out.println(）打印日志，在Build下看到对应的日志
     */
    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private TypeMirror typeString;
    private String host;

    /**
     * 初始化方法会被注解处理工具调用，并传入参数processingEnvironment，该参数提供了很多有用的工具类，
     * 如：Elements、Types、Filter等等
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        routeNodes = new ArrayList<>();

        types = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        logger = new Logger(processingEnv.getMessager());

        typeUtils = new TypeUtils(types, elementUtils);
        typeString = elementUtils.getTypeElement("java.lang.String").asType();

        Map<String, String> options = processingEnv.getOptions();
        if (options != null) {
            host = options.get(Constants.KEY_HOST_NAME);
            logger.info("host is " + host);
        }

        if (host == null || Constants.STRING_EMPTY.equals(host)) {
            host = Constants.DEFAULT_HOST;
        }
        logger.info("RouteNodeProcessor init ");
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

        if (set == null) {
            return false;
        }

        //这里返回所有使用了AppLifeCycle 注解的元素
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(RouteNode.class);

        parseRouteNodes(elements);


        //开始生成代理类
        Set<Map.Entry<String, AppLikeProxyClassCreator>> entries = null;
        for (Map.Entry<String, AppLikeProxyClassCreator> entry : entries) {
            String className = entry.getKey();
            AppLikeProxyClassCreator creator = entry.getValue();


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

    private void parseRouteNodes(Set<? extends Element> elements) {

        TypeMirror typeActivity = elementUtils.getTypeElement(Constants.ACTIVITY).asType();

        //遍历所有使用了该注解的元素
        for (Element element : elements) {
            TypeMirror tm = element.asType();
            RouteNode routeNode = element.getAnnotation(RouteNode.class);

            if (types.isSubtype(tm, typeActivity)) {
                logger.info("Found activity route is " + tm.toString());
                Node node = new Node();
                String path = routeNode.path();
                checkPath(path);

                node.setPath(path);
                node.setDesc(routeNode.desc());
                node.setPriority(routeNode.priority());
                node.setNodeType(NodeType.ACTIVITY);
                node.setRawType(element);

                Map<String, Integer> paramsType = new HashMap<>();
                Map<String, Integer> paramsDesc = new HashMap<>();

                //遍历所有的成员变量
                for (Element filed : element.getEnclosedElements()) {
                    if (filed.getKind().isField() && filed.getAnnotation(AutoWired.class) != null) {
                        AutoWired autoWired = filed.getAnnotation(AutoWired.class);
                        paramsType.put(StringU)
                    }
                }
            }
        }
    }

    private void checkPath(String path) {

    }
}

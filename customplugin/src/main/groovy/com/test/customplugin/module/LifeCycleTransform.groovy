package com.test.customplugin.module

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Gradle Transform技术，简单来说就是能够让开发者在项目构建阶段即由class到dex转换期间修改class文件，
 * Transform阶段会扫描所有的class文件和资源文件，找到目标class文件(即IAppLike 其子类的代理类,根据包名和文件名成的规则来确定)，
 * 并通过ASM 在指定文件的方法中插入相关代码。
 *
 * 这里可以使用三种方式来做1）ASM，如当前方式；2）javassist 找到目标class文件（即IAppLike的子类，可以通过获取其实现的接口集合，确定是否包含目标接口）
 * 3)在运行时通过获取指定包名的IAppLike 其子类的代理类，缺点就是影响性能，冷启动会慢
 *
 *
 * 1）找到IAppLike子类的代理类集合，并在{@link AppLifeCycleManager}的loadAppLike()方法中插入
 * registerAppLike("com.test.lifecycle_apt.proxy.fly$$ModuleAAppLike$$Proxy");代码
 * 2）找到作为主工程（即主工程或者单独运行的module）的Application 子类，并在onCreate()方法中插入
 * AppLifeCycleManager.init(getApplicationContext());
 * 避免在每个子module 中添加该方法。
 *
 * 这种方式的缺点：编译事件长
 */
public class LifeCycleTransform extends Transform {

    Project project

    public LifeCycleTransform(Project project) {
        this.project = project
    }

    //该Transform的名称，自定义即可，只是一个标示
    @Override
    public String getName() {
        return "LifeCycleTransform"
    }

    //该Transform支持扫描的文件类型，分为class文件和资源文件，我们这里只处理class文件扫描
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //Transform的扫描范围，扫描整个工程，包括当前module及其他jar包、arr文件等所有的class
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //是否增量扫描
    @Override
    public boolean isIncremental() {
        return true
    }


    /**
     * 扫描所有的文件，找到目标class 文件
     */
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        println("start to transform ----->>>>>>")

        def inputs = transformInvocation.inputs
        def context = transformInvocation.context
        def referencedInputs = transformInvocation.referencedInputs
        def outputProvider = transformInvocation.outputProvider
        def incremental = transformInvocation.incremental

        def appLikeProxyClassList = []

        //inputs就是所有扫描到class文件或者是jar包，一共两种类型
        inputs.each { TransformInput input ->
            //1.遍历所有的class文件目录
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //递归扫描该目录下所有的class文件
                if (directoryInput.file.isDirectory()) {
                    directoryInput.file.eachFileRecurse { File file ->
                        //形如fly$$*****$$Proxy.class的类，是我们要找的目标class,直接通过class的名称来判断，也可以再加上包名的判断，更严谨些
                        if (CusScanUtil.isTargetProxyClass(file)) {
                            println("target's  file.parent is ${file.parent}")
                            //如果是我们自己生产的代理类，保存该类的类名
                            appLikeProxyClassList.add(file.name)
                        }
                    }
                }

                //Transform扫描的class文件是输入文件（input）,有输入必然有输出（output），处理完成后需要将输入文件拷贝到一个输出目录下去
                //后面打包将class 文件转换成dex 文件时，直接采用的就是输出目录下的class文件了，必须这样获取输出路径的目录名称
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //2、遍历查找所有的jar包
            input.jarInputs.each { JarInput jarInput ->
                //println("\n jarInput = ${jarInput}")

                //与处理class文件一样，处理jar包也是一样，最后要将inputs转换为outputs
                def jarName = jarInput.name
                def md5 = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //获取输出路径下的jar 包名称，必须这样获取，得到的输出路径名不能重复，否则会被覆盖
                def dest = outputProvider.getContentLocation(jarName + md5, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    File src = jarInput.file
                    //先简单过滤掉support-v4 之类的jar包，只处理我们业务逻辑的jar包
                    if (CusScanUtil.shouldProcessPreDexJar(src.absolutePath)) {
                        //扫描jar包的核心代码在这里，主要做两件事：
                        //1、扫描该jar包里有没有实现IAppLike接口的代理类；
                        //2、扫描AppLifeCycleManager这个类在那个jar包里，并记录下来，后面需要在该类里动态注入字节码；
                        List<String> list = CusScanUtil.scanJar(src, dest)
                        if (list != null) {
                            appLikeProxyClassList.addAll(list)
                        }
                    }
                }
                //将输入文件拷贝到输出目录下
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        /**
         * 遍历目标class类全名称
         */
        appLikeProxyClassList.forEach({ fileName ->
            println("file name = ${fileName} \n")
        })

        println("包含AppLifeCycleManager类的jar文件")
        println(CusScanUtil.FILE_CONTAINS_INIT_CLASS.getAbsolutePath())

        println("开始自动注册")

        //1、通过前面的步骤，我们已经扫描到所有实现了IAppLike接口的代理类；
        //2、后面需要在AppLifeCycleManager 这个类的初始化方法里，动态注入字节码；
        //3、将所有IAppLike接口的代理类，通过类名进行反射调用实例化；
        //这样最终生成的apk包里，AppLifeCycleManager调用init()方法时，已经可以加载所有组件的生命周期类了；
        new AppLikeCodeInjector(appLikeProxyClassList).execute()

        println("transform finish -----------<<<<\n")
    }
}

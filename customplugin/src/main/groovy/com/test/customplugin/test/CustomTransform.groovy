package com.test.customplugin.test

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.internal.FileUtils
import org.gradle.internal.impldep.org.apache.ivy.util.FileUtil

public class CustomTransform extends Transform {

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        //当前是否是增量编译 由isIncremental() 方法的返回值和当前的编译是否有增量更新基础
        boolean isIncremental = transformInvocation.incremental()

        //消费型输入，可以从中获取jar包和class文件夹路径，需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs()

        //outputProvider 管理输出路径，如果消费型输入为空，则outputProvider 为null;
        def outputProvider = transformInvocation.getOutputProvider()

        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR
                )
                //将修改过的字节码copy道dest,就可以实现编译期间干预字节码的目的了

            }
        }


    }

    @Override
    String getName() {
        return "com.test.customplugin.test.CustomTransform"
    }

    //只获取class 文件
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * SCOPE_FULL_PROJECT:
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //是否开启增量更新
    @Override
    boolean isIncremental() {
        return true
    }
}
package com.test.customplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class MyClassTransform extends Transform {

    private Project mProject

    public MyClassTransform(Project mProject) {
        this.mProject = mProject
    }

    @Override
    public String getName() {
        return MyClassTransform.class.getSimpleName()
    }

    /**
     * 需要处理的数据类型，有两种
     * TransformManager.CONTENT_CLASS：代表处理Java 的class文件
     * TransformManager.CONTENT_RESOURCES 代表处理Java 的资源
     *
     * @return
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指transform 要操作内容的范围
     *
     * @return
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }


    //是否支持增量更新
    @Override
    public boolean isIncremental() {
        return false
    }


    /**
     * Transform 的核心方法
     * inputs 中传过来的是输入流，其中有两种格式，jar包和目录文件
     * outputProvider 获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译报错
     *
     * @param transformInvocation
     */
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        Collection<TransformInput> inputs = transformInvocation.getInputs()
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        boolean incremental = transformInvocation.isIncremental()
        Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs()

        //遍历文件夹
        inputs.each { TransformInput input ->
            //遍历文件夹
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //注入代码
                MyInjects.inject(directoryInput.file.absolutePath, mProject)

                //获取output目录
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)


                //将input 的目录复制到output指定目录
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            //遍历jar 文件 对jar 不操作，但是要输出到out路径
            input.jarInputs.each { JarInput jarInput ->
                //重命名输出文件（同目录copyFile 会出现冲突）
                def jarName = jarInput.name
                println("jar =" + jarInput.file.getAbsoluteFile())

                String md5Name = DigestUtils.md5Hex(jarInput.file.getAbsoluteFile())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}

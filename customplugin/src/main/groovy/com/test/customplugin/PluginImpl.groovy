package com.test.customplugin

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class PluginImpl implements Plugin<Project> {

    //用来记录task 的执行时长等信息
    Map<String, TaskExecuteTimeInfo> timeCostMap = new HashMap<>()

    //用来顺序记录执行的task 名称
    List<String> taskPathList = new ArrayList<>()

    @Override
    void apply(Project project) {

        //监听每个task的执行
        project.getGradle().addListener(new TaskExecutionListener() {
            @Override
            void beforeExecute(Task task) {
                //task开始执行之前收集
                TaskExecuteTimeInfo timeInfo = new TaskExecuteTimeInfo();
                //记录开始时间
                timeInfo.setStart(System.currentTimeMillis())
                timeInfo.setPath(task.getPath())
                timeCostMap.put(timeInfo.getPath(), timeInfo)
                taskPathList.add(timeInfo.getPath())
            }

            @Override
            void afterExecute(Task task, TaskState state) {
                //task 执行完之后，记录结束时的时间
                TaskExecuteTimeInfo timeInfo = timeCostMap.get(task.getPath())
                timeInfo.setEnd(System.currentTimeMillis())
                timeInfo.setTotal(timeInfo.getEnd() - timeInfo.getStart())
            }
        })


        project.getGradle().addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {

            }

            @Override
            void settingsEvaluated(Settings settings) {

            }

            @Override
            void projectsLoaded(Gradle gradle) {

            }

            @Override
            void projectsEvaluated(Gradle gradle) {

            }

            @Override
            void buildFinished(BuildResult result) {
                println("--------------------")
                //按task 执行顺序打印出执行时长信息
                for (String path : taskPathList) {
                    long total = timeCostMap.get(path).total
                    println(path + "，， 时间：：" + total)
                    println("--------------------")
                }
                println("--------------------")
            }
        })
    }


    class TaskExecuteTimeInfo {
        long total
        String path
        long start
        long end
    }


}
package org.moe.idea.extension

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import org.gradle.tooling.model.idea.IdeaModule
import org.jetbrains.plugins.gradle.model.GradleExtensions
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.moe.gradle.model.GradlePluginModel
import org.moe.idea.MOESdkPlugin
import org.moe.idea.model.GradleModuleModel

class MOEGradleProjectResolver : AbstractProjectResolverExtension() {
    override fun createModule(gradleModule: IdeaModule, projectDataNode: DataNode<ProjectData>): DataNode<ModuleData>? {
        val moduleDataNode = nextResolver.createModule(gradleModule, projectDataNode)
        if (moduleDataNode == null) {
            return moduleDataNode
        }

        val exts = resolverCtx.getExtraProject(gradleModule, GradleExtensions::class.java)
        val moeExt = exts?.extensions?.singleOrNull { it.name == "moe" }

        var gradlePluginModel = resolverCtx.getExtraProject(gradleModule, GradlePluginModel::class.java)
        if (moeExt != null || gradlePluginModel != null) {

            if(gradlePluginModel == null) {
                // Fake a plugin model to support old gradle plugin
                gradlePluginModel = when (moeExt!!.typeFqn) {
                    "org.moe.gradle.MoeSDKExtension" -> DummyGradlePluginModel(false)
                    "org.moe.gradle.MoeExtension" -> DummyGradlePluginModel(true)
                    else -> null
                }
            }

            moduleDataNode.createChild(
                GradleModuleModel.KEY,
                GradleModuleModel(
                    moduleName = moduleDataNode.data.internalName,
                    gradlePlugins = gradlePluginModel?.gradlePluginList?.toList() ?: emptyList(),
                    taskNames = gradleModule.getTaskNames()
                )
            )
        }

        return moduleDataNode
    }

    override fun getExtraProjectModelClasses(): MutableSet<Class<*>> {
        return mutableSetOf(
            GradlePluginModel::class.java
        )
    }

    private class DummyGradlePluginModel(
        private val isMOEApp: Boolean
    ): GradlePluginModel{
        override fun getGradlePluginList(): MutableCollection<String> = if(isMOEApp) {
            mutableListOf(MOESdkPlugin.GRADLE_PLUGIN_MOE)
        } else {
            mutableListOf(MOESdkPlugin.GRADLE_PLUGIN_MOE_SDK)
        }
    }

    companion object {
        private fun IdeaModule.getTaskNames(): List<String> {
            return gradleProject.tasks.mapNotNull { task ->
                task.name.takeIf { it.isNotBlank() }
            }
        }
    }
}

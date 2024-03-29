package org.moe.idea.model.impl

import org.moe.gradle.model.GraalVMProperties
import org.moe.gradle.model.MOESdkProperties
import org.moe.idea.extension.MOEGradleProjectResolver.Companion.readGradleModelOptionalProperty
import java.io.Serializable

data class MOESdkPropertiesImpl(
    override val home: String,
    override val version: String?,
    override val coreJar: String,
    override val platformJar: String?,
    override val junitJar: String,
    override val graalVM: GraalVMProperties?,
) : MOESdkProperties, Serializable {
    constructor(input: MOESdkProperties) : this(
        home = input.home,
        version = input.readGradleModelOptionalProperty { version },
        coreJar = input.coreJar,
        platformJar = input.platformJar,
        junitJar = input.junitJar,
        graalVM = input.readGradleModelOptionalProperty { graalVM }?.let(::GraalVMPropertiesImpl),
    )
}

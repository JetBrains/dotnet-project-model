/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

@file:Suppress("unused")

package org.jetbrains.dotnet.discovery

/**
 * Represents dnx project model.
 */
class JsonProjectDto {
    var testRunner: String? = null
    var configurations: Map<String, Any>? = null
    var frameworks: Map<String, Any>? = null
    var runtimes: Map<String, Any>? = null
    var dependencies: Map<String, Any>? = null
}

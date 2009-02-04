/*
 * Copyright 2008 Nokia Siemens Networks Oyj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robotframework.jvmconnector.launch.jnlp;

import java.io.File;

import org.robotframework.javalib.beans.common.URLFileFactory;

public class JarExtractor {
    private final URLFileFactory fileFactory;

    public JarExtractor(URLFileFactory fileFactory) {
        this.fileFactory = fileFactory;
    }

    public Jar createMainJar(JNLPElement jnlpRootElement) {
        String codeBase = jnlpRootElement.getAttributeValue("codebase");
        JNLPElement jarElement = jnlpRootElement.getFirstChildElement("resources").getFirstChildElement("jar");
        String jarHref = jarElement.getAttributeValue("href");
        File jarFile = fileFactory.createFileFromUrl(codeBase + "/" + jarHref);
        return createJar(jarFile);
    }

    Jar createJar(File jarFile) {
        return new JarImpl(jarFile);
    }
}

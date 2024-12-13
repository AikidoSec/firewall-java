package dev.aikido.agent.helpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ClassLoader {
    private static final Logger logger = LogManager.getLogger(ClassLoader.class);

    public static Method fetchMethod(String classPkg, String methodName) {
        try {
            String jarFilePath = System.getProperty("AIK_agent_api_jar");
            URLClassLoader classLoader = null;
            URL[] urls = {new URL(jarFilePath)};
            classLoader = new URLClassLoader(urls);
            if (classLoader == null) {
                return null;
            }

            // Load the class from the JAR
            Class<?> clazz = classLoader.loadClass(classPkg);

            // Run report with "argument"
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            classLoader.close(); // Close the class loader
        } catch(Exception e) {
            logger.debug(e);
        }
        return null;
    }
}

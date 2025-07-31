package com.chen.jatool.common.utils;


import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

/**
 */
@Slf4j
public class TemplateUtil {

    private static TemplateEngine templateEngine;

    public static TemplateEngine getEngine() {
        if (templateEngine != null) {
            return templateEngine;

        }
        SpringTemplateEngine engine = new SpringTemplateEngine();

        ClassLoaderTemplateResolver htmlResolver = new ClassLoaderTemplateResolver();
        htmlResolver.setPrefix("/static/thymeleaf/"); // Location in resources folder
        htmlResolver.setSuffix(".html"); // File extension
        htmlResolver.setTemplateMode("HTML");
        htmlResolver.setCharacterEncoding("UTF-8");
        htmlResolver.setCacheable(false); // Disable cache for development

        engine.addTemplateResolver(htmlResolver);

        templateEngine = engine;
        return engine;
    }


    public static String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return getEngine().process(templateName, context);
    }
}

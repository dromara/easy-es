package org.dromara.easyes.core.toolkit;

import org.dromara.easyes.common.utils.LogUtils;
import org.dromara.easyes.common.utils.StringUtils;
import org.dromara.easyes.core.config.GeneratorConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.dromara.easyes.common.constants.BaseEsConstants.*;

/**
 * entity代码生成器
 *
 * @author hwy
 **/
public class EntityGenerator {
    private static final String UNDERLINE = "_";
    private static final String USER_DIR = "user.dir";
    private static final String SRC = "src";
    private static final String MAIN = "main";
    private static final String JAVA = "java";


    private static final String PACKAGE = "package %s;\n\n";

    private static final char PACKAGE_SEPARATOR = '.';

    private static final String LOMBOK_IMPORT = "import lombok.Data;\n";
    private static final String EE_IMPORT = "import org.dromara.easyes.annotation.*;\n\n";

    private static final Pattern ILLEGAL_FIELD_NAME_PATTERN = Pattern.compile("^[^a-zA-Z_$]|[\\W&&[^_]]");


    private static final String LOMBOK_TEMPLATE = "public class %s {\n" +
            "    // Fields\n" +
            "%s" +
            "}\n";
    private static final String TEMPLATE = "public class %s {\n" +
            "    // Fields\n" +
            "%s" +
            "\n" +
            "    // Getters and Setters\n" +
            "%s" +
            "\n" +
            "%s" +
            "\n" +
            "}\n";
    private static final String CLASS_ANNOTATION = "@IndexName(\"%s\")\n" + "@Settings(shardsNum = %d, replicasNum = %d)\n";
    private static final String LOMBOK_ANNOTATION = "@Data\n";
    private static final String FIELD_TEMPLATE = "    private %s %s;\n";
    private static final String GETTER_TEMPLATE = "    public %s get%s() {\n" +
            "        return this.%s;\n" +
            "    }\n";
    private static final String SETTER_TEMPLATE = "    public void set%s(%s %s) {\n" +
            "        this.%s = %s;\n" +
            "    }\n";

    private static final String JAVA_SUFFIX = ".java";

    /**
     * entity 生成器
     *
     * @param config      配置
     * @param fields      字段及类型映射
     * @param className   生成的类名
     * @param shardsNum   分片
     * @param replicasNum 副本
     * @throws IOException 异常
     */
    public static void generateEntity(GeneratorConfig config, Map<String, String> fields, String className, Integer shardsNum, Integer replicasNum) throws IOException {
        // build path info
        className = config.isEnableUnderlineToCamelCase() ? capitalize(StringUtils.underlineToCamel(className))
                : capitalize(className);
        String indexName = config.isEnableUnderlineToCamelCase() ? capitalize(StringUtils.underlineToCamel(config.getIndexName()))
                : capitalize(config.getIndexName());

        // main class
        boolean mainClass = Objects.equals(className, indexName);

        String wholePath = System.getProperty(USER_DIR) + File.separator + SRC + File.separator + MAIN + File.separator + JAVA + File.separator + packageToPath(config.getDestPackage());
        Path outputPath = Paths.get(wholePath, className + JAVA_SUFFIX);
        if (!Files.exists(outputPath.getParent())) {
            Files.createDirectories(outputPath.getParent());
        }

        StringBuilder fieldBuilder = new StringBuilder();
        StringBuilder getterBuilder = new StringBuilder();
        StringBuilder setterBuilder = new StringBuilder();

        // build field info
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            if (config.isEnableUnderlineToCamelCase()) {
                fieldName = StringUtils.underlineToCamel(fieldName);
            }

            // escape illegal sign
            fieldName = sanitizeFieldName(fieldName);

            String fieldType = entry.getValue();

            fieldBuilder.append(String.format(FIELD_TEMPLATE, fieldType, fieldName));
            if (!config.isEnableLombok()) {
                getterBuilder.append(String.format(GETTER_TEMPLATE, fieldType, capitalize(fieldName), fieldName));
                setterBuilder.append(String.format(SETTER_TEMPLATE, capitalize(fieldName), fieldType, fieldName, fieldName, fieldName));
            }
        }

        // build content
        String content;
        if (config.isEnableLombok()) {
            content = String.format(PACKAGE, config.getDestPackage()) + LOMBOK_IMPORT + EE_IMPORT + addIndexAnnotation(indexName, shardsNum, replicasNum, mainClass) +
                    LOMBOK_ANNOTATION + String.format(LOMBOK_TEMPLATE, className, fieldBuilder);
        } else {
            content = String.format(PACKAGE, config.getDestPackage()) + EE_IMPORT + addIndexAnnotation(indexName, shardsNum, replicasNum, mainClass) +
                    String.format(TEMPLATE, className, fieldBuilder, getterBuilder, setterBuilder);
        }

        // write class to file
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(content);
            LogUtils.info("Generated entity class: " + outputPath);
        }
    }

    /**
     * 将Java包名转换为对应的文件系统路径。
     *
     * @param packageName Java包的全名，如 "com.example.project.module"
     * @return 对应于文件系统的路径，使用正确的路径分隔符
     */
    private static String packageToPath(String packageName) {
        // 将包名中的点（.）替换为当前系统的路径分隔符
        return packageName.replace(PACKAGE_SEPARATOR, File.separatorChar);
    }

    /**
     * 首字母大写
     *
     * @param str 原字符串
     * @return 首字母大写后的字符串
     */
    private static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(ZERO)) + str.substring(ONE);
    }

    /**
     * 移除字符串中的非法字符，使其适合作为Java类的字段名
     *
     * @param fieldName 原始字段名字符串
     * @return 符合Java命名规范的字段名
     */
    private static String sanitizeFieldName(String fieldName) {
        // 使用正则表达式替换非法字符为空字符，同时确保字段名不以数字开头
        String sanitized = ILLEGAL_FIELD_NAME_PATTERN.matcher(fieldName).replaceAll(EMPTY_STR);
        if (Character.isDigit(sanitized.charAt(ZERO))) {
            // 如果第一个字符是数字，前缀加上下划线
            sanitized = UNDERLINE + sanitized;
        }
        return sanitized;
    }

    private static String addIndexAnnotation(String indexName, Integer shardsNum, Integer replicasNum, boolean mainClass) {
        if (mainClass) {
            return String.format(CLASS_ANNOTATION, indexName, shardsNum, replicasNum);
        } else {
            return EMPTY_STR;
        }
    }

}